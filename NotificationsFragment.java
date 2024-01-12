package com.example.sharepictures.ui.personal;

import android.content.Context;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.DialogInterface;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.sharepictures.Connect;
import com.example.sharepictures.LoginActivity;
import com.example.sharepictures.R;
import com.example.sharepictures.databinding.FragmentNotificationsBinding;

import java.sql.SQLException;

public class NotificationsFragment extends Fragment {//这是个人信息

    private FragmentNotificationsBinding binding;
    private String username;
    private String password;
    private byte[] imagedata;
    private Bitmap imagebm;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // 绑定布局
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        // 获取根视图
        View root = binding.getRoot();

        // 获取SharedPreferences中存储的用户账户信息
        String prefsName = getResources().getString(R.string.shared_preferences_file_name);
        String accountKey = getResources().getString(R.string.login_account_name);
        String passwordKey = getResources().getString(R.string.login_password);
        SharedPreferences spf = this.getActivity().getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        username = spf.getString(accountKey, "");
        password = spf.getString(passwordKey, "");

        binding.textPerson.setText(username);

        // 检查是否已经查询过头像
        boolean hasCachedImage = spf.getBoolean("has_cached_image_" + username, false);
        if (!hasCachedImage) {
            // 如果没有缓存的头像数据，进行查询
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final byte[] imagedata = Connect.queryTouxiangById(username);

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (imagedata != null && imagedata.length > 0) {
                                    // 缓存头像数据
                                    SharedPreferences.Editor editor = spf.edit();
                                    editor.putString("cached_image_" + username, Base64.encodeToString(imagedata, Base64.DEFAULT));
                                    editor.putBoolean("has_cached_image_" + username, true);
                                    editor.apply();

                                    //展示获取的头像数据
                                    Bitmap imagebm = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                                    binding.ivTouxiang.setImageBitmap(imagebm);
                                } else {
                                    // 可以设置默认头像图片
                                    Bitmap defaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.img);
                                    binding.ivTouxiang.setImageBitmap(defaultImage);
                                }
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            // 如果已经查询过头像，加载缓存的头像数据
            String cachedImageData = spf.getString("cached_image_" + username, null);
            if (cachedImageData != null) {
                byte[] cachedImageBytes = Base64.decode(cachedImageData, Base64.DEFAULT);
                Bitmap cachedImage = BitmapFactory.decodeByteArray(cachedImageBytes, 0, cachedImageBytes.length);
                binding.ivTouxiang.setImageBitmap(cachedImage);
            }
        }

        //退出登陆
        binding.loginoutbutton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),LoginActivity.class);
                startActivity(intent);
                System.exit(0);//将活动销毁，只剩下一个登陆界面的活动
            }
        });

        binding.button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setData(Uri.parse("https://www.guet.edu.cn/"));//Url 要打开的网址
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent); //启动浏览器
            }
        });

        binding.button2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("SHAREPICTURE软件信息"); // 设置对话框标题
                String text = "基于Android的图片分享软件。用户可将图片分享至平台以供其他用户浏览，用户可对喜欢的图片进行点赞保存分享等操作。";
                builder.setMessage(text); // 设置对话框消息


                // 设置对话框按钮
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 关闭对话框
                    }
                });

                // 创建并显示对话框
                AlertDialog dialog = builder.create();
                dialog.show();

                // 添加动画效果
                Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                dialog.getWindow().getDecorView().startAnimation(fadeIn);
            }
        });

        binding.button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 创建一个 AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("版本信息"); // 设置对话框标题

                // 获取版本号
                try {
                    PackageManager packageManager = getContext().getPackageManager();
                    PackageInfo packInfo = packageManager.getPackageInfo(getContext().getPackageName(), 0);
                    String version = packInfo.versionName;
                    builder.setMessage("当前应用版本：" + version); // 设置对话框消息
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                // 设置对话框按钮
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 关闭对话框
                    }
                });

                // 创建并显示对话框
                AlertDialog dialog = builder.create();
                dialog.show();

                // 添加动画效果
                Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                dialog.getWindow().getDecorView().startAnimation(fadeIn);
            }
        });

        binding.button4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setData(Uri.parse("https://github.com/DrinkwangzaidiTaurus/PhotoSharingDbhll-"));//Url 要打开的网址
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent); //启动浏览器
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}