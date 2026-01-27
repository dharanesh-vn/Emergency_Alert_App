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
    private Button btnCapture, btnGallery, btnCrop, btnResize, btnBrightness, btnBlur, btnSave;
    private SeekBar seekBarEdit;

    private Bitmap originalBitmap;
    private Bitmap editedBitmap;
    private Uri currentPhotoUri;

    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_GALLERY = 102;

    private enum EditMode { NONE, BRIGHTNESS, BLUR }
    private EditMode currentMode = EditMode.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);

        initializeViews();
        setupListeners();
        checkCameraPermission();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.image_view);
        btnCapture = findViewById(R.id.btn_capture);
        btnGallery = findViewById(R.id.btn_gallery);
        btnCrop = findViewById(R.id.btn_crop);
        btnResize = findViewById(R.id.btn_resize);
        btnBrightness = findViewById(R.id.btn_brightness);
        btnBlur = findViewById(R.id.btn_blur);
        btnSave = findViewById(R.id.btn_save);
        seekBarEdit = findViewById(R.id.seekbar_edit);

        seekBarEdit.setMax(100);
        seekBarEdit.setProgress(50); // neutral
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    private void setupListeners() {

        btnCapture.setOnClickListener(v -> captureImage());
        btnGallery.setOnClickListener(v -> selectFromGallery());

        btnCrop.setOnClickListener(v -> cropImage());
        btnResize.setOnClickListener(v -> resizeImage());

        btnBrightness.setOnClickListener(v -> {
            currentMode = EditMode.BRIGHTNESS;
            Toast.makeText(this, "Use slider to adjust brightness (+ / -)", Toast.LENGTH_SHORT).show();
        });

        btnBlur.setOnClickListener(v -> {
            currentMode = EditMode.BLUR;
            Toast.makeText(this, "Use slider to adjust blur (+ / -)", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> saveImage());

        seekBarEdit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (originalBitmap == null) return;

                int value = progress - 50; // -50 to +50

                if (currentMode == EditMode.BRIGHTNESS) {
                    editedBitmap = applyBrightness(originalBitmap, value);
                } else if (currentMode == EditMode.BLUR) {
                    editedBitmap = applyBlur(originalBitmap, Math.max(1, progress / 10));
                }

                if (editedBitmap != null) {
                    imageView.setImageBitmap(editedBitmap);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File file = createImageFile();
            currentPhotoUri = FileProvider.getUriForFile(
                    this, "com.emergency.alert.fileprovider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void selectFromGallery() {
        startActivityForResult(
                new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                REQUEST_GALLERY);
    }

    private File createImageFile() {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_" + time + ".jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK) {
                Uri uri = (requestCode == REQUEST_CAMERA) ? currentPhotoUri : data.getData();
                originalBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                editedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(editedBitmap);
                seekBarEdit.setProgress(50);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void cropImage() {
        if (originalBitmap == null) return;

        int w = originalBitmap.getWidth();
        int h = originalBitmap.getHeight();
        editedBitmap = Bitmap.createBitmap(originalBitmap, w / 10, h / 10,
                w * 8 / 10, h * 8 / 10);
        imageView.setImageBitmap(editedBitmap);
    }

    private void resizeImage() {
        if (originalBitmap == null) return;
        editedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                originalBitmap.getWidth() / 2,
                originalBitmap.getHeight() / 2,
                true);
        imageView.setImageBitmap(editedBitmap);
    }

    private Bitmap applyBrightness(Bitmap bitmap, int value) {
        ColorMatrix matrix = new ColorMatrix(new float[]{
                1, 0, 0, 0, value,
                0, 1, 0, 0, value,
                0, 0, 1, 0, value,
                0, 0, 0, 1, 0
        });

        Bitmap out = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return out;
    }

    private Bitmap applyBlur(Bitmap bitmap, int scale) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap small = Bitmap.createScaledBitmap(bitmap, w / scale, h / scale, false);
        return Bitmap.createScaledBitmap(small, w, h, false);
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
