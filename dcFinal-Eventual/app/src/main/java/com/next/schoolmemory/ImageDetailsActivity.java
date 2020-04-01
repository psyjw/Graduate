package com.next.schoolmemory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.next.schoolmemory.Bean.Means;
import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Bean.Noteinfo;
import com.next.schoolmemory.Model.NoteInfoModel;

import java.io.File;
import java.util.List;

//图墙点击后进入的大图模式activity
//主要包含两个功能，一个是将图片缩小到原始尺寸下左右滑动切换功能，另一个是对图片的放大缩小和移动功能
public class ImageDetailsActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private List<NoteBean> list;
    int imagePosition;
    int currentPage;
    private ViewPager viewPager;//用于管理图片的滑动// manage swiping photo

    //显示当前图片的页数// total number of pages
    private TextView pageText;
    private NoteInfoModel noteInfoModel;
    public String[] ImagePaths;
    public Noteinfo noteinfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_details);

        noteInfoModel = new NoteInfoModel(this);
        refreshPicPaths();//刷新获取所有图片notebean和图片路劲

        imagePosition = getIntent().getIntExtra("image_position", 0);//获取用户点击的图片的具体位置，如果没有传入，则默认为0，即第一张图片
        pageText = (TextView) findViewById(R.id.page_text);//显示当前页数和总页数的textview
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(imagePosition);
        viewPager.setOnPageChangeListener(this);
        // 设定当前的页数和总页数// setup current number of pages and total pages
        pageText.setText((imagePosition + 1) + "/" + list.size());
    }

    //ViewPager的适配器// viewPager adapter
    class ViewPagerAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            String imagePath = list.get(position).getPhotopath();
            currentPage = position-1;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//以bitmap形式显示// bitmap
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.empty_photo);
            }
            View view = LayoutInflater.from(ImageDetailsActivity.this).inflate(
                    R.layout.zoom_image_layout, null);
            ZoomImageView zoomImageView = (ZoomImageView) view
                    .findViewById(R.id.zoom_image_view);
            zoomImageView.setImageBitmap(bitmap);
            zoomImageView.setActivity(ImageDetailsActivity.this);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

    }

    @Override
    public void onPageScrollStateChanged(int arg0) { }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) { }

    @Override
    public void onPageSelected(int currentPage) {
        // 每当页数发生改变时重新设定一遍当前的页数和总页数
        pageText.setText((currentPage + 1) + "/" + list.size());
        this.currentPage = currentPage;
    }

    public void refreshPicPaths(){//获取所有图片以及图片路径//get all photos and photo paths
        list = noteInfoModel.readAllPhotoBean();
        ImagePaths = new String[list.size()];
        for(int i=0;i<list.size();i++){
            ImagePaths[i] = list.get(i).getPhotopath();
        }
    }

    public void startNoteInfoActivity(){//进入详情页（用于双击监听后的活动）
        Intent intent=new Intent(ImageDetailsActivity.this, NoteinfoActivity.class);
        Bundle bundle=new Bundle();
        bundle.putSerializable("noteinfo", Means.changefromNotebean(list.get(currentPage)));
        intent.putExtras(bundle);
        startActivity(intent);
    }
}