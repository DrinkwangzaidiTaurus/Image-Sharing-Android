package com.example.sharepictures.ui.upload;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sharepictures.Connect;
import com.example.sharepictures.LoginActivity;
import com.example.sharepictures.R;
import com.example.sharepictures.databinding.FragmentDashboardBinding;

import java.io.ByteArrayOutputStream;

public class DashboardFragment extends Fragment {//发布照片

    private byte[] imagedata;
    private Bitmap imagebm;
    private FragmentDashboardBinding binding;
    private boolean isImageSelected = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //选择上传图片
        binding.imageButton.setOnClickListener(new View.OnClickListener() {//选择上传的图片
            @Override
            public void onClick(View view) {
                //检测是否进行了授权
                if (ContextCompat.checkSelfPermission(DashboardFragment.this.getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DashboardFragment.this.getActivity(), new
                            String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    //打开系统相册
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1);//请求标识为1
                }
            }
        });

        //删除图标
        binding.clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.PicturesDetails.setText("");
            }
        });


        binding.fabu.setOnClickListener(new View.OnClickListener() {//发布照片按钮
            @Override
            public void onClick(View view) {

                if (!isImageSelected) {
                    Toast.makeText(getContext(), "请选择上传的图片", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 获取输入的图片编号和图片详情
                String pictureId = binding.PictureId.getText().toString();
                String pictureDetails = binding.PicturesDetails.getText().toString();

                if (TextUtils.isEmpty(pictureId)) {
                    // 图片主题为空时显示错误提示
                    Toast.makeText(getContext(), "请输入图片主题", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(pictureDetails)) {
                    // 图片详情为空时显示错误提示
                    Toast.makeText(getContext(), "请输入图片详情", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 在这里获取当前登录用户的信息，例如用户的ID
                String uploaderId = LoginActivity.getCurrentUserId();
                byte[] imageBytes = imagedata;

                Thread insertThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int value01 = 0;
                        try {
                            try {
                                value01 = Connect.insertPicture(pictureId, pictureDetails, -1, imageBytes, uploaderId);
                            } catch (java.sql.SQLException e) {
                                throw new RuntimeException(e);
                            }
                            final int resultValue = value01; // Store the result value to use in the UI thread

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (resultValue == 1) {
                                        Toast.makeText(getContext(), "发布成功，请刷新图片动态", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(getContext(), "发布失败，主题已被使用", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (SQLException ex) {
                        }
                    }
                });
                // 启动子线程
                insertThread.start();
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 如果请求代码为1，并且返回结果为OK（图片选择成功）并且数据不为空
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData(); // 获取选择的图片的URI
            String[] filePathColumns = {MediaStore.Images.Media.DATA}; // 定义查询的列名
            Cursor c = DashboardFragment.this.getActivity().getContentResolver().query(selectedImage, filePathColumns, null, null, null);

            // 检查游标是否为空
            if (c != null) {
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(filePathColumns[0]); // 获取列索引
                    String imagePath = c.getString(columnIndex); // 获取图片路径

                    if (!TextUtils.isEmpty(imagePath)) {
                        showImage(imagePath); // 调用方法显示图片
                        isImageSelected = true; // 标记图片已选择
                    } else {
                        Toast.makeText(getContext(), "无法获取图片路径", Toast.LENGTH_SHORT).show();
                    }
                }
                c.close(); // 关闭游标
            }
        }
        // 如果返回结果为CANCELED（取消选择图片）
        else if (resultCode == Activity.RESULT_CANCELED) {
            // 清除已选择的图片和图片数据
            imagedata = null;
            imagebm = null;
            // 设置图片按钮为默认的相机图标
            binding.imageButton.setImageResource(R.drawable.ic_baseline_photo_camera_24);
        }
    }

    private void showImage(String imagePath) {
        imagebm = BitmapFactory.decodeFile(imagePath);

        // 压缩图片以确保大小不超过20KB
        int quality = 100;
        int maxFileSize = 20 * 1024; // 20KB字节
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        do {
            baos.reset();
            imagebm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            quality -= 10;
        } while (baos.size() > maxFileSize && quality > 0);

        imagedata = baos.toByteArray();// 存储压缩后的图片数据
        binding.imageButton.setImageBitmap(imagebm);// 将压缩后的图片显示在按钮上

        Log.d("ImageSize", "Compressed Image Size: " + (imagedata.length / 1024) + "KB");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 销毁绑定的视图
    }
}