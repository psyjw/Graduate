package com.next.schoolmemory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Presenter.Presenter_picwall;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//本类用于在图墙模块，让图片以“瀑布流”形式显示

public class MyScrollView extends ScrollView implements View.OnTouchListener {
    //每页要加载的图片数量
    public static final int PAGE_SIZE = 15;
    //记录当前已加载到第几页
    private int page;
    //每一列的宽度
    private int columnWidth;
    //当前第一列的高度
    private int firstColumnHeight;
    //当前第二列的高度
    private int secondColumnHeight;
    //当前第三列的高度
    private int thirdColumnHeight;
    //是否已加载过一次layout，这里onLayout中的初始化只需加载一次
    private boolean loadOnce;
    //对图片进行管理的工具类
    private ImageLoader imageLoader;
    //第一列的布局
    private LinearLayout firstColumn;
    //第二列的布局
    private LinearLayout secondColumn;
    //第三列的布局
    private LinearLayout thirdColumn;
    //记录所有正在下载或等待下载的任务。
    private static Set<LoadImageTask> taskCollection;
    //MyScrollView下的直接子布局。
    private static View scrollLayout;
    //MyScrollView布局的高度。
    private static int scrollViewHeight;
    //记录上垂直方向的滚动距离。
    private static int lastScrollY = -1;
    //记录所有界面上的图片，用以可以随时控制对图片的释放。
    private List<ImageView> imageViewList = new ArrayList<ImageView>();

    private Presenter_picwall presenter_picwall;
    private SquareFragment squareFragment;

