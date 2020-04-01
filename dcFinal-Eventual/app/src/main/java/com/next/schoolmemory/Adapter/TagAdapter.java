package com.next.schoolmemory.Adapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.next.schoolmemory.ClassifyResultActivity;
import com.next.schoolmemory.R;

import java.util.ArrayList;
import java.util.List;

//分类界面下的Adapter
// the adapter in classify page
public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {
    private List<String> tagList;
    public Fragment fragment;
    private int searchType;//初始值为0 //the initial value is 0

    //initialization
    public TagAdapter(List<String> tagList, Fragment frag, int type) {
        this.tagList = tagList;
        this.fragment = frag;
        this.searchType = type;
    }
    @Override
    public TagAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_layout, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    //将每个子项holder绑定数据
    //bind data to each holder
    @Override
    public void onBindViewHolder(final TagAdapter.ViewHolder holder, final int position) {
        holder.mTextView.setText(tagList.get(position));
        holder.itemView.setTag(tagList.get(position));
        startClassifyResultActivity(holder.linearLayout, holder.mTextView.getText().toString());
    }

    private void startClassifyResultActivity(View view, final String str) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//对每个项目添加点击监听，点击后进入这个词条的分类项目 //add listener to each tag, when the user press this tag, it will go to related calssify page.
                //Toast.makeText(fragment.getActivity(), "the user clicks！", Toast.LENGTH_SHORT).show();
                Intent mintent = new Intent(fragment.getActivity(), ClassifyResultActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("search",str);
                bundle.putInt("type", searchType);
                mintent.putExtras(bundle);
                fragment.startActivity(mintent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public LinearLayout linearLayout;

        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.tag_tv);
            linearLayout = (LinearLayout)view.findViewById(R.id.linearlayout);
        }
    }
}
