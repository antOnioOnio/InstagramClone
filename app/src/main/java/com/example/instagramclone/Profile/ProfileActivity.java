package com.example.instagramclone.Profile;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.instagramclone.R;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.utils.ViewCommentFragment;
import com.example.instagramclone.utils.ViewPostFragment;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener ,
        ViewPostFragment.OnCommentThreadSelectedListener {
    private static final String TAG = "ProfileActivity";

    //------------interface methods-------------------------
    @Override
    public void OnCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "OnCommentThreadSelectedListener: selected a comment thread");

        ViewCommentFragment fragment = new ViewCommentFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comment_fragment));
        transaction.commit();


    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: selected an image gridview " + photo.toString());

        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number),activityNumber );

        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    //-------------------------------------------------------------------------

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

    }
    
    private void init(){
        Log.d(TAG, "init: inflating profile fragment");
        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack("Profile");
        transaction.commit();
    }


}
