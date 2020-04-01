package com.next.schoolmemory.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.next.schoolmemory.AdvancedSearchActivity;
import com.next.schoolmemory.Bean.Means;
import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.ClassifyResultActivity;
import com.next.schoolmemory.EditActivity;
import com.next.schoolmemory.FileFragment;
import com.next.schoolmemory.NoteinfoActivity;
import com.next.schoolmemory.R;
import com.next.schoolmemory.SearchFragment;

public class RecyclerViewCardAdapter extends RecyclerView.Adapter<RecyclerViewCardAdapter.Viewholder> {
    private ArrayList<NoteBean> notelist;
    private Context context;
    private Fragment fragment;
    private AppCompatActivity myActivity;

    private int fragmentType;
    public final static int FILE_TYPE=1;
    public final static int SEARCH_TYPE=2;

    private int activityType;
    public final static int ADVANCED_TYPE = 11;
    public final static int CLASSIFY_TYPE = 12;

    private int contextType;
    public final static int ACTIVITY_TYPE=21;
    public final static int FRAGMENT_TYPE=22;

    private static final int ACTIVITY_NEWFILE = 1000;//request code

    //The difference between these two constructors is the type of the third parameter.
    public RecyclerViewCardAdapter(ArrayList<NoteBean> mnotelist, Context mcontext, Fragment fragment, int type){
        this.notelist=mnotelist;
        this.context=mcontext;
        this.fragment=fragment;
        fragmentType=type;
        contextType = FRAGMENT_TYPE;
    }

