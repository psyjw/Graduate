package com.next.schoolmemory;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;

public class ClassifyActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout = null;
    private ViewPager viewPager;

    private String[] mTabTitles = new String[2];

    PeopleClassifyFirstFragment peopleFragmentFirst;
    LabelClassifyFirstFragment labelFragmentFirst;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//此行用于去除默认的顶部栏 // disappear the default toolbar
        setContentView(R.layout.activity_people_classify);

        toolbar = (Toolbar) this.findViewById(R.id.classify_toolar);
        tabLayout = (TabLayout) this.findViewById(R.id.tablayout);
        viewPager = (ViewPager) this.findViewById(R.id.tab_viewpager);

        initView();
    }

    protected void initView(){
        initToolbar();
        mTabTitles[0] = "People";//tab的两个标签的标题
        mTabTitles[1] = "Tag";//
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        //设置tablayout距离上下左右的距离
        //tab_title.setPadding(20,20,20,20);
        PagerAdapter pagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        //将ViewPager和TabLayout绑定
        // bind ViewPager and TabLayout
        tabLayout.setupWithViewPager(viewPager);
        peopleFragmentFirst = new PeopleClassifyFirstFragment();//这两个是加入到tablayout中的两个Fragment //these are the two fragment add to tablayout
        labelFragmentFirst = new LabelClassifyFirstFragment();
    }

    protected void initToolbar(){
        toolbar.setTitle("People and tag");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.back);//此行必须放在setSupportActionBar后执行
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {//对后退键的监听。
            @Override
            public void onClick(View view) {
                ClassifyActivity.this.finish();
            }
        });
    }

    final class MyViewPagerAdapter extends FragmentPagerAdapter {
        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            //return mFragmentArrays[position];
            if(position==0)//当用户点击tab上的两个选项时，选择跳转到的fragment //when user press the tab, jump to related fragment
                return peopleFragmentFirst;
            else
                return labelFragmentFirst;
        }
        @Override
        public int getCount() {//返回tab的个数
            //return mFragmentArrays.length;
            return 2;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }
    }
}
