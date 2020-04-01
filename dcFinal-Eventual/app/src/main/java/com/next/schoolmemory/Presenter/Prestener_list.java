package com.next.schoolmemory.Presenter;

import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.FileFragment;
import com.next.schoolmemory.Model.NoteInfoModel;

//作为fileFragment和数据库连接的中间类
// this is the class between fileFragment and database.
public class Prestener_list {
    private FileFragment fileFragment;
    private NoteInfoModel noteInfoModelImp;

    public Prestener_list(FileFragment mfileFragment){//初始化 // initialization
        this.fileFragment=mfileFragment;
        noteInfoModelImp=new NoteInfoModel(fileFragment.getListActivityConent());
    }

    public void readNotefromDatatoList() {//读取数据库所有notebean并显示 // get information from database and show the list
        fileFragment.readAllNotefromData(noteInfoModelImp.QueryAllNotefromData());
    }
    //删除notebean //delete notebean
    public void deleteNotebean(NoteBean noteBean) {
        noteInfoModelImp.DeleteNotefromData(noteBean);
    }
}