    public void setPresenter(Presenter_picwall pp, SquareFragment ss){
        this.presenter_picwall = pp;
        this.squareFragment = ss;
    }
    //在Handler中进行图片可见性检查的判断，以及加载更多图片的操作。
    private static Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            MyScrollView myScrollView = (MyScrollView) msg.obj;
            int scrollY = myScrollView.getScrollY();
            // 如果当前的滚动位置和上次相同，表示已停止滚动
            if (scrollY == lastScrollY) {
                // 当滚动的最底部，并且当前没有正在下载的任务时，开始加载下一页的图片
                if (scrollViewHeight + scrollY >= scrollLayout.getHeight()
                        && taskCollection.isEmpty()) {
                    myScrollView.loadMoreImages();
                }
                myScrollView.checkVisibility();
            } else {
                lastScrollY = scrollY;
                Message message = new Message();
                message.obj = myScrollView;
                // 5毫秒后再次对滚动位置进行判断
                handler.sendMessageDelayed(message, 5);
            }
        };
    };

    //MyScrollView的构造函数。Constructor Method
    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        imageLoader = ImageLoader.getInstance();
        taskCollection = new HashSet<LoadImageTask>();
        setOnTouchListener(this);
    }

    //进行一些关键性的初始化操作，获取MyScrollView的高度，以及得到第一列的宽度值。并在这里开始加载第一页的图片。
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce) {
            scrollViewHeight = getHeight();
            scrollLayout = getChildAt(0);
            firstColumn = (LinearLayout) findViewById(R.id.first_column);
            secondColumn = (LinearLayout) findViewById(R.id.second_column);
            thirdColumn = (LinearLayout) findViewById(R.id.third_column);
            columnWidth = firstColumn.getWidth();
            loadOnce = true;
            loadMoreImages();
        }
    }

    //监听用户的触屏事件，如果用户手指离开屏幕则开始进行滚动检测。 Listener
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Message message = new Message();
            message.obj = this;
            handler.sendMessageDelayed(message, 5);
        }
        return false;
    }

    //开始加载下一页的图片，每张图片都会开启一个异步线程去下载。
    public void loadMoreImages() {
        if (hasSDCard()) {
            int startIndex = page * PAGE_SIZE;
            int endIndex = page * PAGE_SIZE + PAGE_SIZE;
            if (startIndex < presenter_picwall.ImagePaths.length) {
//                Toast.makeText(getContext(), "Loading...", Toast.LENGTH_SHORT).show();
                if (endIndex > presenter_picwall.ImagePaths.length) {
                    endIndex = presenter_picwall.ImagePaths.length;
                }
                for (int i = startIndex; i < endIndex; i++) {
                    LoadImageTask task = new LoadImageTask();
                    taskCollection.add(task);
                    //task.execute(presenter_picwall.ImagePaths[i]);
                    task.execute(i);
                }
                page++;
            } else {
                Toast.makeText(getContext(), "No more photos", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No SIM card detected", Toast.LENGTH_SHORT).show();
        }
    }

    //遍历imageViewList中的每张图片，对图片的可见性进行检查，如果图片已经离开屏幕可见范围，则将图片替换成一张空图。
    public void checkVisibility() {
        for (int i = 0; i < imageViewList.size(); i++) {
            ImageView imageView = imageViewList.get(i);
            int borderTop = (Integer) imageView.getTag(R.string.border_top);
            int borderBottom = (Integer) imageView
                    .getTag(R.string.border_bottom);
            if (borderBottom > getScrollY()
                    && borderTop < getScrollY() + scrollViewHeight) {
                //String imageUrl = (String) imageView.getTag(R.string.image_url);
                //NoteBean noteBean = (NoteBean) imageView.getTag(R.string.image_url);
                int pos = (Integer) imageView.getTag(R.string.image_url);
                Bitmap bitmap = imageLoader.getBitmapFromMemoryCache(presenter_picwall.list.get(pos).getPhotopath());
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    LoadImageTask task = new LoadImageTask(imageView);
                    task.execute(pos);
                }
            } else {
                imageView.setImageResource(R.drawable.empty_photo);
            }
        }
    }

    /**
     * 判断手机是否有SD卡。
     *
     * @return 有SD卡返回true，没有返回false。
     */
    private boolean hasSDCard() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    //异步下载图片的任务
    class LoadImageTask extends AsyncTask<Integer, Void, Bitmap> {
        private NoteBean noteBean;
        //可重复使用的ImageView
        private ImageView mImageView;
        //记录每个图片对应的位置
        private int mItemPosition;

        public LoadImageTask() {
        }

        //将可重复使用的ImageView传入
        public LoadImageTask(ImageView imageView) {
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            mItemPosition = params[0];
            noteBean = presenter_picwall.list.get(mItemPosition);
            Bitmap imageBitmap = imageLoader
                    .getBitmapFromMemoryCache(noteBean.getSmallPicPath());
            if (imageBitmap == null) {
                imageBitmap = loadImage(noteBean.getSmallPicPath());
            }
            return imageBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                double ratio = bitmap.getWidth() / (columnWidth * 1.0);
                int scaledHeight = (int) (bitmap.getHeight() / ratio);
                addImage(bitmap, columnWidth, scaledHeight);
            }
            taskCollection.remove(this);
        }

        //加载图片
        private Bitmap loadImage(String imageUrl) {
            File imageFile = new File(imageUrl);
            if (imageUrl != null) {
                Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(imageFile.getPath(), columnWidth);
                if (bitmap != null) {
                    imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
                    return bitmap;
                }
            }
            return null;
        }

        /**
         * 向ImageView中添加一张图片
         *
         * @param bitmap
         *            待添加的图片
         * @param imageWidth
         *            图片的宽度
         * @param imageHeight
         *            图片的高度
         */
        private void addImage(Bitmap bitmap, int imageWidth, int imageHeight) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    imageWidth, imageHeight);
            if (mImageView != null) {
                mImageView.setImageBitmap(bitmap);
            } else {
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(params);
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);//ScaleType.FIT_XY
                imageView.setPadding(5, 5, 5, 5);
                imageView.setTag(R.string.image_url, mItemPosition);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ImageDetailsActivity.class);
                        //intent.putExtra("image_path", getImagePath(mImageUrl));
                        Bundle bundle = new Bundle();
                        intent.putExtras(bundle);
                        intent.putExtra("image_position", mItemPosition);//可以只传这个
                        getContext().startActivity(intent);
                    }
                });
                findColumnToAdd(imageView, imageHeight).addView(imageView);
                imageViewList.add(imageView);
            }
        }

        /**
         * 找到此时应该添加图片的一列。原则就是对三列的高度进行判断，当前高度最小的一列就是应该添加的一列。
         *
         * @param imageView
         * @param imageHeight
         * @return 应该添加图片的一列
         */
        private LinearLayout findColumnToAdd(ImageView imageView,
                                             int imageHeight) {
            if (firstColumnHeight <= secondColumnHeight) {
                if (firstColumnHeight <= thirdColumnHeight) {
                    imageView.setTag(R.string.border_top, firstColumnHeight);
                    firstColumnHeight += imageHeight;
                    imageView.setTag(R.string.border_bottom, firstColumnHeight);
                    return firstColumn;
                }
                imageView.setTag(R.string.border_top, thirdColumnHeight);
                thirdColumnHeight += imageHeight;
                imageView.setTag(R.string.border_bottom, thirdColumnHeight);
                return thirdColumn;
            } else {
                if (secondColumnHeight <= thirdColumnHeight) {
                    imageView.setTag(R.string.border_top, secondColumnHeight);
                    secondColumnHeight += imageHeight;
                    imageView
                            .setTag(R.string.border_bottom, secondColumnHeight);
                    return secondColumn;
                }
                imageView.setTag(R.string.border_top, thirdColumnHeight);
                thirdColumnHeight += imageHeight;
                imageView.setTag(R.string.border_bottom, thirdColumnHeight);
                return thirdColumn;
            }
        }
    }

}
