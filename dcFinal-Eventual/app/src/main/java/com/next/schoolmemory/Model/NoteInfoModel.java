package com.next.schoolmemory.Model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.next.schoolmemory.Bean.DaoMaster;
import com.next.schoolmemory.Bean.DaoSession;
import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Bean.NoteBeanDao;

//DaoMaster, DaoSession, NoteBeanDao 在build/generated/source/greendao里面，要想重新生成只需Build-Make Project即可
//DaoMaster, DaoSession and NoteBeanDao is in build/generated/source/greendao, if want to regenerate, you need to go to Build-Make Project.
//本类用于和数据库的直接交互
public class NoteInfoModel {
    private NoteBeanDao noteBeanDao;
    private SQLiteDatabase db;
    private DaoSession daoSession;
    private Context mcontext;

    public NoteInfoModel(Context context){
        this.mcontext=context;
        initGreendao();//初始化数据库//initialize database
    }
    void initGreendao(){//创建数据库（根据数据库特性，如果创建数据库的名称和现有的数据库名称相同，则返回现有的该数据库）
        //create a database. According to greenDao, if the name of the new database is same to a existing database, then return the existing database.
        //以下内容为greenDao官方要求的初始化代码
        //below are the official contents to initialize greenDao.
        DaoMaster.DevOpenHelper helper=new DaoMaster.DevOpenHelper(mcontext,"recluse-db",null);
        db=helper.getWritableDatabase();
        DaoMaster daoMaster=new DaoMaster(db);
        daoSession=daoMaster.newSession();
        noteBeanDao=daoSession.getNoteBeanDao();
    }

    //数据库插入操作
    //insert to database
    public void InsertNotetoData(NoteBean noteBean) {
        noteBeanDao.insert(noteBean);
    }

    //数据库删除操作
    //delete from database
    public void DeleteNotefromData(NoteBean noteBean) {
        noteBeanDao.delete(noteBean);
    }
    //如果想要插入的notebean和数据库现有的notebean相同，则先将数据库中的删除，再插入新的
    //if the notebean which is going to be inserted has the same id as a existing notebean, delete the old one then insert the new one.
    public void ChangeNotetoData(NoteBean noteBean) {
        if (noteBean.getId()!=null){
            DeleteNotefromDataByid(noteBean.getId());
            noteBeanDao.insert(noteBean);
        }
    }



    public List<NoteBean> QueryAllNotefromData() {
        //以创建日期排序，最后创建的在最上面
        //sort by the create date, the latest one is on the top.
        daoSession.clear();
        noteBeanDao.detachAll();
        List mlist=noteBeanDao.loadAll();
        Collections.reverse(mlist);//倒序
        return mlist;

        //以下按最新操作时间排序，但是排序根据只精确到日
        //List<NoteBean> allList = noteBeanDao.queryBuilder().where(NoteBeanDao.Properties.Noteinfo.notEq(""))
         //                                       .orderDesc(NoteBeanDao.Properties.Createtime).list();
        //return allList;
    }

    //根据id删除
    //delete by id
    public void DeleteNotefromDataByid(Long id) {
        noteBeanDao.deleteByKey(id);
    }

    //获取所有包含坐标的notebean
    //return all the notebean which has location information
    public List<NoteBean> getLocationPoints(){
        List<NoteBean> list = noteBeanDao.queryBuilder().
                where(NoteBeanDao.Properties.HasLocation.eq(true)).
                orderAsc(NoteBeanDao.Properties.Createtime).
                list();
        return list;
    }

    //用于简单搜索，搜索每个notebean中的noteinfo是否含有传入String
    //keyword searching, search the text messages in all notebean to see whether it contains parameter.
    public List<NoteBean> searchStringInBase(String ss){
        List<NoteBean> list = noteBeanDao.queryBuilder().
                where(NoteBeanDao.Properties.Noteinfo.like("%"+ss+"%")).
                orderAsc(NoteBeanDao.Properties.Createtime).
                list();
        return list;
    }

    //高级搜索，即多条件搜索
    //advanced search (multiple search)
    public List<NoteBean> advancedSearch(String keyword, String people,String label, String startDate, String endDate){
        int start = Integer.parseInt(startDate);
        int end = Integer.parseInt(endDate);
        List<NoteBean> list;
        QueryBuilder qb= noteBeanDao.queryBuilder();
        qb.where(NoteBeanDao.Properties.Noteinfo.like("%"+keyword+"%"),
                NoteBeanDao.Properties.People.like("%"+people+"%"),
                NoteBeanDao.Properties.Labels.like("%"+label+"%"),
                qb.and(NoteBeanDao.Properties.Date.ge(start), NoteBeanDao.Properties.Date.le(end)));
        list = qb.list();
        return list;
    }

    //通过人物精确搜索，假如同时包含人物“张三”和“张三丰”，搜索“张三”只会返回“张三”，不会包括“张三丰”
    //accurate search by people tag. If there is a "Tim" tag and "Timberlake" tag, when the user type "Tim" will only return the result of "Tim".
    public List<NoteBean> searchByPeople(String search_str) {
        List<NoteBean> list;
        String str = search_str+";";
        QueryBuilder qb= noteBeanDao.queryBuilder();
        qb.where(NoteBeanDao.Properties.People.like("%"+str+"%"));
        list = qb.list();
        return list;
    }

    //通过标签精确搜索，同上
    // search by tag
    public List<NoteBean> searchByLabel(String search_str) {
        List<NoteBean> list;
        String str = search_str+";";
        QueryBuilder qb= noteBeanDao.queryBuilder();
        qb.where(NoteBeanDao.Properties.Labels.like("%"+str+"%"));
        list = qb.list();
        return list;
    }

    public List<NoteBean> readAllPhotoBean(){ //返回所有含有图片的noteBean // return all of the notebean that contains picture
        return noteBeanDao.queryBuilder().where(NoteBeanDao.Properties.Photopath.notEq("null")).list();
    }
}
