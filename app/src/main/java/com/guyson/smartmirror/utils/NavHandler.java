package com.guyson.smartmirror.utils;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.guyson.smartmirror.FacialRecognitionActivity;
import com.guyson.smartmirror.R;
import com.guyson.smartmirror.UserActivity;

public class NavHandler {

    public static void handleNav(MenuItem item, Context context, FirebaseAuth auth) {
        switch (item.getItemId()) {

            case R.id.setup_fr: {
                //Setup face recognition
                Intent intent = new Intent(context, FacialRecognitionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            }
            case R.id.home: {
                //Home
                Intent intent = new Intent(context, UserActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            }
            case R.id.logout: {
                auth.signOut();
                break;
            }
        }
    }

}
