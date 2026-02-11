package com.emergency.alert;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageEditorActivity extends AppCompatActivity {

    private ImageView imageView;
    private SeekBar seekBar;

    private Bitmap originalBitmap;
    private Bitmap editedBitmap;
    private Uri savedImageUri;

    private enum EditMode { NONE, BRIGHTNESS, BLUR }
    private EditMode currentMode = EditMode.NONE;

    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

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
        Button btnShare = findViewById(R.id.btn_share);

        seekBar.setMax(100);
        seekBar.setProgress(50);

        setupLaunchers();

        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());

        btnBrightness.setOnClickListener(v -> {
            currentMode = EditMode.BRIGHTNESS;
            seekBar.setVisibility(SeekBar.VISIBLE);
        });

        btnBlur.setOnClickListener(v -> {
            currentMode = EditMode.BLUR;
            seekBar.setVisibility(SeekBar.VISIBLE);
        });

        btnSave.setOnClickListener(v -> saveToGallery());
        btnShare.setOnClickListener(v -> shareImage());

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

    private void setupLaunchers() {

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        originalBitmap = addTimestampWatermark(bitmap);
                        editedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        imageView.setImageBitmap(editedBitmap);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    try {
                        Bitmap bmp = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(uri));
                        originalBitmap = addTimestampWatermark(bmp);
                        editedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        imageView.setImageBitmap(editedBitmap);
                    } catch (Exception e) {
                        Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openCamera() {
        if (checkPermission(Manifest.permission.CAMERA)) {
            cameraLauncher.launch(null);
        }
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private boolean checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 301);
            return false;
        }
        return true;
    }

    private Bitmap addTimestampWatermark(Bitmap bitmap) {
        Bitmap result = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(28f);
        paint.setShadowLayer(1f, 1f, 1f, Color.BLACK);

        String timestamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        float x = result.getWidth() - paint.measureText(timestamp) - 20;
        float y = result.getHeight() - 20;

        canvas.drawText(timestamp, x, y, paint);
        return result;
    }

    private Bitmap applyBrightness(Bitmap bmp, int value) {
        ColorMatrix matrix = new ColorMatrix(new float[]{
                1, 0, 0, 0, value,
                0, 1, 0, 0, value,
                0, 0, 1, 0, value,
                0, 0, 0, 1, 0
        });

        Bitmap out = Bitmap.createBitmap(
                bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas canvas = new Canvas(out);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        canvas.drawBitmap(bmp, 0, 0, paint);
        return out;
    }

    private Bitmap applyBlur(Bitmap bmp, int scale) {
        Bitmap small = Bitmap.createScaledBitmap(
                bmp, bmp.getWidth() / scale, bmp.getHeight() / scale, false);
        return Bitmap.createScaledBitmap(
                small, bmp.getWidth(), bmp.getHeight(), false);
    }

    private void saveToGallery() {
        if (editedBitmap == null) return;

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME,
                    "Evidence_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/EmergencyAlert");

            savedImageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            OutputStream out = getContentResolver().openOutputStream(savedImageUri);
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            out.close();

            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        if (savedImageUri == null) {
            Toast.makeText(this, "Save image before sharing", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, savedImageUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share evidence using"));
    }
}
