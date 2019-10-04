package com.example.instagramclone;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.utils.BottomNavigationViewHelper;
import com.example.instagramclone.utils.FirebaseMethods;
import com.example.instagramclone.utils.GridImageAdapter;
import com.example.instagramclone.utils.SquareImageView;
import com.example.instagramclone.utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.nio.BufferUnderflowException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";


    private FirebaseMethods mFirebaseMethods;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    // vars
    private Photo mPhoto;
    private int mActivityNumber=0;
    private String photoUsername;
    private String photofileUrl;
    private UserAccountSettings userAccountSettings;


    // widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationViewEx ;
    private TextView mBackLabel, mCaption, mUserName, mTimeStamp;
    private ImageView mBackArrow, mEllipses, mHeartRead, mHeartWhite, mProfileImage;



    public ViewPostFragment(){
        super();
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavBar);
        mBackArrow = (ImageView) view.findViewById(R.id.backarrow);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.caption);
        mUserName = (TextView) view.findViewById(R.id.username);
        mTimeStamp = (TextView) view.findViewById(R.id.image_time_posted);
        mEllipses = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartRead = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo);



        try{
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumberFromBundle();



        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: Nullpointer "+ e.getMessage() );
        }

        setupFirebaseAuth();
        setUpBottomNavigationView();
        getPhotoDetails();
        setUpWidgets();

        return view;
    }

    /**
     * Retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private Photo getPhotoFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.photo));
        }else {
            return null;
        }
    }
    /**
     * Retrieve the activity from the incoming bundle from profileActivity interface
     * @return
     */
    private int getActivityNumberFromBundle(){
        Log.d(TAG, "getActivityNumberFromBundle: getting activity number from bundle");

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.activity_number));
        }else {
            return 0;
        }

    }

    /**
     * to get how many days ago the pictures was taken
     * @return
     */
    private String getTimeStampDifference(){
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss",  Locale.FRENCH);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        Date today = c.getTime() ;
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp =  mPhoto.getDate_created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime()-timestamp.getTime())/1000 / 60 /60 /24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimeStampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }

        return difference;
    }

    private void getPhotoDetails(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void setUpWidgets(){
        String timestampDiff = getTimeStampDifference();
        if ( timestampDiff.equals("0")){
            mTimeStamp.setText(timestampDiff + "DAYS AGO");
        }else{
            mTimeStamp.setText("TODAY");
        }

        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), mProfileImage, null, "");
        mUserName.setText(userAccountSettings.getUsername());

    }

    private void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity() , getActivity(), bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);

    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if ( user != null){
                    Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());

                }else {
                    Log.d(TAG, "onAuthStateChanged: signed out");
                }
            }
        };


    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener) ;

    }

    @Override
    public void onStop() {
        super.onStop();
        if ( mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
