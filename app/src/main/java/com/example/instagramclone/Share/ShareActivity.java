package com.example.instagramclone.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.instagramclone.R;
import com.example.instagramclone.utils.BottomNavigationViewHelper;
import com.example.instagramclone.utils.Permissions;
import com.example.instagramclone.utils.SectionPagesAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private ViewPager mViewPager;
    private Context mContext = ShareActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: started");

        if (checkPermissionArray(Permissions.PERMISSIONS)){
            setupViewPager();
        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }

      //  setUpBottomNavigationView();
    }


   public boolean checkPermissionArray(String[] permissions){
       Log.d(TAG, "checkPermissionArray: checking permissions");

       for ( int i = 0; i<permissions.length ; i++){
           String check = permissions[i];
           if(!checkPermissions(check)){
               return false;
           }
       }
       return true;
   }

    /**
     * return the current tab number
     * 0 == GalleryFragment
     * 1 == PhotoFragment
     * @return
     */
   public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
   }

   private void setupViewPager(){
       SectionPagesAdapter adapter = new SectionPagesAdapter(getSupportFragmentManager());
       adapter.addFragment(new GalleryFragment());
       adapter.addFragment(new PhotoFragment());

       mViewPager = (ViewPager) findViewById(R.id.container);
       mViewPager.setAdapter(adapter);

       TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
       tabLayout.setupWithViewPager(mViewPager);

       tabLayout.getTabAt(0).setText(R.string.gallery);
       tabLayout.getTabAt(1).setText(R.string.photo);

   }

    public int getTask(){
        Log.d(TAG, "getTask: TASK--> " + getIntent().getFlags() );
        return getIntent().getFlags();
    }



   public boolean checkPermissions(String permission){
       Log.d(TAG, "checkPermissions: checkin permission " + permission);

       int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

       if ( permissionRequest != PackageManager.PERMISSION_GRANTED){
           Log.d(TAG, "checkPermissions: \n Permission was not grant for " + permission );
           return false;
       }
       Log.d(TAG, "checkPermissions: \n Permission was granted for " + permission );
    return true;
   }



    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions");

        ActivityCompat.requestPermissions(ShareActivity.this, permissions, VERIFY_PERMISSIONS_REQUEST);

    }



    /**
     * BottomNavigationView setUp
     */
    private void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavBar);
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
