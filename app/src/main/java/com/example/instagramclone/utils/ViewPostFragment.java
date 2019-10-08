package com.example.instagramclone.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.instagramclone.R;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public interface OnCommentThreadSelectedListener{
        void OnCommentThreadSelectedListener(Photo photo);
    }

    // every time we do the interface it looks like we have to do the onAttach method
    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    private FirebaseMethods mFirebaseMethods;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StringBuilder mUsers;

    // vars
    private Photo mPhoto;
    private int mActivityNumber=0;
    private String photoUsername;
    private String photofileUrl;
    private UserAccountSettings userAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private boolean mLikedByCurrentUser;
    private String mLikesString="";




    // widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationViewEx ;
    private TextView mBackLabel, mCaption, mUserName, mTimeStamp,mLikes;
    private ImageView mBackArrow, mEllipses, mHeartRead, mHeartWhite, mProfileImage, mComment;



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
        mCaption = (TextView) view.findViewById(R.id.image_caption);
        mUserName = (TextView) view.findViewById(R.id.username);
        mTimeStamp = (TextView) view.findViewById(R.id.image_time_posted);
        mLikes = (TextView) view.findViewById(R.id.image_likes);
        mEllipses = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartRead = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo);
        mComment = (ImageView) view.findViewById(R.id.speech_bubble);

        mHeart = new Heart(mHeartWhite, mHeartRead);
        mGestureDetector = new GestureDetector( getActivity(), new GestureListener());

        try{
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumberFromBundle();
            getPhotoDetails();
            getLikesString();


        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: Nullpointer "+ e.getMessage() );
        }
        setupFirebaseAuth();
        setUpBottomNavigationView();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException " + e.getMessage() );
        }
    }

    private void getLikesString(){
        Log.d(TAG, "getLikesString: getting likes string");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.db_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                mUsers.append("");
                for ( DataSnapshot singleSnapshot : dataSnapshot.getChildren() ){

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for ( DataSnapshot singleSnapshot : dataSnapshot.getChildren() ){
                                Log.d(TAG, "onDataChange: found like "+
                                        singleSnapshot.getValue(User.class).getUsername());

                                mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(",");
                            }

                            String[] splitUsers = mUsers.toString().split(",");

                            if(mUsers.toString().contains(userAccountSettings.getUsername()+ ",")){
                                Log.d(TAG, "onDataChange: changing liked to true");
                                mLikedByCurrentUser = true;
                            }else {
                                Log.d(TAG, "onDataChange: mUsers---> "+ mUsers.toString());
                                Log.d(TAG, "onDataChange: username-->"+userAccountSettings.getUsername());
                                Log.d(TAG, "onDataChange: changing liked to false");
                                mLikedByCurrentUser = false;
                            }
                            int lenght = splitUsers.length;

                            if ( lenght >4){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", "+ splitUsers[1]
                                        + ", " +splitUsers[2]
                                        + "and "+ (splitUsers.length - 3) + " others";
                            }else{
                                switch (lenght){
                                    case 1:
                                        mLikesString = "Liked by " + splitUsers[0];
                                        break;
                                    case 2:
                                        mLikesString = "Liked by " + splitUsers[0] + " and "+ splitUsers[1];
                                        break;
                                    case 3:
                                        mLikesString = "Liked by " + splitUsers[0]
                                                + ", "+ splitUsers[1]
                                                + "and " +splitUsers[2];
                                        break;
                                    case 4:
                                        mLikesString = "Liked by " + splitUsers[0]
                                                + ", "+ splitUsers[1]
                                                + ", " +splitUsers[2]
                                                + "and "+ splitUsers[3];
                                        break;
                                }
                                Log.d(TAG, "onDataChange: likes string: " + mLikesString);
                                setUpWidgets();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                if (!dataSnapshot.exists()){
                    mLikesString= "";
                    mLikedByCurrentUser = false;
                    setUpWidgets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected you bastard ");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.db_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for ( DataSnapshot singleSnapshot : dataSnapshot.getChildren() ){

                        String keyId = singleSnapshot.getKey();
                        // case 1--> the user already liked the photo

                        if (mLikedByCurrentUser &&
                                singleSnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        ) {
                            myRef.child(getString(R.string.db_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            myRef.child(getString(R.string.db_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            mHeart.toggleLike();
                            getLikesString();

                        }

                        // case 2--> the user has not liked the photo
                        else if(!mLikedByCurrentUser){
                            // add new like
                            addNewLike();
                            break;
                        }



                    }
                    if ( !dataSnapshot.exists()){
                        // add new like
                        addNewLike();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeId = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.db_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        myRef.child(getString(R.string.db_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();

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

            return  bundle.getInt(getString(R.string.activity_number));
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
        Log.d(TAG, "getPhotoDetails: getting photo details");
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

                    Log.d(TAG, "onDataChange: useraccounts settings found \n "+ userAccountSettings.toString());
                    Log.d(TAG, "onDataChange: username--> " + userAccountSettings.getUsername());

                }
                // setUpWidgets();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void setUpWidgets() {
        Log.d(TAG, "setUpWidgets: setting widgets");
        String timestampDiff = getTimeStampDifference();
        if (timestampDiff.equals("0")) {
            mTimeStamp.setText(timestampDiff + "DAYS AGO");
        } else {
            mTimeStamp.setText("TODAY");
        }

        try {
            UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), mProfileImage, null, "");
            mUserName.setText(userAccountSettings.getUsername());
            mLikes.setText(mLikesString);
            mCaption.setText(mPhoto.getCaption());


            mBackArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: navigating back");
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            mComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Leaving a comment");
                    // we need to implement a interface to go to the correct comment thread, we need to pass the photo
                    mOnCommentThreadSelectedListener.OnCommentThreadSelectedListener(mPhoto);
                }
            });

            if (mLikedByCurrentUser) {
                Log.d(TAG, "setUpWidgets: Liked By Current User ");
                mHeartWhite.setVisibility(View.GONE);
                mHeartRead.setVisibility(View.VISIBLE);

                mHeartRead.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.d(TAG, "onTouch: red heart touch detected.");
                        return mGestureDetector.onTouchEvent(event);
                    }
                });

            } else {
                Log.d(TAG, "setUpWidgets: not liked Liked By Current User ");

                mHeartWhite.setVisibility(View.VISIBLE);
                mHeartRead.setVisibility(View.GONE);
                mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.d(TAG, "onTouch: white heart touch detected.");
                        return mGestureDetector.onTouchEvent(event);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "setUpWidgets: " + e.getMessage());
        }
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
