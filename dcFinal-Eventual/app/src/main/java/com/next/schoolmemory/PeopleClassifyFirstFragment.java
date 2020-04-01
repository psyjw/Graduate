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
import static com.next.schoolmemory.ClassifyResultActivity.PEOPLE_TYPE;

//Similar to LabelClassifyFirstFragment
//这个类和LabelClassifyFirstFragment类极为类似，只是把搜索的对象从标签变成了人物。因此不再注释。
public class PeopleClassifyFirstFragment extends Fragment {
    private ListView listView;
    private NoteInfoModel noteInfoModel;
    private String peopleTotal="";
    private String[] people;
    private ArrayAdapter<String> adapter;
    private EditText search_input;
    List<NoteBean> list;
    List<String> tagList=new ArrayList<>();

    private RecyclerView tagRecyclerView;
    private static final int MAX = 9;
    private TagAdapter tagAdapter;

    public final int CLASSIFY_ACTIVITY=5000;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_people_classify_first, container, false);
        listView = (ListView)view.findViewById(R.id.search_result_list);
        search_input = (EditText) view.findViewById(R.id.people_search_edittext);

        tagRecyclerView = (RecyclerView) view.findViewById(R.id.tag_rv);

        collectPeople();
        //showPeople();
        initInputArea();

        showTags();

        return view;
    }

    private void showTags() {
        GridLayoutManager layoutManage = new GridLayoutManager(this.getActivity(), 2);
        layoutManage.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (tagList.get(position).length()>MAX)
                    return 2;
                return 1;
            }
        });
        tagRecyclerView.setLayoutManager(layoutManage);
        tagAdapter = new TagAdapter(tagList,PeopleClassifyFirstFragment.this, PEOPLE_TYPE);
        tagRecyclerView.setAdapter(tagAdapter);
    }

    private void collectPeople(){
        noteInfoModel = new NoteInfoModel(this.getActivity());//建立数据库连接
        list = noteInfoModel.QueryAllNotefromData();

        for(int i=0;i<list.size();i++){
            String[] str = list.get(i).getPeople().split(";");
            for(int j=0;j<str.length;j++) {
                if(!str[j].equals(""))
                if (!peopleTotal.contains(str[j] + ";"))
                    peopleTotal = peopleTotal + str[j] + ";";
            }
        }
        people = peopleTotal.split(";");
        for(int i=0;i<people.length;i++) {
            if(!people[i].equals(""))
                tagList.add(people[i]);
        }
        /******
        if(people[0].equals("")){
            Toast.makeText(getActivity(),"统计故障！！", Toast.LENGTH_SHORT).show();
        }
        *****/
    }

    private void initInputArea(){
        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = search_input.getText().toString();
                List<String> tmp = new ArrayList<>();
                String newDatas[];
                for(int i=0;i< people.length;i++){
                    if(people[i].contains(str))
                        tmp.add(people[i]);
                }
                newDatas = new String[tmp.size()];
                for(int i=0;i<tmp.size();i++){
                    newDatas[i]=tmp.get(i);
                }

                adapter=new ArrayAdapter<String>(PeopleClassifyFirstFragment.this.getActivity(),android.R.layout.simple_list_item_1,newDatas);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        startClassifyResultActivity(people[position]);
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

    private void startClassifyResultActivity(String search_str){
        Intent intent = new Intent(PeopleClassifyFirstFragment.this.getActivity(), ClassifyResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("search", search_str);
        bundle.putInt("type", PEOPLE_TYPE);
        intent.putExtras(bundle);
        startActivityForResult(intent, CLASSIFY_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==CLASSIFY_ACTIVITY) {
                //刷新数据 refresh data
                collectPeople();
                showTags();
            }
        }
    }
}
