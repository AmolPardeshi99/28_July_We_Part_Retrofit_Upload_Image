package com.example.retrofit_3;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ImageView mIvGallery;
    private Button mBtnOpenGallery;
    private Button mBtnUploadImage;
    private String imagePath;

    private ActivityResultLauncher<Intent> launchGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getData() != null) {
                        Uri selectedInput = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(selectedInput);
                            mIvGallery.setImageBitmap(BitmapFactory.decodeStream(inputStream));
                            getImagePathFromUri(selectedInput);
//                            Cursor c = getImagePathFromUri(selectedInput);
//                            c.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mIvGallery = findViewById(R.id.imageView);
        mBtnOpenGallery = findViewById(R.id.btnGallery);
        mBtnUploadImage = findViewById(R.id.btnUpload);
        mBtnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ifpermissionGranted()) {
                    openGallery();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }

            }
        });

        mBtnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void uploadImage() {
        ApiService apiService = Network.getInstance().create(ApiService.class);
        File file = new File(imagePath); //convert image path to file
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
        apiService.uploadImage(multipartBody, "amol").enqueue(
                new Callback<ResponseDTO>() {
                    @Override
                    public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                        Toast.makeText(MainActivity.this, "Sucess", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onFailure(Call<ResponseDTO> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();

                    }
                }
        );
    }


    private boolean ifpermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launchGallery.launch(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NotNull
    private Cursor getImagePathFromUri(Uri selectedImage) {
        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor c = getContentResolver().query(selectedImage, filePath,
                null, null, null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePath[0]);
        imagePath = c.getString(columnIndex);
        return c;
    }
}