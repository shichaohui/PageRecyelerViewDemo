package com.example.sch.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private PageRecyclerView mRecyclerView = null;
    private List<String> dataList = null;
    private PageRecyclerView.PageAdapter myAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initData();

        mRecyclerView = (PageRecyclerView) findViewById(R.id.cusom_swipe_view);
        // 设置指示器
        mRecyclerView.setIndicator((PageIndicatorView) findViewById(R.id.indicator));
        // 设置行数和列数
        mRecyclerView.setPageSize(3, 3);
        // 设置页间距
        mRecyclerView.setPageMargin(30);
        // 设置数据
        mRecyclerView.setAdapter(myAdapter = mRecyclerView.new PageAdapter(dataList, new PageRecyclerView.CallBack() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item, parent, false);
                return new MyHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((MyHolder) holder).tv.setText(dataList.get(position));
            }

            @Override
            public void onItemClickListener(View view, int position) {
                Toast.makeText(MainActivity.this, "点击："
                        + dataList.get(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClickListener(View view, int position) {
                Toast.makeText(MainActivity.this, "删除："
                        + dataList.get(position), Toast.LENGTH_SHORT).show();
                myAdapter.remove(position);
            }
        }));

    }

    private void initData() {
        dataList = new ArrayList<>();
        for (int i = 0; i < 47; i++) {
            dataList.add(String.valueOf(i));
        }
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        public TextView tv = null;

        public MyHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.text);
        }
    }

}