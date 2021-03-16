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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.guyson.smartmirror.models.User;
import com.guyson.smartmirror.utils.ExtraUtilities;

public class RegisterActivity extends AppCompatActivity {


    private TextInputEditText mEmailEditText, mPasswordEditText, mConfirmPasswordEditText, mFirstNameEditText, mLastNameEditText;
    private MaterialButton mRegisterButton;
    private ProgressBar mProgressBar;
    private TextView mLoginTextView;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        mFirstNameEditText = findViewById(R.id.input_firstName);
        mLastNameEditText = findViewById(R.id.input_lastName);
        mEmailEditText = findViewById(R.id.input_email);
        mPasswordEditText = findViewById(R.id.input_password);
        mConfirmPasswordEditText = findViewById(R.id.input_confirmPassword);
        mRegisterButton = findViewById(R.id.register_button);
        mProgressBar = findViewById(R.id.progressbar);
        mLoginTextView = findViewById(R.id.tv_login);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleRegister();
            }
        });

        mLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Direct to login page
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void handleRegister() {

        //Hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mRegisterButton.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

        final String firstName = mFirstNameEditText.getText().toString().trim();
        final String lastName = mLastNameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString();
        final String confirmPassword = mConfirmPasswordEditText.getText().toString();

        //If fields are empty
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please enter valid data!", Toast.LENGTH_SHORT).show();
        }
        //If password and confirm password are different
        else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
        }
        //If email is not valid
        else if (!ExtraUtilities.isEmailValid(email)){
            Toast.makeText(this, "Please enter valid email!", Toast.LENGTH_SHORT).show();
        }
        else {

            //Show progress bar
            mProgressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        String uid = firebaseAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserReference = FirebaseDatabase.getInstance().getReference().child("user").child(uid);
                        User user = new User(uid, email, firstName, lastName);
                        currentUserReference.setValue(user);

                        Toast.makeText(RegisterActivity.this, "Successfully Registered!", Toast.LENGTH_SHORT).show();

                        mProgressBar.setVisibility(View.INVISIBLE);
                        //Redirect to home page
                        Intent homePageIntent = new Intent(RegisterActivity.this, UserActivity.class);
                        homePageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homePageIntent);
                    }else{
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                }
            });



        }
    }
}