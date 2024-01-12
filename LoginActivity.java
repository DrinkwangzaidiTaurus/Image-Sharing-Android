package com.example.sharepictures;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Boolean bPwdSwitch = false;//判断密码是否可见,这里一开始默认密码不可见
    private EditText etPwd;//输入密码
    private EditText etAccount;//输入用户的账户名
    private CheckBox cbRememberPwd;//是否记住密码
    private static String currentUserId;// 静态变量用于存储当前登录用户的ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginactivity);//设置当前界面的布局

        // 初始化各种控件
        final ImageView ivPwdSwitch = findViewById(R.id.iv_pwd_switch);//是否切换明文状态的格式
        etPwd = findViewById(R.id.et_pwd);//密码的形式
        etAccount = findViewById(R.id.et_account);//用户的账户名
        cbRememberPwd = findViewById(R.id.cb_remember_pwd);//记住密码的形式
        Button btLogin = findViewById(R.id.bt_login);//登陆按钮的形式
        TextView btSignup= findViewById(R.id.bt_signup);//注册账户的形式

        // 为账号输入框添加文本变化监听器
        //TextWatcher 是一个接口，所以需要实现它的所有方法，即使不需要这些方法的实际功能
        etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 当账号输入框的文本发生变化时，检查是否为空，如果为空则清空密码输入框
                if (TextUtils.isEmpty(charSequence)) {
                    etPwd.setText(""); // 清空密码输入框
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //当前登陆活动设置一个监听事件
        btLogin.setOnClickListener(new View.OnClickListener() {

            //点击登陆按钮之后，实现页面的跳转
            @Override
            public void onClick(View v) {
                String userName=etAccount.getText().toString();// 获取输入的用户名
                String passWord=etPwd.getText().toString();// 获取输入的密码

                if (TextUtils.isEmpty(passWord)) {
                    // 密码为空，显示提示信息
                    Toast.makeText(LoginActivity.this, "密码为空，请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                final CountDownLatch latch = new CountDownLatch(1); // 创建一个CountDownLatch，初始值为1
                final int[] accountExists = {0};
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 查询用户是否存在
                            accountExists[0] = Connect.queryUser(userName);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        } finally {
                            latch.countDown(); // 查询操作完成后释放CountDownLatch
                        }
                    }
                }).start();

                try {
                    latch.await(); // 等待CountDownLatch计数变为0
                } catch (InterruptedException e) {
                    // 处理中断异常
                }

                if (accountExists[0] == 0) {
                    Toast.makeText(LoginActivity.this, "账号未注册，请确认账号是否输入正确或者注册账号", Toast.LENGTH_SHORT).show();
                    return;
                }

                    Toast.makeText(LoginActivity.this, "登录中", Toast.LENGTH_SHORT).show();
                Handler handler = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        if(message.what==1){
                            Bundle bundle = (Bundle) message.obj;
                            // 获取String数据
                            String userName = bundle.getString("account");
                            String passWord = bundle.getString("password");

                            // 登录成功，保存当前登录用户的ID
                            currentUserId = userName; // 这里假设用户名即为用户的ID

                            //登陆成功 将用户名和密码保存到 SharedPreferences
                            String spFileName = getResources()//获取当前活动的文件名
                                    .getString(R.string.shared_preferences_file_name);
                            String accountKey = getResources()//用户的账号
                                    .getString(R.string.login_account_name);
                            String passwordKey =  getResources()//登陆密码
                                    .getString(R.string.login_password);
                            SharedPreferences spFile = getSharedPreferences(
                                    spFileName,
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = spFile.edit();
                            //将账户名和密码成对写入安卓自带的SharedPreferences文件里面
                            editor.putString(accountKey, userName);
                            editor.putString(passwordKey, passWord);
                            editor.apply();

                            // 跳转到BottomNavigationActivity
                            Intent intent=new Intent(LoginActivity.this, BottomNavigationActivity.class);
                            startActivity(intent);
                            Toast.makeText(LoginActivity.this, "恭喜你登录成功", Toast.LENGTH_SHORT).show();

                            finish();//结束当前活动 释放资源
                        }else if(message.what == 2){

                            Toast.makeText(LoginActivity.this, "登录失败,账号密码不匹配，请检查后重新输入账号密码", Toast.LENGTH_SHORT).show();
                            etPwd.setText(""); // 清空密码框内容

                        }
                        return false;
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connect.verifyUser(userName,passWord,handler);
                    }
                }).start();



            }
        });

        //为注册按钮添加一个监听事件
        btSignup.setOnClickListener(new View.OnClickListener() {

            //点击注册按钮之后，跳转到注册界面
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
            }
        });

        // 从 SharedPreferences 加载账户信息（数据的持久化存储）
        String spFileName = getResources().getString(R.string.shared_preferences_file_name);//获取当前活动的文件名
        String accountKey = getResources().getString(R.string.login_account_name);//用户的账号
        String passwordKey =  getResources().getString(R.string.login_password);//登陆密码
        String rememberPasswordKey = getResources().getString(R.string.login_remember_password);//记住密码

        //在输入账户密码的时候，加载SharedPreferences中存储的用户账号信息
        SharedPreferences spFile = getSharedPreferences(spFileName, MODE_PRIVATE);
        String account = spFile.getString(accountKey,null);
        String password = spFile.getString(passwordKey,null);

        Boolean rememberPassword = spFile.getBoolean(rememberPasswordKey,false);//false是控制是否记住密码图标，这里表示不记住

        //登陆成功后自动保存密码
        //以下两个if是显示在对应的文本框，但是是否记住密码这个控件失效了
        if (account != null && !TextUtils.isEmpty(account)) {//读取到的账户名不能为空并且账户名框内不能为空
            etAccount.setText(account);
        }

        if (password != null && !TextUtils.isEmpty(password)) {//读取到的密码不能为空并且密码框内不能为空
            etPwd.setText(password);
            cbRememberPwd.setChecked(!rememberPassword);//这里显示记住密码
        }
        else{
            cbRememberPwd.setChecked(rememberPassword);//第一次登陆的时候，显示不记住密码
        }

        // 切换密码可见状态图标的点击事件 添加一个监听事件
        ivPwdSwitch.setOnClickListener(new View.OnClickListener() {
            //ivPwdSwitch对象调用方法，括号里面是new一个接口当参数传入
            @Override
            public void onClick(View view) {
                bPwdSwitch = !bPwdSwitch;
                if (bPwdSwitch) {
                    ivPwdSwitch.setImageResource(
                            R.drawable.ic_baseline_visibility_24);
                    etPwd.setInputType(
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);//输入一个对用户可见的密码
                } else {
                    ivPwdSwitch.setImageResource(
                            R.drawable.ic_baseline_visibility_off_24);
                    etPwd.setInputType(
                            InputType.TYPE_TEXT_VARIATION_PASSWORD |
                                    InputType.TYPE_CLASS_TEXT);//输入一个密码或者是一个普通文本
                    etPwd.setTypeface(Typeface.DEFAULT);
                }
            }
        });

    }

    //作为接口的实现类，不能直接省略
    @Override
    public void onClick(View view) {
    }
    // 静态方法用于获取当前登录用户的ID
    public static String getCurrentUserId() {
        return currentUserId;
    }

}
