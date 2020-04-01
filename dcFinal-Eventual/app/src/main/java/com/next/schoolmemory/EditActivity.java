package com.next.schoolmemory;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.joaquimley.faboptions.FabOptions;
import com.next.schoolmemory.Bean.Means;
import com.next.schoolmemory.Bean.NoteBean;
import com.jaeger.library.StatusBarUtil;

import com.next.schoolmemory.Bean.Noteinfo;
import com.next.schoolmemory.Presenter.Prestener_edit;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISListConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import me.gujun.android.taggroup.TagGroup;

public class EditActivity extends AppCompatActivity implements View.OnClickListener{
    private Prestener_edit prester_edit;//中间类，连接这个Activity和数据库
    private FabOptions fabOptions;//底下的省略号按钮
    private Toolbar toolbar_add;//工具栏
    private TagGroup tagGroup_people;//用于显示已添加的人物
    private TagGroup tagGroup_label;//用于显示已添加的标签
    private TagGroup tagGroup_location;//用于显示已添加的地点
    private List<String> tags_people;
    private List<String> tags_label;
    private MaterialEditText materialEditText;//文字输入区域
    private NoteBean noteBean;//最后加入数据库的数据类
    private static final int REQUEST_LIST_CODE = 0;
    private static final int REQUEST_CAMERA_CODE = 1;
    private LatLng location = null;
    private int addType;
    MaterialEditText input;
    private TextView dateTextView;
    private int type = 0;
    private ImageView imageView;
    private TextView pic_path;

    private final int ACTIVITY_LOCATE = 2000;

    @TargetApi(Build.VERSION_CODES.M)

    //实现中间类
    private void initprestener(){
        prester_edit=new Prestener_edit(this);
    }

