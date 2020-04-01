package com.next.schoolmemory.Presenter;

import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Model.NoteInfoModel;
import com.next.schoolmemory.SearchFragment;

import java.util.List;

////作为searchFragment连接数据库的中间类
// this is the class between searchFragment and database.
public class Presenter_search {
    private SearchFragment mSearchFragment;
    private NoteInfoModel noteInfoModel;

    public Presenter_search(SearchFragment searchFragment){//initialization
        this.mSearchFragment = searchFragment;
        this.noteInfoModel = new NoteInfoModel(mSearchFragment.getActivity());
    }

    public List<NoteBean> searchStringInDatabase(String ss){//easy search(keyword search) function
        List<NoteBean> list = noteInfoModel.searchStringInBase(ss);
        return list;
    }

    public void deleteNotebean(NoteBean noteBean) {
        //通过noteInfoModel类来删除
        noteInfoModel.DeleteNotefromData(noteBean);
    }
}
