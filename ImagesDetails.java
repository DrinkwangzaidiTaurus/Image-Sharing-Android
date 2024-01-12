package com.example.sharepictures;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.sharepictures.ui.home.HomeFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImagesDetails extends AppCompatActivity {
    private byte[] imagedata;
    private File appDir;
    private ImageView picture;
    private TextView pictureId;
    private TextView Imagesdetails;
    private ImageView like;
    private ImageView download;
    private ImageView share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagesdetails);

        picture = findViewById(R.id.imageView);
        pictureId = findViewById(R.id.setId);
        Imagesdetails = findViewById(R.id.Imagesdetails);

        like = findViewById(R.id.like);
        download = findViewById(R.id.saveimage);
        share = findViewById(R.id.share);

        Intent intent = getIntent();
        String idn = intent.getStringExtra("idnum"); // 使用"idnum"来获取传递的idnum值

        final int[] likeSwitch = {0};
        final String[] det = {null};
        String currentid = LoginActivity.getCurrentUserId();

        //final int[] likevalue = {0};
        Thread thread = new Thread(() -> {
            // 在这里放入要在子线程中执行的代码
            likeSwitch[0]=Connect.querylikes(idn, currentid);
        });

        // 启动线程
        thread.start();
        // 创建线程池
        ExecutorService executorService = Executors.newSingleThreadExecutor();


        final String[] authorUsername = new String[1];
        // 异步执行数据库查询操作
        executorService.execute(() -> {
            try {
                final ResultSet resultSet = Connect.queryPictureDetails(idn);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (resultSet != null && resultSet.next()) {
                                pictureId.setText(idn);
                                Imagesdetails.setText(resultSet.getString("details"));
                                det[0] =resultSet.getString("details");

                                byte[] imageData = resultSet.getBytes("pictures");
                                imagedata = imageData;
                                Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                                picture.setImageBitmap(imageBitmap);

                                TextView authorUsernameTextView = findViewById(R.id.authorUsername);
                                authorUsername[0] = resultSet.getString("uploader");
                                authorUsernameTextView.setText(authorUsername[0]);

                                if(likeSwitch[0]==1){
                                    like.setImageResource(R.drawable.heart_color);
                                }
                                if(likeSwitch[0]==-1){
                                    like.setImageResource(R.drawable.heart);
                                }

                            }


                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });


            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        executorService.shutdown();

        // 删除帖子按钮点击事件
        ImageView deleteButton = findViewById(R.id.deleteimageview);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取当前登录用户的信息，这里需要替换成你的实际逻辑
                String currentLoggedInUser = LoginActivity.getCurrentUserId();
                // 获取帖子的上传者信息
                String uploader = authorUsername[0];
                // 检查当前登录用户是否有权删除帖子
                if (currentLoggedInUser != null && currentLoggedInUser.equals(uploader)) {
                    // 当前用户有权删除，执行删除操作
                    Thread deleteThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int resultvalue = Connect.deletePicture(idn);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (resultvalue == 1) {
                                        Toast.makeText(ImagesDetails.this, "删除成功,请点击图片动态按钮进行刷新", Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    } else {
                                        Toast.makeText(ImagesDetails.this, "删除失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                    deleteThread.start();
                } else {
                    // 当前用户无权删除，显示提示消息
                    Toast.makeText(ImagesDetails.this, "您无权删除这个帖子", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //图片放大
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent zoomIntent = new Intent(ImagesDetails.this, ImageZoomActivity.class);
                zoomIntent.putExtra("imageData", imagedata);
                startActivity(zoomIntent);
            }
        });

        // 点赞按钮
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (likeSwitch[0] == -1) {
                    // 从未点赞到已点赞状态
                    like.setImageResource(R.drawable.heart_color);
                    likeSwitch[0] = 1;
                }else if(likeSwitch[0] == 1) {
                    // 从已点赞到未点赞状态
                    like.setImageResource(R.drawable.heart);
                    likeSwitch[0] = -1;
                }

                final int[] lastResult = {-3};
                // 创建一个新的线程来执行网络请求
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        lastResult[0] = Connect.likeOrUnlikePicture(idn,currentid,likeSwitch[0], det[0],imagedata ,authorUsername[0]);

                        // 切换回主线程以更新UI
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (lastResult[0] == 1) {
                                    Toast.makeText(ImagesDetails.this, "点赞成功，请刷新图片动态", Toast.LENGTH_SHORT).show();
                                } else if (lastResult[0] == -1) {
                                    Toast.makeText(ImagesDetails.this, "取消点赞成功，请刷新图片动态", Toast.LENGTH_SHORT).show();
                                } else if (lastResult[0] == 2) {
                                    Toast.makeText(ImagesDetails.this, "不可重复点赞，请刷新图片动态", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        // 下载按钮点击事件
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                saveImageToGallery(ImagesDetails.this, imagebm);
            }
        });

        // 分享按钮点击事件
        //将图像数据保存到媒体存储库，并返回一个URI，然后使用这个URI来分享图像
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 保存图像到媒体存储库并获取URI
                Uri imageUri = saveImageToMediaStore(ImagesDetails.this, imagedata);

                // 调用分享方法
                shareImage(ImagesDetails.this, imageUri, "分享到……");
            }
        });
    }

    // 保存图像到媒体存储库并返回URI
    public Uri saveImageToMediaStore(Context context, byte[] imageData) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri imageUri = null;
        try {
            // 插入图像数据到媒体存储库
            imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (imageUri != null) {
                // 打开输出流，将图像数据写入到媒体存储库中 字节
                OutputStream outputStream = context.getContentResolver().openOutputStream(imageUri);
                if (outputStream != null) {
                    outputStream.write(imageData);
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageUri;
    }

    // 保存图片到相册
    public void saveImageToGallery(Context context, Bitmap bmp) {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

            for (String str : permissions) {
                if (ImagesDetails.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    ImagesDetails.this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                } else {
                    appDir = new File(context.getExternalFilesDir(null).getPath() + "/SharePictures");
                    appDir.mkdir();
                }
            }
        }

        //生成唯一的文件名
        //将当前时间的毫秒数转换为一个字符串
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir + "/" + fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

        } catch (FileNotFoundException e) {
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(file.getPath()))));
    }

    //分享图片
    public static void shareImage(Context context, Uri uri, String title) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);//隐式启动activity
        shareIntent.setType("image/*");
        context.startActivity(Intent.createChooser(shareIntent, title));
    }


}
