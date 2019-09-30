package com.example.instagramclone.Login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context mContext;

    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: started");

        mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar);
        mPleaseWait = (TextView) findViewById(R.id.tvPleaseWait);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        mContext = LoginActivity.this;

        mProgressBar.setVisibility(View.GONE);
        mPleaseWait.setVisibility(View.GONE) ;

       setupFirebaseAuth();
       init();

    }

    private void init(){
        //initialize the button for log in
        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to log in");
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(isNull(email) && isNull(password) ){
                    Toast.makeText(mContext,"Rellena los campos", Toast.LENGTH_SHORT).show();
                }else{
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);

                    signIn(email,password);
                }
            }
        });

        TextView linkSingUp = (TextView) findViewById(R.id.link_signup);
        linkSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: going to register screen");
                Intent intent = new Intent(mContext, RegisterActivity.class);
                startActivity(intent);

            }
        });

        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);

            finish();
        }
    }

    private boolean isNull( String string){
        if (string.equals("")){
            return true;
        }
        return false;
    }


    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        try{
                            if(user.isEmailVerified()){
                                Log.d(TAG, "onComplete: succes, emails is verified.");
                                Intent intent = new Intent(mContext, HomeActivity.class);
                                startActivity(intent);
                            }else {
                                Toast.makeText(mContext, "Email is not verified, check your email inbox", Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                                mPleaseWait.setVisibility(View.GONE);
                                mAuth.signOut();
                            }
                        }catch(NullPointerException e){
                            Log.e(TAG, "onComplete: NullPointerException "+ e.getMessage() );
                        }




                        if (task.isSuccessful()) {
                            try{
                                if(user.isEmailVerified()){
                                    Log.d(TAG, "onComplete: succes, emails is verified.");
                                    Intent intent = new Intent(mContext, HomeActivity.class);
                                    startActivity(intent);
                                }else {
                                    Toast.makeText(mContext, "Email is not verified, check your email inbox", Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.GONE);
                                    mPleaseWait.setVisibility(View.GONE);
                                    mAuth.signOut();
                                }
                            }catch(NullPointerException e){
                                Log.e(TAG, "onComplete: NullPointerException "+ e.getMessage() );
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();


                            mProgressBar.setVisibility(View.GONE);
                            mPleaseWait.setVisibility(View.GONE);
                        }

                        // ...
                    }
                });
    }


    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
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


}
