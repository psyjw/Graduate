package com.next.schoolmemory.Presenter;

import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.MapFragment;
import com.next.schoolmemory.Model.NoteInfoModel;

import java.util.List;

//作为mapFragment连接数据库的中间类
// this is the class between mapFragment and database.
public class Presenter_map {
    private MapFragment mMapFragment;
    private NoteInfoModel noteInfoModel;

    public Presenter_map(MapFragment mapFragment){//初始化
        this.mMapFragment = mapFragment;
        noteInfoModel=new NoteInfoModel(mapFragment.getListActivityConent());
    }

    public List<NoteBean> getLocPoints(){
        return noteInfoModel.getLocationPoints();
    }
}
