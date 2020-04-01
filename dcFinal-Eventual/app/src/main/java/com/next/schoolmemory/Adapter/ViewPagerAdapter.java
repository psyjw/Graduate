package com.next.schoolmemory.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

//用于主页切换的adapter
//this adapter is for main page switching.
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> list;

    //设置标签页
    public void setList(List<Fragment> list) {
        this.list = list;
        notifyDataSetChanged();
    }
    //initialization
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    //获取特定位置的内容
    //get the content of a specific location
    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }
    //获取个数
    //get the amount
    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }
}
