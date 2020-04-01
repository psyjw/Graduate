package com.next.schoolmemory;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;

import com.next.schoolmemory.Adapter.RecyclerViewCardAdapter;
import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Model.NoteInfoModel;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.gujun.android.taggroup.TagGroup;

public class AdvancedSearchActivity extends AppCompatActivity {
    private Toolbar toolbar;
    MaterialEditText keywordInput, peopleInput,labelinput;
    TagGroup DateRange;
    List<String> dateTags = new ArrayList<>();
    Button searchButton;
    List<NoteBean> searchResult;
    private RecyclerView recyclerView;
    public NoteInfoModel noteInfoModel;//和数据库交互，不通过presenter类 // interact with database

    private static final int ACTIVITY_NEWFILE = 1000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//此行用于去除默认的顶部栏 // wipe of the default toolbar
        setContentView(R.layout.activity_advanced_search);
        initView();//初始化界面 // initialize
    }
    private void initView() {
        recyclerView = (RecyclerView) this.findViewById(R.id.search_listview);
        initToolbar();//initialize toolbar
        initInputArea();//initialize each input area.
        initDateTags();//初始化日期选择区域 //initialize the date choosing function
        initSearchButton();//初始化搜索按钮 //initialize search button
        initLinkDatabase();//初始化对数据库的连接 //initialize the connection with database
    }

    //初始化对数据库的连接
    //initialize the connection with database
    private void initLinkDatabase() {
        noteInfoModel = new NoteInfoModel(this);
    }

    //对各个输入区域的初始化
    //initialize each input area.
    private void initInputArea() {
        keywordInput = (MaterialEditText) this.findViewById(R.id.advanced_search_keyword_input);
        peopleInput = (MaterialEditText) this.findViewById(R.id.advanced_search_people_input);
        labelinput = (MaterialEditText)this.findViewById((R.id.advanced_search_label_input));
    }

    //对搜索按钮的初始化
    //initialize search button
    private void initSearchButton() {
        searchButton = (Button)this.findViewById(R.id.advanced_search_start);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshAndShowResult();//根据当前各个条件进行搜索，刷新显示结果 // search by the current parameter, and refresh the result showing part
            }
        });
    }

    public Context getContext(){
        return this;
    }

    private void initDateTags() {
        DateRange = (TagGroup)this.findViewById(R.id.dateTags);
        dateTags.add("start:20000101");//起止日期都初始化为2000年1月1日 //the start and end date were initialize as 2000/1/1
        dateTags.add("end:20000101");
        DateRange.setTags(dateTags);

        DateRange.setOnTagClickListener(new TagGroup.OnTagClickListener() {
            @Override
            public void onTagClick(String tag) {
                if(tag.contains("start"))
                    DateSelectAlert(0);//show dialog for user to choose date.
                else DateSelectAlert(1);
            }
        });
    }

    //用户选择完后更新tag，显示用户选择的日期
    //after the user choose the date, refresh the date tag
    private void updateDateTags(int i,String str) {
        if(i==0)
            str="start:"+str;
        else
            str = "end:"+str;
        dateTags.set(i,str);
        DateRange.setTags(dateTags);
    }

    //日期选择dialog
    //date choosing dialog
    private void DateSelectAlert(final int type){
        final Calendar calendar=Calendar.getInstance();
        new DatePickerDialog(this,0, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                int month=i1+1;//月份要加1 // month needs to plus 1
                String date_tmp = new String();
                date_tmp = date_tmp+""+i;
                if(month<10) date_tmp+="0";
                date_tmp+=month;
                if(i2<10) date_tmp+="0";
                date_tmp+=i2;
                updateDateTags(type,date_tmp);
            }
        },calendar.get(Calendar.YEAR)
                ,calendar.get(Calendar.MONTH)
                ,calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    //初始化toolbar
    //initialize toolbar
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.advanced_search_toolbar);
        toolbar.setTitle("Advanced Search");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.back);//此行必须放在setSupportActionBar后执行 // this code must executed after setSupportActionBar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {//对后退键的监听。
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);
                AdvancedSearchActivity.this.finish();//对后退键的监听，点击activity结束 // add listener to the goback button, when press it , the activity ends.
            }
        });
    }

    //根据当前条件进行搜索，更新显示内容
    //search by current parameter, refresh the result showing list.
    public void refreshAndShowResult(){
        String keyword = keywordInput.getText().toString();//get the content in each area
        String people = peopleInput.getText().toString();
        String label = labelinput.getText().toString();
        String[] start_tmp = dateTags.get(0).split(":");
        String start = start_tmp[1];//获取开始日期string
        String[] end_tmp = dateTags.get(1).split(":");
        String end = end_tmp[1];//获取结束日期string

        //根据以上条件搜索并显示结果
        //search with the parameters above
        searchResult = noteInfoModel.advancedSearch(keyword, people,label, start, end);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this.getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        RecyclerViewCardAdapter recyclerViewCardAdapter=new RecyclerViewCardAdapter((ArrayList<NoteBean>) searchResult,this,AdvancedSearchActivity.this, RecyclerViewCardAdapter.ADVANCED_TYPE);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewCardAdapter);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK){
            if(requestCode == ACTIVITY_NEWFILE){//从编辑界面返回后清空列表，清空输入区域 //when return from editactivity, clear the input area
                keywordInput.setText("%$");
                refreshAndShowResult();//清空结果显示区域 // clear the result showing area
                keywordInput.setText("");
                peopleInput.setText("");
                labelinput.setText("");
            }
        }
    }
}
