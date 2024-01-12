package com.example.sharepictures;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ImageZoomActivity extends AppCompatActivity {

    private ImageView zoomedImageView;// 用于显示缩放后的图像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        zoomedImageView = findViewById(R.id.zoomedImageView);

        byte[] imageData = getIntent().getByteArrayExtra("imageData");
        if (imageData != null) {
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            zoomedImageView.setImageBitmap(imageBitmap);
        } else {
            // 处理imageData为空的情况
            Toast.makeText(this, "无法读取图像", Toast.LENGTH_SHORT).show();
        }
    }
}