    private final int CHANGE_TYPE=10;

    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        initprestener();//实现代理接口//interface
        initViewAndData();//一系列的初始化//Initialization
        Bundle bundle = getIntent().getBundleExtra("data");//获取传来的数据，只有用户选择“编辑”时会传来数据，否则bundle为null//get data
        if(bundle!=null) {
            type = bundle.getInt("type", 0);
            if (type == CHANGE_TYPE) {//用户选择了“编辑”，则将数据库中已有的信息显示到各个位置：输入区，图片区，标签区等等
                putInfoInNoteBean((Noteinfo) bundle.getSerializable("noteinfo"));
            }
        }
    }

    private void putInfoInNoteBean(Noteinfo noteinfo) {//将Noteinfo类的对象中的信息显示到界面上，并保存到一个noteBean中
        noteBean.setId(noteinfo.getId());//储存id
        materialEditText.setText(noteinfo.getNoteinfo());//输入界面显示文字//type words
        noteBean.setDate(noteinfo.getDate());//储存日期//save date
        String dateShow = (noteinfo.getDate()+"").substring(0,4)+"-"+(noteinfo.getDate()+"").substring(4,6)+"-"+(noteinfo.getDate()+"").substring(6,8);
        dateTextView.setText(dateShow);//显示日期//show date
        noteBean.setPhotopath(noteinfo.getPhotopath());//储存图片路径//save photo path
        noteBean.setSmallPicPath(noteinfo.getSmallPicPath());//储存压缩图片路径
        if(!noteBean.getSmallPicPath().equals("null")) {//如果图片存在//if picture is existed
            Bitmap bitmap = null;
            try {//由路径获取Bitmap//get bitmap
                bitmap = BitmapFactory.decodeStream(new FileInputStream(noteBean.getSmallPicPath()));//
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //存在两个特殊位置的图片显示出来会逆时针旋转90度，因此要把它们转回正常状态
            if (noteBean.getPhotopath().contains("cache") || noteBean.getPhotopath().contains("Camera"))
                bitmap = turn90degree(bitmap);//将图片旋转90度
            imageView.setImageBitmap(bitmap);//显示压缩图片
            pic_path.setText(noteBean.getPhotopath());//显示图片路径
        }
        if(!noteinfo.getPeople().equals("")) {//将目前的人物用taggroup显示出来
            noteBean.setPeople(noteinfo.getPeople());//储存人物
            List<String> tags_people_tmp = getTagsFromTotalString(noteinfo.getPeople());//由一长串String切割储存到List中
            tags_people = tags_people_tmp;
            tagGroup_people.setTags(tags_people);//用taggroup显示人物
        }
        if(!noteinfo.getLabels().equals("")) {//对标签进行处理，原理同上
            noteBean.setLabels(noteinfo.getLabels());
            List<String> tags_label_tmp = getTagsFromTotalString(noteinfo.getLabels());
            tags_label = tags_label_tmp;
            tagGroup_label.setTags(tags_label);
        }
        if(noteinfo.getLocation().latitude!=0 && noteinfo.getLocation().longitude!=0){//如果含有地址
            noteBean.setHasLocation(true);//储存地址
            noteBean.setLocation(noteinfo.getLocation());
            tagGroup_location.setTags("LONG:"+noteBean.getLocation_longtitude()+", "+"LAT："+noteBean.getLocation_latitude());//显示经纬度
        }
    }

    //由一长串String切割储存到List中
    private List<String> getTagsFromTotalString(String totalString) {
        List<String> res = new ArrayList<>();
        String[] data = totalString.split(";");//用分号进行分割，这样即使人名中有重复也没有关系
        for(int i=0;i<data.length;i++){
            res.add(data[i]);
        }
        return res;
    }


    private void initViewAndData(){
        pic_path = (TextView) this.findViewById(R.id.pic_path_textview);
        imageView = (ImageView)this.findViewById(R.id.small_image);
        this.tags_people=new ArrayList<>();
        this.tags_label=new ArrayList<>();
        this.noteBean=new NoteBean();
        dateTextView = this.findViewById(R.id.mydate);
        dateTextView.setText("Today");
        //noteBean对一系列内容进行初始化
        noteBean.setNotetype("null");//默认为null
        noteBean.setTime("null");
        noteBean.setHasLocation(false);//初始化设定为没有地址。另外，location的经纬度在不设置的情况下默认为0
        noteBean.setPhotopath("null");
        noteBean.setSmallPicPath("null");
        noteBean.setIsshow(true);
        inittoolbarseting();//初始化toolbar
        initfloationgButton();//初始化地下的省略号按钮
        inittagegroup();//初始化新建页面中的标签组
        initEdittextView();//输入区的初始化
        initsaveview();//初始化右上角的“保存”键，建立监听
        ISNav.getInstance().init(new ImageLoader() {//外部代码框架内容初始化
            @Override
            public void displayImage(Context context, String path, ImageView imageView) {
                Glide.with(context).load(path).into(imageView);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initphotoshowdialog(noteBean.getPhotopath());//当用户点击缩略图时对图片进行展示
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                photoDeleteAlert();//当用户长按缩略图时弹出窗口询问是否删除
                return false;
            }
        });
    }

    //照片删除弹出窗口提示
    private void photoDeleteAlert() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        String mes = "Sure to delete the photo?";//标题
        builder.setMessage(mes);
        builder.setCancelable(true);//是否可取消
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();//弹窗消失
                imageView.setImageBitmap(null);//图片显示区域删除图片，并将noteBean中的一系列内容初始化
                new File(noteBean.getSmallPicPath()).delete();
                noteBean.setPhotopath("null");
                noteBean.setSmallPicPath("null");
                pic_path.setText("");
                Toast.makeText(EditActivity.this, "The photo has been deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    //获取context
    public Context getbasecontext() {//获取环境，定义的时候说明返回的是一个Context类，activity本身也可作为context
        return this;
    }

    //实例化保存按钮
    private void initsaveview(){
        TextView saveview=this.findViewById(R.id.add_savefile);
        saveview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //用户点击右上角的保存则进入保存信息到数据库的步骤
                savedNoteinfotoDatabase();
            }
        });
    }

    //实例化floatingbuttond对象
    private void initfloationgButton(){//初始化底下的省略号按钮
        fabOptions=(FabOptions)this.findViewById(R.id.add_floatingbutton);
        fabOptions.setButtonsMenu(R.menu.menu_item_add_floatingbutton);//把对应的menu的xml文件起来
        fabOptions.setOnClickListener(this);//设置监听。
    }

    //实例化toolbar对象，设置返回按钮，监听返回按钮状态
    private void inittoolbarseting(){
        toolbar_add=(Toolbar)this.findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar_add);//启动toolbar
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//将向左的后退键显示出来
        getSupportActionBar().setTitle("");
        toolbar_add.setNavigationOnClickListener(new View.OnClickListener() {//对后退键的监听。这个只是针对左上角的后退键，使用手机自带的后退键仍然能触发保存机制
            @Override
            public void onClick(View view) {
                if (materialEditText.getText().toString().isEmpty()){//如果用户在新建文件中没有写入内容，则直接结束activity
                    finish();
                }else {//如果有写入内容，则开始准备保存，询问用户是否保存
                    initsavenotedialog();
                }
            }
        });
    }


    //实例化一个保存界面弹出的dialog
    private void initsavenotedialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.add_dialog_savenote_message);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.add_dialog_savenote_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                savedNoteinfotoDatabase();//将数据保存到数据库中
                dialogInterface.dismiss();//dialog消失
                finish();//activity结束
            }
        });
        builder.setNegativeButton(R.string.add_dialog_savenote_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    //将数据添加到数据库
    private void savedNoteinfotoDatabase(){
        if (materialEditText.getText().toString().isEmpty()){//如果为空，则不保存
            Toast.makeText(EditActivity.this, "textfield is empty", Toast.LENGTH_SHORT).show();
        }else {
            noteBean.setNoteinfo(materialEditText.getText().toString());//将内容储存到noteBean中
            noteBean.setCreatetime(Means.getCreatetime());
            if(noteBean.getDate()==0) noteBean.setDate(Integer.parseInt(Means.getCreatetime()));//如果用户没有设定具体日期，则设置为创建日期
            String people_save = "";
            //将tagGraoup中的内容保存到一个String中，每个人名/标签用分号隔开
            for(int i=0;i<tags_people.size();i++)
                people_save = people_save+tags_people.get(i)+";";//用分号隔开每个名字
            noteBean.setPeople(people_save);
            String label_save="";
            for(int i=0;i<tags_label.size();i++)
                label_save = label_save+tags_label.get(i)+";";//用分号隔开每个标签
            noteBean.setLabels(label_save);
            prester_edit.saveNoteinfotoDatabase(noteBean);//通过中间类将数据保存
            setResult(RESULT_OK);
            finish();
        }
    }

    //实例化标签框
    private void inittagegroup(){
        //初始化人物标签框
        tagGroup_people=(TagGroup)this.findViewById(R.id.people_group);
        tagGroup_people.setTags(tags_people);
        tagGroup_people.setOnTagClickListener(new TagGroup.OnTagClickListener(){
            @Override
            public void onTagClick(String tag) {
                peopleLabelDeleteAlert(0, tag);//用户点击一个tag，则弹出窗口询问用户是否删除这个tag
            }
        });
        tagGroup_label=(TagGroup) this.findViewById(R.id.label_group);
        tagGroup_label.setTags(tags_label);
        tagGroup_label.setOnTagClickListener(new TagGroup.OnTagClickListener(){
            @Override
            public void onTagClick(String tag) {
                peopleLabelDeleteAlert(1, tag);//标签同上
            }
        });
        tagGroup_location=(TagGroup)this.findViewById(R.id.taggroup_location);
        tagGroup_location.setTags("Empty");//定位显示初始化为Empty
        tagGroup_location.setOnTagClickListener(new TagGroup.OnTagClickListener(){
            @Override
            public void onTagClick(String tag) {
                if(tag!="Empty"){
                    locationDeleteAlert();//如果用户有进行过定位，则询问用户是否删除
                }
            }
        });
    }

    //定位删除询问dialog
    private void locationDeleteAlert() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        String mes = "Sure to delete location information?";
        builder.setMessage(mes);
        builder.setCancelable(true);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //显示区域初始化，noteBean中的信息初始化
                dialogInterface.dismiss();
                noteBean.setHasLocation(false);
                noteBean.setLocation_latitude(0);
                noteBean.setLocation_longtitude(0);
                tagGroup_location.setTags("Empty");
                Toast.makeText(EditActivity.this, "Location informaiton has been deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    //人物或标签删除询问dialog
    private void peopleLabelDeleteAlert(final int type, final String tag) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        String mes = "Sure to delete "+tag+" ";
        if(type==0) mes+="People";
        else if(type==1) mes+="Tag";
        builder.setMessage(mes);
        builder.setCancelable(true);
        boolean res;
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(type==0) {
                    tags_people.remove(tag);
                    tagGroup_people.setTags(tags_people);
                    Toast.makeText(EditActivity.this, tag + "Deleted successfully", Toast.LENGTH_SHORT).show();
                }
                else if(type==1){
                    tags_label.remove(tag);
                    tagGroup_label.setTags(tags_label);
                    Toast.makeText(EditActivity.this, tag + "Deleted successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    //实例化一个显示图片的dialog。（用于用户点击缩略图时显示大图）
    private void initphotoshowdialog(String imagpath){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        LayoutInflater layoutInflater=LayoutInflater.from(this);
        View centerview=layoutInflater.inflate(R.layout.activity_add_photodiaolg,null);
        final ImageView photoimageview;
        final AlertDialog alertDialog=builder.setView(centerview).create();
        photoimageview=(ImageView)centerview.findViewById(R.id.add_dialog_imageview);
        Glide.with(centerview.getContext()).load(imagpath).into(photoimageview);//用Glide代码框架进行显示
        photoimageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();//点击后大图消失
            }
        });
        alertDialog.show();
    }

    //实例化一个edittext
    private void initEdittextView(){
        materialEditText=(MaterialEditText)this.findViewById(R.id.add_noteinfoedittext);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_floatingbutton_people://添加人物或标签
                addPeople();
                if (fabOptions.isOpen()){
                    fabOptions.close(null);
                }
                break;
            case R.id.add_floatingbutton_location:
                startLocateActivity();//添加定位
                if (fabOptions.isOpen()){
                    fabOptions.close(null);
                }
                break;
            case R.id.add_floatingbutton_photo://添加图片（拍照或从相册选择）
                initphotoseleteActivity();
                if (fabOptions.isOpen()){
                    fabOptions.close(null);
                }
                break;
            case R.id.add_floatingbutton_time://设定日期
                initdatecenterdialog();
                if (fabOptions.isOpen()){
                    fabOptions.close(null);
                }
                break;
            default:
                break;
        }
    }

    //弹出dialog，让用户输入人名/标签，可进行选择
    private void addPeople() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.add_people_dialog_message);
        builder.setCancelable(true);

        View alertView =  getLayoutInflater().inflate(R.layout.add_alert, null);
        final RadioGroup radioGroup = alertView.findViewById(R.id.rgroup);
        if(radioGroup!=null) {
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.radio_people:
                            updateAddType(0);
                            break;
                        case R.id.radio_label:
                            updateAddType(1);
                            break;
                    }
                }
            });
        }
        input = (MaterialEditText) alertView.findViewById(R.id.alert_input_area);
        builder.setView(alertView);
        builder.setPositiveButton(R.string.add_people_dialog_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(!input.getText().toString().equals("")) {//如果输入不为空
                    //将数据保存
                    if (addType == 0) {
                        tags_people.add(input.getText().toString());
                        tagGroup_people.setTags(tags_people);
                    } else if (addType == 1) {
                        tags_label.add(input.getText().toString());
                        tagGroup_label.setTags(tags_label);
                    }
                }
                dialogInterface.dismiss();
                radioGroup.check(R.id.radio_people);//对radioGruop的初始化
            }
        });
        builder.setNegativeButton(R.string.add_people_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

     //实例化图片拍摄选取对象，代码来自框架说明，通过这个框架用户可以选择通过系统相册中的图片添加还是拍照添加
    @TargetApi(Build.VERSION_CODES.M)
    private void initphotoseleteActivity(){
        ISListConfig config=new ISListConfig.Builder()
                .multiSelect(false)
                .rememberSelected(false)
                .btnBgColor(Color.WHITE)
                .btnTextColor(Color.BLACK)
                .statusBarColor(getColor(R.color.colorFloatingButton))
                .backResId(R.drawable.icon_back)
                .title("Select photos")
                .titleColor(Color.BLACK)
                .titleBgColor(getColor(R.color.colorFloatingButton))
                .needCamera(true)
                .needCrop(false)
                .cropSize(0,0,400,200)
                .maxNum(9)
                .build();
        ISNav.getInstance().toListActivity(this,config,REQUEST_CAMERA_CODE);
    }

    //实例化一个日期选择dialog
    private void initdatecenterdialog(){
        final Calendar calendar=Calendar.getInstance();
        new DatePickerDialog(this,0, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                //后面三个参数分别对应年，月，日
                int month=i1+1;//月份要+1才和实际日期相同
                String date_tmp = new String();
                date_tmp = date_tmp+""+i;
                if(month<10) date_tmp+="0";
                date_tmp+=month;
                if(i2<10) date_tmp+="0";
                date_tmp+=i2;
                //updateTagsGroup(2,date_tmp);
                noteBean.setDate(Integer.parseInt(date_tmp));
                String dateShow = date_tmp.substring(0,4)+"-"+date_tmp.substring(4,6)+"-"+date_tmp.substring(6,8);
                dateTextView.setText(dateShow);
            }
        },calendar.get(Calendar.YEAR)
                ,calendar.get(Calendar.MONTH)
                ,calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    //对“添加图片”跳转到其他activity返回的结果进行处理
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            if (requestCode == REQUEST_CAMERA_CODE && data != null) {//如果是从相册处理任务返回
                List<String> pathlist = data.getStringArrayListExtra("result");
                noteBean.setPhotopath(pathlist.get(0).toString());
                //Toast.makeText(EditActivity.this, "返回图片成功", Toast.LENGTH_SHORT).show();
                //String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tencent/MicroMsg/WeiXin/";
                //     /storage/emulated/0/tencent/...
                pic_path.setText(noteBean.getPhotopath());

                comPressBitMap(noteBean);//对图片进行压缩
            }
            else if(requestCode == ACTIVITY_LOCATE){//如果是从添加定位活动返回
                Bundle bundle = data.getParcelableExtra("bundle");
                location = bundle.getParcelable("location");
                if(location!=null) {
                    noteBean.setLocation(location);//将定位传入
                    noteBean.setHasLocation(true);//表明有定位信息
                    tagGroup_location.setTags("LONG: "+noteBean.getLocation_longtitude()+", "+"LAT:"+noteBean.getLocation_latitude());//将定位的经纬度显示出来
                }
            }
        }
    }

    //跳转到添加定位activity
    protected void startLocateActivity(){
        Intent intent = new Intent();
        intent.setClass(EditActivity.this, LocateActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);
        startActivityForResult(intent,ACTIVITY_LOCATE);
    }

    public void updateAddType(int t){
        addType = t;
    }

    //压缩noteBean中的照片成bitmap，然后以jpg格式保存到外部存储中
    private void comPressBitMap(NoteBean noteBean){
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(noteBean.getPhotopath()));//将大图变为BitMap
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);//写入到输出流
            BitmapFactory.Options options = new BitmapFactory.Options();//通过options进行参数设定
            options.inJustDecodeBounds = true;
            byte[] bytes = outputStream.toByteArray();
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            int oldWidth = options.outWidth;
            int scal = oldWidth / 500;//压缩比例
            options.inJustDecodeBounds = false;
            options.inSampleSize = scal;
            Bitmap map = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);//压缩后得到的bitmap
            String filename = UUID.randomUUID().toString()+".jpg";//通过UUID生成文件名
            String ssmallPicPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.next.schoolmemory/smallPic/"+filename;
            noteBean.setSmallPicPath(ssmallPicPath);//设置压缩图片路径
            File file = new File(noteBean.getSmallPicPath());
            if(!(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.next.schoolmemory/smallPic").exists())) {
                new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/com.next.schoolmemory/smallPic").mkdirs();
                //如果文件夹不存在就先创建
                //Toast.makeText(EditActivity.this, "创建了文件夹！",Toast.LENGTH_SHORT).show();
            }
            //通过输出流进行写入
            FileOutputStream out = new FileOutputStream(noteBean.getSmallPicPath());
            map.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

            //Toast.makeText(EditActivity.this, "压缩图片成功", Toast.LENGTH_SHORT).show();
            Bitmap showmap;
            //在两个路径中图片显示会不正确，需要进行旋转
            if(noteBean.getPhotopath().contains("cache")||noteBean.getPhotopath().contains("Camera"))
                showmap = turn90degree(map);
            else
                showmap = map;

            imageView.setImageBitmap(showmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //旋转图片
    public Bitmap turn90degree(Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        //Toast.makeText(EditActivity.this, "旋转图片成功", Toast.LENGTH_SHORT).show();
        return rotatedBitmap;
    }
}
