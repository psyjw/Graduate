package com.next.schoolmemory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.next.schoolmemory.Adapter.RecyclerViewCardAdapter;
import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Model.NoteInfoModel;

import java.util.ArrayList;
import java.util.List;

public class ClassifyResultActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private String search_str;
    private RecyclerView recyclerView;
    public NoteInfoModel noteInfoModel;
    public List<NoteBean> resultList;
    private int myType=0;
    public final static int PEOPLE_TYPE = 1;
    public final static int LABEL_TYPE=2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//此行用于去除默认的顶部栏
        setContentView(R.layout.activity_classify_result);

        //提取其他activity传来的信息，一个是type，表明搜索的是人物还是标签，另一个是搜索的String
        //get the information from other activity
        myType = getIntent().getExtras().getInt("type");
        search_str = getIntent().getExtras().getString("search");

        recyclerView=(RecyclerView)this.findViewById(R.id.recycler_list);

        initView();//初始化界面
    }

    private void initView(){
        noteInfoModel = new NoteInfoModel(this);//初始化数据库连接 // initialize the connection with the database
        initToolbar();//初始化toolbar // initialize toolbar
        refreshAndShow();//刷新 //refresh
    }

    public void refreshAndShow() {
        //根据type决定是搜索人物还是搜索标签
        //depends on type, the system will search people or tag
        if(myType==PEOPLE_TYPE)
            resultList = noteInfoModel.searchByPeople(search_str);
        else if(myType==LABEL_TYPE)
            resultList = noteInfoModel.searchByLabel(search_str);
        //将结果显示出来
        //show the results
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this.getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        RecyclerViewCardAdapter recyclerViewCardAdapter=new RecyclerViewCardAdapter((ArrayList<NoteBean>) resultList,this,this, RecyclerViewCardAdapter.CLASSIFY_TYPE);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewCardAdapter);
    }

    private void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.classified_toolar);
        toolbar.setTitle(search_str);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.back);//此行必须放在setSupportActionBar后执行
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {//对后退键的监听。
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);//对后退键的监听 // add listner to goback button
                ClassifyResultActivity.this.finish();
            }
        });
    }
}
