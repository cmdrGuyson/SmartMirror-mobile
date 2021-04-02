package com.guyson.smartmirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText mEmailEditText, mPasswordEditText;
    private MaterialButton mLoginButton;
    private ProgressBar mProgressBar;
    private TextView mRegisterTextView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Authorization
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is logged in direct user to "Home"
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(MainActivity.this, UserActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };

        //Initialize views
        mEmailEditText = findViewById(R.id.input_email);
        mPasswordEditText = findViewById(R.id.input_password);
        mLoginButton = findViewById(R.id.login_button);
        mProgressBar = findViewById(R.id.progressbar);
        mRegisterTextView = findViewById(R.id.tv_register);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogin();
            }
        });

        mRegisterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Direct to register page
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {

        //Hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mLoginButton.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString();

        //If there are empty fields
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter valid data!", Toast.LENGTH_SHORT).show();
        }else{

            //Show progress bar
            mProgressBar.setVisibility(View.VISIBLE);

            //Sign in using firebase auth
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mProgressBar.setVisibility(View.INVISIBLE);

                        Toast.makeText(MainActivity.this, "Successfully signed in!", Toast.LENGTH_SHORT).show();

                        //Direct to home page
                        Intent homePageIntent = new Intent(MainActivity.this, UserActivity.class);
                        homePageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homePageIntent);

                    }else{
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for authorization
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}