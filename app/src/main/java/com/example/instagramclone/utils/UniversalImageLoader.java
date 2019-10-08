package com.example.instagramclone.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.instagramclone.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class UniversalImageLoader {

    private static final int defaultimage = R.drawable.ic_android;
    private Context mContext ;

    public UniversalImageLoader(Context mContext) {
        this.mContext = mContext;
    }

    public ImageLoaderConfiguration getConfig(){
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultimage)
                .showImageForEmptyUri(defaultimage)
                .showImageOnFail(defaultimage)
                .considerExifParams(true) //----> rotation fixed
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build() ;

        return configuration;

    }

    /**
     * Usamos este se puede usar para cuando tenemos imagenes estaticas. No lo uses para imagenes que se cambien en Fragment/activities
     * o en list or gridLists
     * @param imgURL
     * @param image
     * @param mProgressBar
     * @param append
     */
    public static void setImage(String imgURL, ImageView image, final ProgressBar mProgressBar, String append ){

    ImageLoader imageLoader = ImageLoader.getInstance();
    imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String imageUri, View view) {
            if ( mProgressBar != null){
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if ( mProgressBar != null){
                mProgressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if ( mProgressBar != null){
                mProgressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            if ( mProgressBar != null){
                mProgressBar.setVisibility(View.GONE);
            }
        }
    });





















    }
}
