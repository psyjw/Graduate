package com.next.schoolmemory.Presenter;

import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.MapFragment;
import com.next.schoolmemory.Model.NoteInfoModel;
import com.next.schoolmemory.SquareFragment;

import java.util.List;

////作为SquareFragment连接数据库的中间类
// this is the class between SquareFragment and database.
public class Presenter_picwall {
    private SquareFragment mSquareFragment;
    private NoteInfoModel noteInfoModel;
    public String[] ImagePaths;
    public List<NoteBean> list;

    public Presenter_picwall(SquareFragment squareFragment){//初始化 // initialization
        this.mSquareFragment = squareFragment;
        noteInfoModel=new NoteInfoModel(mSquareFragment.getActivity());
        refreshPicPaths();
    }

    public void refreshPicPaths(){
        //获取所有包含图片的notebean和其中所有的图片路径 // get all of the notebeans in the database that contains picture and the path of these pictures.
        list = noteInfoModel.readAllPhotoBean();
        ImagePaths = new String[list.size()];
        for(int i=0;i<list.size();i++){
            ImagePaths[i] = list.get(i).getPhotopath();
        }
    }
}