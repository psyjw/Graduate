package com.next.schoolmemory;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

//本类用于详情页展示中，使得图片宽度和屏幕宽度相同，长度以图片初始比例进行调整。

public class MyImageView extends ImageView {
    public MyImageView(Context context) {//java代码new对象使用
        super(context);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs) {
        //xml中布局使用
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = getDrawable();
        if(drawable != null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            float drawHeight = drawable.getIntrinsicHeight();
            float drawWidth = drawable.getIntrinsicWidth();
            // 控件的宽度width = 屏幕的宽度 MeasureSpec.getSize(widthMeasureSpec);
            // 控件的高度 = 控件的宽度width*图片的宽高比 drawHeight / drawWidth；
            int height = (int) Math.ceil(width * (drawHeight / drawWidth));
            setMeasuredDimension(width, height);
        }
        else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}