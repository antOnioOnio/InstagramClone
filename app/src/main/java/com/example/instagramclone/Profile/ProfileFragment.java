package com.example.instagramclone.Profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.instagramclone.Login.LoginActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.example.instagramclone.utils.BottomNavigationViewHelper;
import com.example.instagramclone.utils.FirebaseMethods;
import com.example.instagramclone.utils.GridImageAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public interface OnGridImageSelectedListener{
        // we are using here the activityNumber cause we can access to the photo from many places and we need to know where are we coming from, leñé
        void onGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener onGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUMBER_GRID_COLUMNS = 3;

    private TextView mPost, mFollowers,mWebsite,  mFollowing,mDisplayName, mUsername, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private Context mContext;

    private FirebaseMethods mFirebaseMethods;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false );

        mDisplayName = (TextView) view.findViewById(R.id.displayNameProfile);
        mUsername = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mPost = (TextView) view.findViewById(R.id.tvPosts);
        mFollowers = (TextView) view.findViewById(R.id.tvFollowers);
        mFollowing = (TextView) view.findViewById(R.id.tvFollowing);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        gridView = (GridView) view.findViewById(R.id.gridview);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar) ;
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavBar);
        mContext = getActivity().getApplicationContext();
        mFirebaseMethods = new FirebaseMethods(getActivity());

        mProgressBar.setVisibility(View.GONE);
        setUpBottomNavigationView();
        setupToolBar();

        setupFirebaseAuth();
        setupGridView();

        Log.d(TAG, "onCreateView: started");

        TextView editProfile = (TextView) view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating to : edit profile fragment ");
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });

        return view;
    }

    private void setProfileWidgets(UserSettings userSettings){
     //   Log.d(TAG, "setProfileWidgets: Setting widgets from data retrieving from database");
       User user = userSettings.getUser();
       UserAccountSettings settings = userSettings.getUserAccountSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPost.setText(String.valueOf(settings.getPosts()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupToolBar(){

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating to account settings");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    private void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,getActivity(), bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }

    private void setupGridView(){
        Log.d(TAG, "setupGridView:");
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.db_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object> )singleSnapshot.getValue();

                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    List<Like> likeList = new ArrayList<Like>();

                    for(DataSnapshot ds : singleSnapshot
                            .child(getString(R.string.field_likes))
                            .getChildren()){

                        Like like = new Like();
                        like.setUser_id(ds.getValue(Like.class).getUser_id());
                        likeList.add(like);
                    }
                    photo.setLike(likeList);
                    photos.add(photo);

                }
                //setup our image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUMBER_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<String>();
                for (int i = 0 ; i < photos.size(); i++){
                    imgUrls.add(photos.get(i).getImage_path());
                }

                GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, "", imgUrls);
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        onGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
                // retrieve image for user
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    @Override
    public void onAttach(Context context) {
        try{
            onGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
        super.onAttach(context);
    }
}
