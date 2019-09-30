package com.example.instagramclone.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagramclone.Home.HomeActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.models.User;
import com.example.instagramclone.utils.FirebaseMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private FirebaseMethods mFirebaseMethods;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private Context mContext;
    private String email, username, password;
    private EditText mEmail,mUserName, mPassword;
    private TextView tvPleaseWait;
    private ProgressBar mProgressBar;
    private  Button btnRegister;
    private String append = "";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;
        mFirebaseMethods = new FirebaseMethods(mContext);
        Log.d(TAG, "onCreate: started");
        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void initWidgets(){
        Log.d(TAG, "initWidgets: initializing widgets");
        btnRegister = (Button) findViewById(R.id.btn_register);
        mEmail = (EditText)findViewById(R.id.input_email);
        mUserName = (EditText)findViewById(R.id.input_username);
        mPassword = (EditText)findViewById(R.id.input_password);
        mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar) ;
        tvPleaseWait = (TextView) findViewById(R.id.tvPleaseWait);
        mProgressBar.setVisibility(View.GONE);
        tvPleaseWait.setVisibility(View.GONE);
    }

    private void init(){
        //initialize the button for log in
        Log.d(TAG, "init: initializing register");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                username = mUserName.getText().toString();

                if(isNull(email) && isNull(password) && isNull(username) ){
                    Toast.makeText(mContext,"Rellena los campos", Toast.LENGTH_SHORT).show();

                }else{
                    Log.d(TAG, "onClick: registering a new email");
                    mProgressBar.setVisibility(View.VISIBLE);
                    tvPleaseWait.setVisibility(View.VISIBLE);

                    mFirebaseMethods.registerNewEmail(email, password, username);
                }
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
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if ( user != null){
                    Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    finish();

                }else {
                    Log.d(TAG, "onAuthStateChanged: signed out");
                }
            }
        };
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

                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH "+ singleSnapshot.getValue(User.class).getUsername() );
                        append = myRef.push().getKey().substring(3,10);
                        Log.d(TAG, "onDataChange: username already exists, appending some random string "+ append);
                    }
                }
                String mUsername="";
                mUsername = username + append;

                //add new user to the database
                mFirebaseMethods.addNewUser(email, mUsername, "", "", "" );


                Toast.makeText(mContext, "Signup succesful. Sending verification email.", Toast.LENGTH_SHORT).show();

                // we signOut untill he verify the email
                mAuth.signOut();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener) ;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if ( mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private boolean isNull( String string){
        if (string.equals("")){
            return true;
        }
        return false;
    }

}
