package com.example.zking.example;
/**
 * Created by Administrator on 2017/7/29 0029.
 */
public interface OnRefreshListener {
    public void refreshBefore(CustomListView civ);//刷新前
    public void refreshAfter(CustomListView civ);//刷新后
    public void refreshStart(CustomListView civ);//开始刷新
}
