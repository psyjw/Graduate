package com.next.schoolmemory.Presenter;


import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.EditActivity;
import com.next.schoolmemory.Model.NoteInfoModel;

//作为EditActivity连接数据库的中间类
// this is the class between EditActivity and database.
public class Prestener_edit {
    private EditActivity editActivity;
    private NoteInfoModel noteInfoModel;

    public Prestener_edit(EditActivity editActivityImp){//初始化 // initialization
        this.editActivity=editActivityImp;
        this.noteInfoModel=new NoteInfoModel(editActivityImp.getbasecontext());
    }

    //添加到数据库
    public void saveNoteinfotoDatabase(NoteBean noteBean) {
        if (noteBean.getId()!=null){
            noteInfoModel.ChangeNotetoData(noteBean);
        }else {
            noteInfoModel.InsertNotetoData(noteBean);
        }
    }
}
