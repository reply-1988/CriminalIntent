package com.example.jingj.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, int destWidh, int destHeight) {
        //读取磁盘上图片的大小
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidh = options.outWidth;
        float srcHeight = options.outHeight;
        //判断出来需要裁减多少
        int inSampleSize = 1;
        if (srcWidh > destWidh || srcHeight > destHeight) {
            float heightscale = srcHeight / destHeight;
            float widhscale = srcWidh / destWidh;

            inSampleSize = Math.round(heightscale > widhscale ? heightscale : widhscale);

            options = new BitmapFactory.Options();
        }
        options.inSampleSize = inSampleSize;
        //读取并创建最终的Bitmap
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

    public static Bitmap getFullBitmap(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(path, options);
    }

}
