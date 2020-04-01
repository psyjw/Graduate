package com.next.schoolmemory.Bean;
import com.baidu.mapapi.model.LatLng;

import java.io.Serializable;

//用于储存noteBean的信息，结构和NoteBean 基本一样，但是实现了Serializable接口。
// this class is for saving the information in noteBean, it has almost the same content as notebean, but it implements Serializable interface.
public class Noteinfo implements Serializable {
    private Long id;
    private String noteinfo;
    private String notetype;
    private int date;
    private String time;
    private String people;
    private String labels;
    //private LatLng location;
    // Caused by: java.io.NotSerializableException: com.baidu.mapapi.model.LatLng
    //java.lang.RuntimeException: Parcelable encountered IOException writing serializable object (name = com.next.schoolmemory.Bean.Noteinfo)
    public static transient LatLng location = null;
    private String photopath;
    private Boolean isshow;
    private String createtime;
    private String smallPicPath;

    public Noteinfo(Long id, String noteinfo, String notetype,
                    int date, String time, LatLng location, String photopath,
                    Boolean isshow, String createtime, String smallPicPath) {
        this.id = id;
        this.noteinfo = noteinfo;
        this.notetype = notetype;
        this.date = date;
        this.time = time;
        this.location = location;
        this.photopath = photopath;
        this.isshow = isshow;
        this.createtime = createtime;
        this.smallPicPath = smallPicPath;
    }
    public Noteinfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNoteinfo() {
        return this.noteinfo;
    }
    public void setNoteinfo(String noteinfo) {
        this.noteinfo = noteinfo;
    }
    public String getNotetype() {
        return this.notetype;
    }
    public void setNotetype(String notetype) {
        this.notetype = notetype;
    }
    public int getDate() {
        return this.date;
    }
    public void setDate(int date) {
        this.date = date;
    }
    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public LatLng getLocation() {
        return this.location;
    }
    public void setLocation(LatLng location) {
        this.location = location;
    }
    public String getPhotopath() {
        return this.photopath;
    }
    public void setPhotopath(String photopath) {
        this.photopath = photopath;
    }
    public void setSmallPicPath(String smallPicPath) {this.smallPicPath=smallPicPath;}
    public String getSmallPicPath(){return this.smallPicPath;}
    public Boolean getIsshow() {
        return this.isshow;
    }
    public void setIsshow(Boolean isshow) {
        this.isshow = isshow;
    }
    public String getCreatetime() {
        return this.createtime;
    }
    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public void setPeople(String people) {
        this.people = people;
    }
    public String getPeople(){
        return this.people;
    }
    public void setLabels(String labels) {this.labels=labels;}
    public String getLabels() { return this.labels;}
}
