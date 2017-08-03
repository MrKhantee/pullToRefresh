package com.example.zking.example;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    CustomListView clv;
    List<Object> list = new ArrayList<Object>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clv=(CustomListView)findViewById(R.id.customLV);
        initDataSource();
        clv.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list));
        //设置刷新监听事件
        clv.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void refreshBefore(CustomListView civ) {
            }
            @Override
            public void refreshAfter(CustomListView civ) {
            }
            @Override
            public void refreshStart(final CustomListView civ) {
                //这里new一个线程，用于模仿网络刷新请求时长
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            this.sleep(4000);
                            civ.myHandler.sendEmptyMessage(CustomListView.REFRESH_END);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });


    }
    //初始数据
    public void initDataSource() {
        for (int i = 0; i <30; i++) {
            list.add("a" + i);
        }
    }
}
