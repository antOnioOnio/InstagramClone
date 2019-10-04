package com.example.instagramclone.Profile;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.R;
import com.example.instagramclone.Share.ShareActivity;
import com.example.instagramclone.dialogs.ConfirmPasswordDialog;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.example.instagramclone.utils.FirebaseMethods;
import com.example.instagramclone.utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener{


    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password: "+ password);

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
           AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        Log.d(TAG, "onConfirmPassword: email--> " + mAuth.getCurrentUser().getEmail());

        //////////////// Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");


                            ///////////////check to see if the email is not already present in the database
                            mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if ( task.isSuccessful()){
                                        if ( task.getResult().getSignInMethods().isEmpty() ){
                                            Log.d(TAG, "onComplete: the email is available");

                                            ///////////////email is available so we update it
                                            mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d(TAG, "User email address updated.");
                                                                Toast.makeText(getActivity(), "Email updated", Toast.LENGTH_SHORT).show();
                                                                mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                            }
                                                        }
                                                    });
                                            
                                        }else {
                                            Log.d(TAG, "onComplete: the email is not available");
                                            Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }else{
                            Log.d(TAG, "onComplete: re-authenticated failed. ");
                        }
                        
                    }
                });

    }

    private static final String TAG = "EditProfileFragment";
    private String userId;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods mFirebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    // edit profile fragment widgets
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;


    // variables
    private UserSettings mUserSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUsername = (EditText) view.findViewById(R.id.username);
        mWebsite = (EditText) view.findViewById(R.id.website);
        mDescription = (EditText) view.findViewById(R.id.description);
        mEmail = (EditText) view.findViewById(R.id.email);
        mPhoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);

       // setProfileImage();

        setupFirebaseAuth();
        mFirebaseMethods = new FirebaseMethods(getActivity());

        //back arrow to navigate back to profile activity
        ImageView backarrow = (ImageView) view.findViewById(R.id.backarrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to profileActivity");
                getActivity().finish();
            }
        });

        ImageView checkmarck = (ImageView) view.findViewById(R.id.saveChanges);
        checkmarck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Attempting to save changes");
                saveProfileSettings();
            }
        });

        return view;
    }


    // saving data into database, needs to check first if the username is unique
    private void saveProfileSettings(){
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());




        //case1 --> user made a change to their username
        if (!mUserSettings.getUser().getUsername().equals(username)){
            checkIfUsernameExists(username);
        }

        //case2--> user made a change to their email
        if (!mUserSettings.getUser().getEmail().equals(email)){

            // 1-> Reaunthenticate
            //          -> confirm the password and email

            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(), getString(R.string.confirm_password));
            dialog.setTargetFragment(EditProfileFragment.this, 1);

            // 2-> check if the email is registered
            //          -fechProvidersForEmails(String email)
            // 3-> change the email
            //          -submit the new email to the database and authenticate
        }

        if(!mUserSettings.getUserAccountSettings().getDisplay_name().equals(displayName) ){
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null, 0);
        }

        if(!mUserSettings.getUserAccountSettings().getWebsite().equals(website)){
            mFirebaseMethods.updateUserAccountSettings(null, website, null, 0);
        }
        if (!mUserSettings.getUserAccountSettings().getDescription().equals(description)){
            mFirebaseMethods.updateUserAccountSettings(null, null, description, 0);
        }
        if( mUserSettings.getUser().getPhone_number() != phoneNumber ){
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber);

        }







    }

    // checking if the user already exists in the database
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if "+username + "already exists");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 // this is going to retrieve a dataSnapshot only if a match is found
                if(!dataSnapshot.exists()){
                    // add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Username saved.", Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH "+ singleSnapshot.getValue(User.class).getUsername() );
                        Toast.makeText(getActivity(), "Username already exists.", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setProfileWidgets(UserSettings userSettings){
        //   Log.d(TAG, "setProfileWidgets: Setting widgets from data retrieving from database");

        mUserSettings = userSettings;

        UserAccountSettings settings = userSettings.getUserAccountSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(  userSettings.getUser().getPhone_number()) );

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                // we need to diferentiate between going to shareactivity from here and from the other, to do that we add the flags
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userId = mAuth.getCurrentUser().getUid();
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


        // this one is always listening
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



}
