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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.R;
import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.Like;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewCommentFragment extends Fragment {

    private static final String TAG = "ViewCommentFragment";

    public ViewCommentFragment(){
        super();
        setArguments(new Bundle());
    }
    //firebase
    private FirebaseMethods mMethods = new FirebaseMethods(getActivity());
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    // vars
    private Photo mPhoto;
    private ArrayList<Comment> mComments;

    //widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        mBackArrow = (ImageView) view.findViewById(R.id.backarrow);
        mCheckMark = (ImageView) view.findViewById(R.id.ivPostComment);
        mComment = (EditText) view.findViewById(R.id.comment);
        mListView = (ListView) view.findViewById(R.id.listview);
        mComments = new ArrayList<>();



        try{
            mPhoto = getPhotoFromBundle();
            setupFirebaseAuth();
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: Nullpointer "+ e.getMessage() );
        }



        return view;
    }

    private void setupWidgets(){

        CommentListAdapter adapter = new CommentListAdapter(getActivity(), R.layout.layout_comments,mComments );
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mComment.getText().toString().equals("")){
                    Log.d(TAG, "onClick: attemting to submit new comment");
                    addNewComment(mComment.getText().toString());
                    mComment.setText("");
                    closeKeyBoard();
                }else {
                    Toast.makeText(getActivity(), "what are you doing ? empty comment ?", Toast.LENGTH_SHORT).show();
                }
            }
        });




    }
    private void closeKeyBoard(){
        View view = getActivity().getCurrentFocus();

        if (view != null){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void addNewComment(String newComment){
        Log.d(TAG, "addNewComment: adding new comment " + newComment);

        String commentID = myRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(mMethods.getTimeStamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.db_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        myRef.child(getString(R.string.db_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);
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

        myRef.child(getString(R.string.db_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Query query = myRef
                                .child(getString(R.string.db_photos))
                                .orderByChild(getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>)singleSnapshot.getValue();

                                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                                    mComments.clear();
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(mPhoto.getCaption());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getDate_created());

                                    mComments.add(firstComment);

                                    for(DataSnapshot ds : singleSnapshot
                                            .child(getString(R.string.field_comments))
                                            .getChildren()){

                                        Comment comment = new Comment();
                                        comment.setUser_id(ds.getValue(Comment.class).getUser_id());
                                        comment.setComment(ds.getValue(Comment.class).getComment());
                                        comment.setDate_created(ds.getValue(Comment.class).getDate_created());
                                        mComments.add(comment);
                                    }

                                    photo.setComments(mComments);
                                    mPhoto = photo;

                                    setupWidgets();


/*                    for(DataSnapshot ds : singleSnapshot
                            .child(getString(R.string.field_likes))
                            .getChildren()){

                        Like like = new Like();
                        like.setUser_id(ds.getValue(Like.class).getUser_id());
                        likeList.add(like);
                    }
       */

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: query cancelled");
                            }
                        });

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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


}
