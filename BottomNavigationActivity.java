package com.example.sharepictures;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.sharepictures.databinding.ActivityBottomNavigationBinding;
import com.example.sharepictures.ui.personal.NotificationsFragment;

public class BottomNavigationActivity extends AppCompatActivity {

    private static final long DELAY_MILLIS = 2000; // 2 秒
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.success_activity);

        // 延时两秒后跳转到下一个界面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 继续执行后续内容
                continueWithNavigation();
            }
        }, DELAY_MILLIS);
    }

    private void continueWithNavigation() {

        // 绑定布局文件
        ActivityBottomNavigationBinding binding = ActivityBottomNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 配置 AppBar 的导航项集合
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_upload, R.id.navigation_personal)
                .build();

        // 获取导航控制器
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_navigation);

        // 将 ActionBar 与导航控制器关联
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 将底部导航视图与导航控制器关联
        NavigationUI.setupWithNavController(binding.navView, navController);//主页


    }


}