    public RecyclerViewCardAdapter(ArrayList<NoteBean> mnotelist, Context mcontext, AppCompatActivity mactivity, int type){
        this.notelist=mnotelist;
        this.context=mcontext;
        this.myActivity=mactivity;
        activityType=type;
        contextType = ACTIVITY_TYPE;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View iten_recycler= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recyclercard,parent,false);
        Viewholder viewholder=new Viewholder(iten_recycler);
        return viewholder;
    }

    //负责将每个子项holder绑定数据
    @Override
    public void onBindViewHolder(Viewholder holder, int position) {
        setMenuListener(holder.recycler_image_menu,notelist.get(position));//设置menu监听，点击弹出底部栏//add listener to the menu button, when user press it, a menu at the bottom will pop out

        holder.recycler_image_notetype.setImageResource(R.drawable.icon_diary3);//set the icon on the left
        holder.recycler_text_note.setText(R.string.note_diary);//set the main showing area

        holder.recycler_text_note.setText(Means.getNotetextOnRecyclerCard(notelist.get(position).getNoteinfo()));

        String date = (notelist.get(position).getDate()+"").substring(0,4)+"-"+(notelist.get(position).getDate()+"").substring(4,6)+"-"+(notelist.get(position).getDate()+"").substring(6,8);
        holder.recycler_text_time.setText(date);//show the date which the user designate in the diary
        addListener(holder.linearLayout,notelist.get(position));//add click listener, so that the user can go to detail page when clicking the item
    }

    @Override
    public int getItemCount() {
        return notelist==null ? 0 : notelist.size();
    }

    //初始化
    public static class Viewholder extends RecyclerView.ViewHolder{
        ImageView recycler_image_notetype,recycler_image_menu;
        TextView recycler_text_note,recycler_text_time;
        LinearLayout linearLayout;
        public Viewholder(View itemView){
            super(itemView);
            recycler_image_notetype=(ImageView)itemView.findViewById(R.id.recycler_image_notetype);//icon area
            recycler_image_menu=(ImageView)itemView.findViewById(R.id.recycler_image_menu);//the button on the right
            recycler_text_note=(TextView)itemView.findViewById(R.id.recycler_text_note);//text showing area
            recycler_text_time=(TextView)itemView.findViewById(R.id.recycler_text_time);//date area
            linearLayout=(LinearLayout)itemView.findViewById(R.id.recycler_item);
        }
    }
    //添加menu监听，即右边那个省略号按钮//add listener to the menu button on the right, when user press it, a menu at the bottom will pop out
    private void setMenuListener(View view, final NoteBean noteBean){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initBottomMenu(noteBean);//pop out the (edit or delete) menu on the bottom
            }
        });
    }

    private void addListener(View view, final NoteBean noteBean){//add listener, when user press the item, it will go to detail page.
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mintent;
                if(contextType==FRAGMENT_TYPE)
                    mintent = new Intent(fragment.getActivity(),NoteinfoActivity.class);
                else if(contextType==ACTIVITY_TYPE)
                    mintent = new Intent(myActivity, NoteinfoActivity.class);
                else
                    mintent = new Intent();
                Bundle bundle=new Bundle();
                bundle.putSerializable("noteinfo", Means.changefromNotebean(noteBean));
                mintent.putExtras(bundle);
                context.startActivity(mintent);
            }
        });
    }

    private void initBottomMenu(final NoteBean noteBean){//instantiation of the Bottomsheetdialog. This is the menu pop from the button
        final BottomSheetDialog bottomSheetDialog;
        final View dialogview;
        if(contextType==FRAGMENT_TYPE){
            bottomSheetDialog=new BottomSheetDialog(fragment.getActivity());
            dialogview= LayoutInflater.from(fragment.getActivity()).inflate(R.layout.activity_main_dialog,null);
        }
        else {
            bottomSheetDialog=new BottomSheetDialog(myActivity);
            dialogview= LayoutInflater.from(myActivity).inflate(R.layout.activity_main_dialog,null);
        }
        LinearLayout list_dialog_linear_delete,list_dialog_linear_change;
        list_dialog_linear_delete=(LinearLayout)dialogview.findViewById(R.id.main_dialog_linear_delete);//删除选项
        list_dialog_linear_change=(LinearLayout)dialogview.findViewById(R.id.main_dialog_linear_change);//修改选项

        list_dialog_linear_delete.setOnClickListener(new View.OnClickListener() {//删除选项的监听
            @Override
            public void onClick(View view) {
                initDeleteDilog(noteBean);//prepare to delete, show a alert dialog"are you sure?"
                bottomSheetDialog.dismiss();//make bottom menu disappear
            }
        });
        list_dialog_linear_change.setOnClickListener(new View.OnClickListener() {//open detail page
            @Override
            public void onClick(View view) {//the listener of the edit button
                Intent addintent;
                if(contextType==FRAGMENT_TYPE)
                    addintent=new Intent(fragment.getActivity(), EditActivity.class);
                else
                    addintent=new Intent(myActivity, EditActivity.class);
                Bundle bundle=new Bundle();
                bundle.putInt("type",10);//10 is a code for "EDIT"，telling editactivity that this is a edit process.
                bundle.putSerializable("noteinfo",Means.changefromNotebean(noteBean));
                addintent.putExtra("data",bundle);
                if(contextType==FRAGMENT_TYPE)
                    fragment.startActivityForResult(addintent,ACTIVITY_NEWFILE);
                else if(contextType==ACTIVITY_TYPE)
                    myActivity.startActivityForResult(addintent,ACTIVITY_NEWFILE);

                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(dialogview);
        bottomSheetDialog.show();
    }

    private void initDeleteDilog(final NoteBean noteBean){//show alert dialog ask whether the user is sure about delete
        AlertDialog.Builder builder;
        if(contextType==FRAGMENT_TYPE)
            builder=new AlertDialog.Builder(fragment.getActivity());
        else
            builder=new AlertDialog.Builder(myActivity);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure to delete it?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //这里分条件进行删除然后刷新操作，因为不同位置的刷新方法不同，需要调用到子类自己的方法，所以用强制转换
                //There are two places where user can delete. One is the list showing page, the other is the searching page.
                if(contextType==FRAGMENT_TYPE) {
                    switch (fragmentType) {
                        case FILE_TYPE:
                            ((FileFragment) fragment).presenter.deleteNotebean(noteBean);
                            ((FileFragment) fragment).refreshList();
                            break;
                        case SEARCH_TYPE:
                            ((SearchFragment) fragment).presenter.deleteNotebean(noteBean);
                            ((SearchFragment) fragment).refreshList();
                            break;
                    }
                }
                else if(contextType==ACTIVITY_TYPE){
                    switch (activityType){
                        case ADVANCED_TYPE:
                            ((AdvancedSearchActivity) myActivity).noteInfoModel.DeleteNotefromData(noteBean);
                            ((AdvancedSearchActivity) myActivity).refreshAndShowResult();
                            break;
                        case CLASSIFY_TYPE:
                            ((ClassifyResultActivity) myActivity).noteInfoModel.DeleteNotefromData(noteBean);
                            ((ClassifyResultActivity) myActivity).refreshAndShow();
                            break;
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
}
