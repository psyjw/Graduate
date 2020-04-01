package com.next.schoolmemory.Presenter;

import java.util.ArrayList;
import java.util.List;

import com.next.schoolmemory.Bean.Noteinfo;
import com.next.schoolmemory.NoteinfoActivity;

//作为NoteInfoActivity和数据库连接的中间类
// this is the class between NoteInfoActivity and database.
public class Prestener_noteinfo{
    private NoteinfoActivity noteinfoActivityImp;
    public Prestener_noteinfo(NoteinfoActivity noteinfoActivityImp){//initialization
        this.noteinfoActivityImp=noteinfoActivityImp;
    }

    public void readDatatoNoteinfo(Noteinfo noteinfo) {
        noteinfoActivityImp.readNoteinfotoNotetext(noteinfo.getNoteinfo());//show the text message
        noteinfoActivityImp.readPhotopathtoNoteImageview(noteinfo.getPhotopath(),noteinfo.getNotetype());//show the picture
        List<String> tags=new ArrayList<String>();
        List<String> tags_people=new ArrayList<>();
        List<String> tags_label=new ArrayList<>();
        String people_tmp;
        String label_tmp;

        //将人物的String用分号隔开存到list中
        //save people tag in string, separated by semicolons and save them in list.
        people_tmp = noteinfo.getPeople();
        String[] p_tmp = people_tmp.split(";");
        for(int i=0;i<p_tmp.length;i++)
            tags_people.add(p_tmp[i]);
        //label同上
        //label saving, same as above
        label_tmp = noteinfo.getLabels();
        String[] l_tmp = label_tmp.split(";");
        for(int i=0;i<l_tmp.length;i++)
            tags_label.add(l_tmp[i]);

        noteinfoActivityImp.readPeopleTaggroup(tags_people);//将人物的tagGroup显示 // show people taggroup
        noteinfoActivityImp.readLabelTaggroup(tags_label);//将标签的tagGroup显示 // show tag taggroup
        noteinfoActivityImp.readDate(noteinfo.getDate());//日期显示 // show date
    }
}
