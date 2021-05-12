package com.guyson.smartmirror.util;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.guyson.smartmirror.FacialRecognitionActivity;
import com.guyson.smartmirror.LocationActivity;
import com.guyson.smartmirror.NewsActivity;
import com.guyson.smartmirror.R;
import com.guyson.smartmirror.TwitterActivity;
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
            case R.id.news_nav: {
                //News
                Intent intent = new Intent(context, NewsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            }
            case R.id.location_nav: {
                //Location
                Intent intent = new Intent(context, LocationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                break;
            }
            case R.id.happy_nav: {
                //Configure happy
                Intent intent = new Intent(context, TwitterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("configurableEmotion", "happy");
                context.startActivity(intent);
                break;
            }
            case R.id.sad_nav: {
                //Configure sad
                Intent intent = new Intent(context, TwitterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("configurableEmotion", "sad");
                context.startActivity(intent);
                break;
            }
            case R.id.neutral_nav: {
                //Configure neutral
                Intent intent = new Intent(context, TwitterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("configurableEmotion", "neutral");
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
