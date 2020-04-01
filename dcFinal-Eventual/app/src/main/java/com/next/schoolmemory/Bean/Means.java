package com.next.schoolmemory.Bean;


import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public abstract class Means {
    public static String getNoteTitleOnNoteinfoActivity(String note){//如果要显示的内容过长，将后面的内容以省略号表示 //if there are too many words in the diary, use apostrophe
        int length=note.length();
        if (length<=5){
            return note;
        }else {
            return note.substring(0,5)+"...";
        }
    }
    //用于设置RecyclerViewCardAdapter内容显示区域的内容，过长的用省略号表示
    //set the content in the showing area of RecyclerViewCardAdapter, if it is too long, use apostrophe
    public static String getNotetextOnRecyclerCard(String note){
        int length=note.length();
        if (length<=20){
            return note;
        }else if (length<=40){
            return note+"\n";
        }else {
            return note.substring(0,40)+"...";
        }
    }

    public static String getCreatetime(){//获取最新更新时间，即系统当前时间 // get date from system
        Calendar calendar= Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String year= String.valueOf(calendar.get(Calendar.YEAR));
        String month= String.valueOf(calendar.get(Calendar.MONTH)+1);//月份要+1才和实际符合 //the number of month needs to plus 1
        String day= String.valueOf(calendar.get(Calendar.DATE));
        String hour= String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minute= String.valueOf(calendar.get(Calendar.MINUTE));
        String result=new String();
        result+=""+year;

        if(Integer.parseInt(month)<10) result+="0";
        result+= month;

        if(Integer.parseInt(day)<10) result+="0";
        result+= day;
        return result;
    }

    //将NoteBean转化成实现了Serializable接口的Noteinfo // change notebean to noteinfo
    //noteinfo和notebean两个类内容基本相同 // the content in noteinfo and notebean are almost the same. but the class noteinfo implements Serializable interface
    public static Noteinfo changefromNotebean(final NoteBean noteBean){
        Noteinfo noteinfo=new Noteinfo();
        String info,type,time,photo,smallphoto, createtime,people,labels;
        int date;
        LatLng location;
        boolean isshow;
        Long id;
        id=noteBean.getId();
        people=noteBean.getPeople();
        labels=noteBean.getLabels();
        info=noteBean.getNoteinfo();
        type=noteBean.getNotetype();
        date=noteBean.getDate();
        time=noteBean.getTime();
        location=noteBean.getLocation();
        photo=noteBean.getPhotopath();
        smallphoto=noteBean.getSmallPicPath();
        createtime=noteBean.getCreatetime();
        isshow=noteBean.getIsshow();
        noteinfo.setId(id);
        noteinfo.setPeople(people);
        noteinfo.setLabels(labels);
        noteinfo.setNoteinfo(info);
        noteinfo.setNotetype(type);
        noteinfo.setDate(date);
        noteinfo.setTime(time);
        noteinfo.setLocation(location);
        noteinfo.setPhotopath(photo);
        noteinfo.setSmallPicPath(smallphoto);
        noteinfo.setCreatetime(createtime);
        noteinfo.setIsshow(isshow);
        return noteinfo;
    }
}
