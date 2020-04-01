package com.next.schoolmemory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.next.schoolmemory.Adapter.RecyclerViewCardAdapter;
import com.next.schoolmemory.Bean.Means;
import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Presenter.Presenter_search;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class SearchFragment extends Fragment {
    private Toolbar toolbar;
    private AppCompatActivity myActivity;
    public Presenter_search presenter;
    private Button searchButton;
    private MaterialEditText searchInfo;
    private List<NoteBean> resultList;
    private TextView advancedSearchButton;
    private RecyclerView recyclerView;

    private static final int ACTIVITY_NEWFILE = 1000;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        toolbar = (Toolbar) view.findViewById (R.id.search_toolbar);
        //listView = (ListView) view.findViewById(R.id.search_listview);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_list);
        searchButton = (Button) view.findViewById(R.id.search_button);
        searchInfo = (MaterialEditText) view.findViewById(R.id.search_edittext);
        advancedSearchButton=(TextView) view.findViewById(R.id.advanced_search_entrance);
        return view;
    }
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        myActivity = (AppCompatActivity) getActivity();
        toolbar.setTitle("Search");
        myActivity.setSupportActionBar(toolbar);
        advancedSearchButton.setOnClickListener(new View.OnClickListener() {//对“高级”按钮的监听，如果用户点击“高级”则跳转到高级搜索
            @Override
            public void onClick(View view) {
                startAdvancedSearchActivity();//跳转到高级搜索 jump to advanced search
            }
        });

        presenter = new Presenter_search(this);//初始化presenter中间类 Initialization

        searchButton.setOnClickListener(new View.OnClickListener() {//对“搜索”按钮的监听
            @Override
            public void onClick(View v) {
                refreshList();//根据输入内容进行搜索，更新列表
            }
        });
    }

    //跳转到高级搜索 advanced search
    public void startAdvancedSearchActivity(){
        Intent intent=new Intent(getActivity(), AdvancedSearchActivity.class);
        intent.putExtras(new Bundle());
        startActivity(intent);
    }
    //刷新显示列表 show lists
    public void refreshList(){
        resultList = presenter.searchStringInDatabase(searchInfo.getText().toString());//搜索得到的数据 get searched data
        //将结果显示出来 show data
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getActivity().getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        RecyclerViewCardAdapter recyclerViewCardAdapter=new RecyclerViewCardAdapter((ArrayList<NoteBean>) resultList,getActivity(),SearchFragment.this, RecyclerViewCardAdapter.SEARCH_TYPE);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewCardAdapter);
    }

    //从文件编辑返回时，清空搜索结果列表和输入区域
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK){
            if(requestCode == ACTIVITY_NEWFILE){
                //清空列表 empty list
                searchInfo.setText("%￥");
                refreshList();//搜索符号使得搜索结果为空，从而清空显示清单 refresh list
                searchInfo.setText("");//清空输入区域 make textfield empty
            }
        }
    }
}
