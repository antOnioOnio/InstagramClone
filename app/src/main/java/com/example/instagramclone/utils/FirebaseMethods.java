package com.example.instagramclone.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.instagramclone.Home.HomeActivity;
import com.example.instagramclone.Profile.AccountSettingsActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.models.Photo;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;

    private FirebaseUser mFirebaseUser;

    private FirebaseDatabase mFirebaseDatabase;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference myRef;

    private Context mContext;

    private String userId;

    private StorageReference mStorageReference;

    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context context){
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser() != null){
            userId = mAuth.getCurrentUser().getUid();
        }
    }

    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;

        for(DataSnapshot ds: dataSnapshot
                            .child(mContext.getString(R.string.db_user_photos))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .getChildren()){
            count++;

        }
        return  count;
    }

    public void uploadNewPhoto(String photoType, final String caption, int count, String imgURL, Bitmap bm){
        Log.d(TAG, "uploadNewPhoto: attemting to upload a new photo ");

        FilePaths filePaths = new FilePaths();
        //case1--> new photo
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading a new Photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count+1)) ;

            // convert image url to a bitmap
            if ( bm == null){
                bm = ImageManager.getBitmap(imgURL);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm,100 );


            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri firebaseURL = uri.getResult();

                    Log.d(TAG, "onSuccess: URI_---> " + firebaseURL);


                    Toast.makeText(mContext, "Toast upload success", Toast.LENGTH_SHORT).show();

                    // add the new photo to 'photos' node and 'user_photos' node
                    addPhotoToDatabase(caption, firebaseURL.toString());


                    // navigate to the main feed so the user can see their photo
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    Toast.makeText(mContext, "photo upload failed" , Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    
                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress" + String.format("%.0f", progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress " + progress + "% done");
                }
            });
        }
        //case2--> new profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading a new profile photo");




            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo" ) ;

            // convert image url to a bitmap
            if ( bm == null){
                bm = ImageManager.getBitmap(imgURL);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm,100 );


            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uri.isComplete());
                    Uri firebaseURL = uri.getResult();

                    Log.d(TAG, "onSuccess: URI_---> " + firebaseURL);


                    Toast.makeText(mContext, "Toast upload success", Toast.LENGTH_SHORT).show();


                    // insert into 'user_account_settings ' node
                    setProfilePhoto(firebaseURL.toString());

                    // we have to set the viewpageradapter to go to the correct place
                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).pagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );




                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    Toast.makeText(mContext, "photo upload failed" , Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress" + String.format("%.0f", progress), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress " + progress + "% done");
                }
            });


        }

    }


    private void setProfilePhoto(String url){

        Log.d(TAG, "setProfilePhoto: setting new profile image");

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);


    }



    public String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.FRENCH);
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));

        return sdf.format(new Date());
    }

    public void addPhotoToDatabase(String caption, String url){
        Log.d(TAG, "addPhotoToDatabase: adding photo to database");

         String tags = StringManipulation.getTags(caption);
         String newPhotoKey = myRef.child(mContext.getString(R.string.db_photos)).push().getKey();
         Photo photo = new Photo();
         photo.setCaption(caption);
         photo.setDate_created(getTimeStamp());
         photo.setImage_path(url);
         photo.setTags(tags);
         photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
         photo.setPhoto_id(newPhotoKey);

         // insert into database

        myRef.child(mContext.getString(R.string.db_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.db_photos)).child(newPhotoKey).setValue(photo);

    }




    public void registerNewEmail(String email, String password, String username){
        Log.d(TAG, "registerNewEmail: started");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            // sending verification email
                            sendVerificationEmail();

                            Log.d(TAG, "createUserWithEmail:success");
                            userId = mAuth.getCurrentUser().getUid();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();


                        }

                        // ...
                    }
                });
    }

    public void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if ( user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                            }else{
                                Toast.makeText(mContext, "couldn´t send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // adding information to the using node and user account settings node
    public void addNewUser(String email, String username, String description, String website, String profile_photo){

        Log.d(TAG, "addNewUser: adding a new user to the database");
        User user = new User (email , 1, userId , StringManipulation.condensUsername(username) );

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .setValue(user);


        UserAccountSettings userAccountSettings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condensUsername(username),
                website,
                userId);


        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .setValue(userAccountSettings);


    }


    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for(DataSnapshot ds : dataSnapshot.getChildren()){

            Log.d(TAG, "getUserAccountSettings: datasnapshot: "+ ds);
            if( ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))){
                try{
                    settings.setProfile_photo(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setUsername(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setDisplay_name(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setDescription(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setWebsite(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setFollowers(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()
                    );
                    settings.setFollowing(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );
                    settings.setPosts(
                            ds.child(userId)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );

                    Log.d(TAG, "getUserAccountSettings: INFORMATION RECIEVED SETTINGS "+ settings.toString());
                }catch(NullPointerException e){
                    Log.e(TAG, "getUserAccountSettings: NullPointerException"+ e.getMessage() );
                }
            }

            if( ds.getKey().equals(mContext.getString(R.string.dbname_users))){

                try{
                    user.setUsername(
                            ds.child(userId)
                                    .getValue(User.class)
                                    .getUsername()
                    );
                    user.setEmail(
                            ds.child(userId)
                                    .getValue(User.class)
                                    .getEmail()
                    );
                    user.setPhone_number(
                            ds.child(userId)
                                    .getValue(User.class)
                                    .getPhone_number()
                    );
                    user.setUser_id(
                            ds.child(userId)
                                    .getValue(User.class)
                                    .getUser_id()
                    );
                }catch(NullPointerException e){
                    Log.e(TAG, "getUserAccountSettings: NullPointerException"+ e.getMessage() );
                }

                Log.d(TAG, "getUserAccountSettings: INFORMATION RECIEVED USER "+ user.toString());
            }


        }

        return new UserSettings(user, settings);
    }


    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: updating username to: " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

    }

    public void updateEmail(String email){
        Log.d(TAG, "updateEmail: updating email to: " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);

    }

    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber){
        Log.d(TAG, "updateUserAccountSettings: updating user account settings");

        if(displayName!= null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }

        if(website!= null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if(description!= null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if(phoneNumber!= 0){
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(userId)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }



    }






}
