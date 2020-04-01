package com.next.schoolmemory;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.next.schoolmemory.Adapter.TagAdapter;
import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Model.NoteInfoModel;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.next.schoolmemory.ClassifyResultActivity.LABEL_TYPE;
import static com.next.schoolmemory.ClassifyResultActivity.PEOPLE_TYPE;

public class LabelClassifyFirstFragment extends Fragment {
    private ListView listView;
    private NoteInfoModel noteInfoModel;
    private String labelTotal="";
    private String[] label;
    private ArrayAdapter<String> adapter;
    private EditText search_input;
    List<NoteBean> list;
    List<String> tagList=new ArrayList<>();

    private RecyclerView tagRecyclerView;
    private static final int MAX = 9;
    private TagAdapter tagAdapter;

    public final int CLASSIFY_ACTIVITY=5000;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_label_classify_first, container, false);
        listView = (ListView)view.findViewById(R.id.search_listview);
        search_input = (EditText) view.findViewById(R.id.label_search_edittext);
        tagRecyclerView = (RecyclerView) view.findViewById(R.id.tag_rv);

        collectLabel();//获取数据库中所有的标签信息 get label info from database
        initInputArea();//初始化搜索输入区域 Initialize search textfield
        showTags();//显示标签 show label

        return view;
    }

    //显示标签
    private void showTags() {
        GridLayoutManager layoutManage = new GridLayoutManager(this.getActivity(), 2);
        layoutManage.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (tagList.get(position).length()>MAX)//如果一个项目中的字符长度超过MAX，则它独占一行，也就是一组两个占两行
                    return 2;
                return 1;//否则占一行，也就是一行一组，一行两个
            }
        });
        //将搜集到的label显示出来 show all labels
        tagRecyclerView.setLayoutManager(layoutManage);
        tagAdapter = new TagAdapter(tagList,LabelClassifyFirstFragment.this, LABEL_TYPE);
        tagRecyclerView.setAdapter(tagAdapter);
    }

    private void collectLabel(){
        noteInfoModel = new NoteInfoModel(this.getActivity());//建立数据库连接 connect to database
        list = noteInfoModel.QueryAllNotefromData();
        for(int i=0;i<list.size();i++){
            String[] str = list.get(i).getLabels().split(";");
            for(int j=0;j<str.length;j++) {
                if ((!str[j].equals("")) && (!labelTotal.contains(str[j] + ";")))//防止出现标签内容为空字符串 handle case of empty string
                    labelTotal = labelTotal + str[j] + ";";//将不重复的标签存到一个String中，用分号隔开
            }
        }
        label = labelTotal.split(";");//通过分号切割提取 split by ;
        for(int i=0;i<label.length;i++) {
            if(!label[i].equals(""))
                tagList.add(label[i]);
        }
    }

    //初始化输入区域 // Initialize input textfield
    private void initInputArea(){
        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //每当输入区域的内容变化时，刷新搜索列表，当不为空时搜索列表覆盖tag列表
                String str = search_input.getText().toString();
                List<String> tmp = new ArrayList<>();
                String newDatas[];
                for(int i=0;i< label.length;i++){
                    if(label[i].contains(str))
                        tmp.add(label[i]);
                }
                newDatas = new String[tmp.size()];
                for(int i=0;i<tmp.size();i++){
                    newDatas[i]=tmp.get(i);
                }
                //将最新的搜索结果显示到listview中
                adapter=new ArrayAdapter<String>(LabelClassifyFirstFragment.this.getActivity(),android.R.layout.simple_list_item_1,newDatas);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        startClassifyResultActivity(label[position]);
                    }
                });
                if(!search_input.getText().toString().equals("")){
                    tagRecyclerView.setVisibility(View.GONE);
                }
                else
                    tagRecyclerView.setVisibility(View.VISIBLE);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    //跳转到分类结果活动 // jump out
    private void startClassifyResultActivity(String search_str){
        Intent intent = new Intent(LabelClassifyFirstFragment.this.getActivity(), ClassifyResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("search", search_str);
        bundle.putInt("type", LABEL_TYPE);
        intent.putExtras(bundle);
        startActivityForResult(intent, CLASSIFY_ACTIVITY);
    }

    //当从其他活动返回时，进行刷新列表
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==CLASSIFY_ACTIVITY) {
                //刷新数据 //refresh data
                collectLabel();
                showTags();
            }
        }
    }
}

