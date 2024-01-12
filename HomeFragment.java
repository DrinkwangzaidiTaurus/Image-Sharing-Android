package com.example.sharepictures.ui.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.sharepictures.Connect;
import com.example.sharepictures.ImagesDetails;
import com.example.sharepictures.R;
import com.example.sharepictures.databinding.FragmentHomeBinding;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;// 用于绑定视图的对象
    private ResultSet resultSet;// 用于存储数据库查询结果的对象

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // 初始化绑定视图的对象
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot(); // 获取根视图

        // 创建存储数据项的集合
        Map<String, Object> item;
        final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        // 在子线程中执行数据库查询
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> item;
                final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

                // 获取 FragmentActivity 的引用
                final FragmentActivity activity = getActivity();

                if (activity != null) { // 检查 FragmentActivity 是否为 null
                    try {
                        try {
                            resultSet = Connect.queryAllPictures();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        if (resultSet != null) {
                            while (resultSet.next()) {
                                item = new HashMap<String, Object>();

                                // 获取图片数据
                                byte[] imageData = resultSet.getBytes("pictures");
                                if (imageData != null) {
                                    Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                                    item.put("pictures", imageBitmap);
                                } else {
                                    Bitmap defaultImageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_baseline_photo_camera_24);
                                    item.put("pictures", defaultImageBitmap);
                                }

                                // 获取图片ID（idnum）和作者
                              //  String idn = resultSet.getString("p.idnum");
                                item.put("idnum", resultSet.getString("p.idnum"));
                                item.put("uploader", resultSet.getString("p.uploader"));

                                item.put("like_num", resultSet.getString("l.likes_count"));

                                data.add(item);
                            }
                        }

                        // 在子线程中更新UI，使用runOnUiThread方法
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (binding != null && binding.listView != null) {
                                    SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), data, R.layout.activity_my_pictufabu,
                                            new String[]{"pictures", "idnum", "uploader","like_num"},
                                            new int[]{R.id.item_image, R.id.idnum, R.id.uploader,R.id.like_num});
                                    simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                                        @Override
                                        public boolean setViewValue(View view, Object data, String textRepresentation) {
                                            if (view instanceof ImageView && data instanceof Bitmap) {
                                                ImageView iv = (ImageView) view;
                                                iv.setImageBitmap((Bitmap) data);
                                                return true;
                                            } else if (view instanceof TextView) {
                                                TextView tv = (TextView) view;
                                                if (view.getId() == R.id.like_num) {
                                                    //当结果为null时候就显示0
                                                    String likeNumText = (data == null) ? "0" : String.valueOf(data);
                                                    tv.setText(likeNumText);
                                                } else {
                                                    tv.setText(textRepresentation);
                                                }
                                                return true;
                                            }
                                            return false;
                                        }
                                    });
                                    binding.listView.setAdapter(simpleAdapter);
                                } else {
                                    Log.e("HomeFragment", "Binding or listView is null");
                                }
                            }
                        });

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        // 设置列表项点击事件的监听器
        binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (resultSet != null) {
                    try {
                        // 移动 resultSet 到选中项的位置
                        resultSet.absolute(position + 1);

                        // 获取选中项的 idnum 值
                        String idnum = resultSet.getString("idnum");
                        String ccc=idnum;

                        // 创建一个 Intent 用于启动 ImagesDetails 活动
                        Intent intent = new Intent(getActivity(), ImagesDetails.class);

                        // 将 idnum 数据传递给 ImagesDetails 活动
                        intent.putExtra("idnum", idnum);

                        // 启动 ImagesDetails 活动
                        startActivity(intent);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return root;//返回根视图并用于显示整个布局
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
