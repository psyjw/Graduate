package com.next.schoolmemory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.next.schoolmemory.Adapter.RecyclerViewCardAdapter;
import com.next.schoolmemory.Bean.Means;
import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Presenter.Prestener_list;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FileFragment extends Fragment {
    private static final int ACTIVITY_NEWFILE = 1000;
    private Toolbar toolbar;
    private AppCompatActivity myActivity;
    public Prestener_list presenter;
    private RecyclerView recyclerView;
    private RelativeLayout relativeLayout;

    public final static int PEOPLE_CLASSIFY_ACTIVITY=3000;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//设置参数，使得右上方的几个配件得以显示//setup parameter
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        toolbar = (Toolbar) view.findViewById (R.id.toolbar_files);
        relativeLayout=(RelativeLayout)view.findViewById(R.id.list_empty);
        recyclerView=(RecyclerView)view.findViewById(R.id.recycler_list);
        presenter=new Prestener_list(this);

        //以下代码原本在oncreated函数中，但问题仍未解决
        myActivity = (AppCompatActivity) getActivity();

        initToolBar();
        return view;
    }

    private void initToolBar() {
        toolbar.setTitle("DiaryCraft");
        //让toolbar取代原本默认的actionbar
        myActivity.setSupportActionBar(toolbar);//激活 // active
        toolbar.inflateMenu(R.menu.files_menu);//一定要添加这行！！！否则最开始不会显示右上角的菜单，在切换后才会显示。//must leave this line
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.add://进入“新建”任务
                        startEditActivity(null);
                        return true;
                    case R.id.people_classify://进入“分类查看”任务
                        startPeopleClassifyActivity();
                        return true;
                }
                return false;
            }
        });
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        //通过中间类，读取数据库信息并显示
        presenter.readNotefromDatatoList();//从数据库读取信息并显示//read info from database
    }

    //跳转到编辑（新建）活动// jump out to edit page
    private void startEditActivity(NoteBean noteBean){
        Intent addintent = new Intent(getActivity(), EditActivity.class);
        Bundle bundle = new Bundle();
        if(noteBean!=null) {
            bundle.putSerializable("noteinfo", Means.changefromNotebean(noteBean));
        }
        addintent.putExtra("date", bundle);
        startActivityForResult(addintent, ACTIVITY_NEWFILE);//需要在新建后调用刷新列表函数。所以采用ForResult的方法开始活动
    }

    //用于添加menu，这个fragment的menu在右上角，点击按钮弹出
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.files_menu, menu);//和对应的menu布局文件联系起来
    }

    //获取Context// get context
    public Context getListActivityConent() {
        return getActivity();
    }

    public void readAllNotefromData(List<NoteBean> noteBeanList) {//读取数据库内的文件
        setMainBackgroundIcon(noteBeanList.size());//判断目前是否有数据，如果没有则显示底下的“空”视图
        //将读取到的内容显示出来//read content
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getActivity().getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        RecyclerViewCardAdapter recyclerViewCardAdapter=new RecyclerViewCardAdapter((ArrayList<NoteBean>) noteBeanList,getActivity(),this, RecyclerViewCardAdapter.FILE_TYPE);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewCardAdapter);
    }


    public void setMainBackgroundIcon(int size) {
        if (size==0){
            relativeLayout.setVisibility(View.VISIBLE);
        }else {
            relativeLayout.setVisibility(View.GONE);//如果数据库没有内容，则让整个relativeLayout消失，让后面的东西（数据为空的背景）显示出来
        }
    }

    //刷新从数据库读取的内容//refresh
    public void refreshList(){//更新列表，在新建文件后使用
        presenter.readNotefromDatatoList();
    }

    //根据其他activity返回的结果进行处理
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK){//主要目的是刷新列表
            if(requestCode == ACTIVITY_NEWFILE || requestCode == PEOPLE_CLASSIFY_ACTIVITY){
                refreshList();
            }
        }
    }

    //进入“分类查看”任务//checkout task
    public void startPeopleClassifyActivity(){
        Intent intent = new Intent();
        intent.setClass(getActivity(), ClassifyActivity.class);
        intent.putExtras(new Bundle());
        startActivityForResult(intent,PEOPLE_CLASSIFY_ACTIVITY);
    }

}
