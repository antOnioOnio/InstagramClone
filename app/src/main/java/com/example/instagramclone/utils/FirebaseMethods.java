package com.example.instagramclone.utils;

import android.content.Context;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.instagramclone.R;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;

    private FirebaseUser mFirebaseUser;

    private FirebaseDatabase mFirebaseDatabase;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference myRef;

    private Context mContext;

    private String userId;

    public FirebaseMethods(Context context){
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            userId = mAuth.getCurrentUser().getUid();
        }
    }
/*

     public boolean checkIfUserExists(String username, DataSnapshot dataSnapshot){
        Log.d(TAG, "checkIfUserExists: checking if "+ username + " exists");

        User user = new User();
        
        for (DataSnapshot ds : dataSnapshot.child(userId).getChildren()){
            Log.d(TAG, "checkIfUserExists: dataSnapshot " + ds);

            user.setUsername(ds.getValue(User.class).getUsername());

            if ( StringManipulation.expandUsername(user.getUsername()).equals(username) ){
                Log.d(TAG, "checkIfUserExists: FOUND A MATCH");
                return true;
                
            }
        }
        
        return false; 
    }
*/

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
                website);


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
