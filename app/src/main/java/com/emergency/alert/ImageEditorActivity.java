package com.emergency.alert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageEditorActivity extends AppCompatActivity {

    private ImageView imageView;
    private SeekBar seekBar;
    private Bitmap originalBitmap, editedBitmap;
    private Uri imageUri;

    private enum EditMode { NONE, BRIGHTNESS, BLUR }
    private EditMode currentMode = EditMode.NONE;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);

        imageView = findViewById(R.id.image_view);
        seekBar = findViewById(R.id.seekbar_edit);

        Button btnCamera = findViewById(R.id.btn_capture);
        Button btnGallery = findViewById(R.id.btn_gallery);
        Button btnBrightness = findViewById(R.id.btn_brightness);
        Button btnBlur = findViewById(R.id.btn_blur);
        Button btnSave = findViewById(R.id.btn_save);

        seekBar.setMax(100);
        seekBar.setProgress(50);

        setupActivityLaunchers();

        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());

        btnBrightness.setOnClickListener(v -> {
            currentMode = EditMode.BRIGHTNESS;
            Toast.makeText(this, "Adjust brightness", Toast.LENGTH_SHORT).show();
        });

        btnBlur.setOnClickListener(v -> {
            currentMode = EditMode.BLUR;
            Toast.makeText(this, "Adjust blur", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> saveImage());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (originalBitmap == null) return;

                if (currentMode == EditMode.BRIGHTNESS) {
                    editedBitmap = applyBrightness(originalBitmap, progress - 50);
                } else if (currentMode == EditMode.BLUR) {
                    editedBitmap = applyBlur(originalBitmap, Math.max(1, progress / 10));
                }

                imageView.setImageBitmap(editedBitmap);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupActivityLaunchers() {

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && imageUri != null) {
                        loadBitmap(imageUri);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        loadBitmap(result.getData().getData());
                    }
                });
    }

    private void openCamera() {
        if (!checkPermission(Manifest.permission.CAMERA)) return;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = createImageFile();
        imageUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                file
        );
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        galleryLauncher.launch(
                new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        );
    }

    private boolean checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 101);
            return false;
        }
        return true;
    }

    private void loadBitmap(Uri uri) {
        try {
            originalBitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(uri)
            );
            editedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageView.setImageBitmap(editedBitmap);
            seekBar.setProgress(50);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_" + time + ".jpg");
    }

    private Bitmap applyBrightness(Bitmap bmp, int value) {
        ColorMatrix cm = new ColorMatrix(new float[]{
                1, 0, 0, 0, value,
                0, 1, 0, 0, value,
                0, 0, 1, 0, value,
                0, 0, 0, 1, 0
        });

        Bitmap out = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return out;
    }

    private Bitmap applyBlur(Bitmap bmp, int scale) {
        Bitmap small = Bitmap.createScaledBitmap(
                bmp, bmp.getWidth() / scale, bmp.getHeight() / scale, false);
        return Bitmap.createScaledBitmap(small, bmp.getWidth(), bmp.getHeight(), false);
    }

    private void saveImage() {
        if (editedBitmap == null) return;

        try {
            File file = createImageFile();
            FileOutputStream fos = new FileOutputStream(file);
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            Toast.makeText(this, "Saved: " + file.getName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }
}
