package com.example.sharepictures;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class SignupActivity extends AppCompatActivity {

    private Boolean bPwdSwitch1 = false;//判断第一个输入密码是否可见,这里默认密码不可见
    private Boolean bPwdSwitch2 = false;//判断第二个确认密码是否可见,这里默认密码不可见
    private EditText etPwd1;//输入密码
    private EditText etPwd2;//输入确认密码
    private EditText etAccount;//输入用户的账户名
    private Button btSignup;// 注册按钮
    private ImageButton chooseImage;//创建一个用户头像
    private byte[] image;//头像的数组
    private Bitmap bm;//选择头像的照片

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.signupactivity);//设置当前界面的样式

        // 初始化各个UI元素
        final ImageView ivPwdSwitch1 = findViewById(R.id.imageView1);//是否切换明文状态的格式
        final ImageView ivPwdSwitch2 = findViewById(R.id.imageView2);//是否切换明文状态的格式
        etAccount = findViewById(R.id.NumberSignup);//用户名输入框
        etPwd1=findViewById(R.id.Password1); // 第一个密码输入框
        etPwd2=findViewById(R.id.Password2); // 第二个确认密码输入框
        btSignup=findViewById(R.id.signupbutton);//注册账户的形式
        chooseImage =findViewById(R.id.chooseimage);//选择头像的形式


        //这是第一个控制密码是否可见
        ivPwdSwitch1.setOnClickListener(new View.OnClickListener() {
            //ivPwdSwitch对象调用方法，括号里面是new一个接口当参数传入
            @Override
            public void onClick(View view) {
                bPwdSwitch1 = !bPwdSwitch1;
                if (bPwdSwitch1) {
                    ivPwdSwitch1.setImageResource(
                            R.drawable.ic_baseline_visibility_24);
                    etPwd1.setInputType(
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);//输入一个对用户可见的密码
                } else {
                    ivPwdSwitch1.setImageResource(
                            R.drawable.ic_baseline_visibility_off_24);
                    etPwd1.setInputType(
                            InputType.TYPE_TEXT_VARIATION_PASSWORD |
                                    InputType.TYPE_CLASS_TEXT);//输入一个密码或者是一个普通文本
                    etPwd1.setTypeface(Typeface.DEFAULT);
                }

            }

        });

        //这是第二个控制密码是否可见
//         添加一个监听事件
        ivPwdSwitch2.setOnClickListener(new View.OnClickListener() {
            //ivPwdSwitch对象调用方法，括号里面是new一个接口当参数传入
            @Override
            public void onClick(View view) {
                bPwdSwitch2 = !bPwdSwitch2;
                if (bPwdSwitch2) {
                    ivPwdSwitch2.setImageResource(
                            R.drawable.ic_baseline_visibility_24);
                    etPwd2.setInputType(
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);//输入一个对用户可见的密码
                } else {
                    ivPwdSwitch2.setImageResource(
                            R.drawable.ic_baseline_visibility_off_24);
                    etPwd2.setInputType(
                            InputType.TYPE_TEXT_VARIATION_PASSWORD |
                                    InputType.TYPE_CLASS_TEXT);//输入一个密码或者是一个普通文本
                    etPwd2.setTypeface(Typeface.DEFAULT);
                }
            }

        });

        // 点击选择头像的事件
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//添加点击事件
                //检测是否进行了授权
                if (ContextCompat.checkSelfPermission(SignupActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(SignupActivity.this, new
                            String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    //打开系统相册
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 1);//请求标识为1
//                    startActivityForResult用于启动一个新的Activity并且期望在完成这个Activity后能够获取一个结果。
//                    它允许一个Activity向另一个Activity发送请求，
//                    并且在第二个Activity处理完请求后可以返回结果给第一个Activity。
                }

            }
        });

        // 点击注册按钮的事件
        btSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = etAccount.getText().toString().trim();
                String password1 = etPwd1.getText().toString().trim();
                String password2 = etPwd2.getText().toString().trim();

                if (image == null) {
                    Toast.makeText(getApplicationContext(), "请上传头像", Toast.LENGTH_SHORT).show();
                    return;
                }else if (account.equals("")) {
                    Toast.makeText(getApplicationContext(), "账号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!account.matches("^[a-zA-Z]+$")) { // 检查用户名是否纯英文
                    Toast.makeText(getApplicationContext(), "账号必须为纯英文", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(password1)) {
                    Toast.makeText(getApplicationContext(), "密码不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!password1.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$")) {
                    // 检查密码是否由数字和字母构成，且长度大于等于6位
                    Toast.makeText(getApplicationContext(), "密码必须包含数字和字母，且长度不低于6位", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(password2)) {
                    Toast.makeText(getApplicationContext(), "请确认密码！", Toast.LENGTH_SHORT).show();
                    return ;
                } else if (!password1.equals(password2)) {
                    Toast.makeText(getApplicationContext(), "密码不一致，请重新确认密码", Toast.LENGTH_LONG).show();
                    return ;
                }else if (CheckIsDataAlreadyInDBorNot(account)) {
                    Toast.makeText(getApplicationContext(), "该账号已被注册，注册失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                    register(account, password1, password2, image);
                    Toast.makeText(getApplicationContext(), "账号注册中，将在2分钟后生效", Toast.LENGTH_SHORT).show();

            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if(message.what==1){
                Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }else if(message.what == 2){
                Toast.makeText(getApplicationContext(), "连接不成功", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    });

    //注册使用的方法
    public void register(String username, String password1, String password2, byte[] image) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Connection connection = Connect.getConnection("001for");
                        Message message = new Message();
                        if (connection != null) {
                            Connect.insertUser(username, password1, image);
                            message.what = 1;
                        } else {
                            message.what = 2;
                        }
                        handler.sendMessage(message);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

        }


    //检验用户名是否已存在
    public boolean CheckIsDataAlreadyInDBorNot(String username) {
        try {
            int result = Connect.queryUser(username);
            return result == 1; // 返回 true 如果查询结果为1，表示用户名已存在
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //requestCode是用来标识请求的来源(这里是图片点击事件，标识为1）， resultCode是用来标识返回的数据来自哪一个activity
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();//选择照片
            String[] filePathColumns = {MediaStore.Images.Media.DATA};//获取图片路径

            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();//正确指向第一个位置
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);// 获取图片的路径
            showImage(imagePath);//进行注册的方法
            c.close();
        }

    }


    //显示头像
    private void showImage(String imagePath) {
        bm = BitmapFactory.decodeFile(imagePath);
        image = compressImage(bm); // 压缩图片
        chooseImage.setImageBitmap(bm);// 将图片显示在选择头像的按钮上
    }


    //压缩头像
    private byte[] compressImage(Bitmap image) {
        //临时存储压缩数据，方便在内存中处理
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        image.compress(Bitmap.CompressFormat.JPEG, quality, baos);

        // 压缩图片大小直到小于100KB
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset();
            quality -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }

        // 返回压缩后的字节数组
        return baos.toByteArray();
    }


}
