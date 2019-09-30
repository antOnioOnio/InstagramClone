package com.example.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.instagramclone.R;
import com.example.instagramclone.utils.BottomNavigationViewHelper;
import com.example.instagramclone.utils.GridImageAdapter;
import com.example.instagramclone.utils.UniversalImageLoader;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;


    private  ImageView profilePhoto;
    private Context mContext = ProfileActivity.this;

    private ProgressBar mProgressBar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: started");

        init();
/*      setUpBottomNavigationView();
        setupToolBar();
        setupActivityWidgets();
        setProfileImage();

        tempGridSetUp();

*/
    }
    
    private void init(){
        Log.d(TAG, "init: inflating profile fragment");
        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack("Profile");
        transaction.commit();
    }
/*

    private void tempGridSetUp(){
        ArrayList<String> imgURLs = new ArrayList<>();

        imgURLs.add("https://i.blogs.es/3650f5/eclipsed-moon-trail-c-chuanjin-su/450_1000.jpg");
        imgURLs.add("https://img.elcomercio.pe/files/ec_article_multimedia_gallery/uploads/2019/05/03/5ccc7ef024fcb.jpeg");
        imgURLs.add("https://img1.imagenesgratis.com/ig/hadas/hadas_075.jpg");
        imgURLs.add("https://ep01.epimg.net/elpais/imagenes/2019/07/26/album/1564160162_283938_1564162123_noticia_normal.jpg");
        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTutuQoB4tewc6laAIMCFf69wzs0vIUcl6gPwxnXr8JSPTGSZP3wg");
        imgURLs.add("https://ep01.epimg.net/elpais/imagenes/2018/12/14/album/1544777592_679099_1544990800_noticia_normal.jpg");
        imgURLs.add("https://estaticos.muyinteresante.es/media/cache/760x570_thumb/uploads/images/gallery/5ab105745cafe8eb5eadc7ae/myanmar.jpg");
        imgURLs.add("https://image.shutterstock.com/image-photo/funny-cat-ophthalmologist-appointmet-squinting-260nw-598805597.jpg");
        imgURLs.add("https://funnyand.com/wp-content/uploads/2014/08/Funny-Little-Baby-300x300.jpg");
        imgURLs.add("https://www.nzherald.co.nz/resizer/xew4JvzOmJvxrpuPC2cR54aAFoc=/360x384/filters:quality(70)/arc-anglerfish-syd-prod-nzme.s3.amazonaws.com/public/YRPX2TUKLZDRJDH4LKRDCR5UIE.jpg");
        imgURLs.add("https://370g431nca8u23kfvb3cilkf-wpengine.netdna-ssl.com/wp-content/uploads/2013/05/6a0133f30ae399970b0192aa1b4c77970d-800wi.jpg");

        setupImageGrid(imgURLs);
    }
    private void setupImageGrid(ArrayList<String> imgURLs){
        GridView gridView = findViewById(R.id.gridview);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;

        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imgURLs);
        gridView.setAdapter(adapter);

    }

    private void setProfileImage(){
        Log.d(TAG, "setProfileImage: setting profile photo.");
        String imgURL = "encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT3QNE18z11cbTYEJ02H0Lr3dPTRGr_9WqQXOhRz5MUMhNq3KIs";
        UniversalImageLoader.setImage(imgURL, profilePhoto, mProgressBar, "https://");

    }

    private void setupActivityWidgets(){
        mProgressBar = (ProgressBar)findViewById(R.id.profileProgressBar);
        mProgressBar.setVisibility(View.GONE);
        profilePhoto = (ImageView) findViewById(R.id.profile_photo);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);

        ImageView profileMenu = (ImageView) findViewById(R.id.profileMenu);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating to account settings");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
    }
    */
/**
     * BottomNavigationView setUp
     *//*

    private void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavBar);
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }
*/

}
