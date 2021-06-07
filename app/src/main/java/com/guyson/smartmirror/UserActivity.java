package com.guyson.smartmirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.guyson.smartmirror.model.CalendarEvent;
import com.guyson.smartmirror.model.NewsArticle;
import com.guyson.smartmirror.model.TwitterArticle;
import com.guyson.smartmirror.util.NavHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Button mSyncButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Setup Authorization
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Login"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(UserActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };

        uid = firebaseAuth.getCurrentUser().getUid();

        //Setup toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        //Setup navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle mActionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.open_nav_drawer,
                R.string.close_nav_drawer
        );

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);

        mSyncButton = findViewById(R.id.sync);

        //Sync calendar when app is open
        syncCalendar();

        mSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncCalendar();
            }
        });
    }

    private void add() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("tweets");
        TwitterArticle article = new TwitterArticle();
        article.setTitle("@NASA");
        article.setDescription("Neque gravida in fermentum et sollicitudin ac orci. Erat imperdiet sed euismod nisi porta lorem mollis aliquam. Malesuada bibendum arcu vitae elementum curabitur vitae. Amet justo donec enim diam.");
        article.setImageUrl("https://firebasestorage.googleapis.com/v0/b/smartmirror-c227b.appspot.com/o/nasa.png?alt=media&token=c8af2bf5-31ef-46e3-a933-3721b706f6c7");
        article.setKeyword("NASA");
        article.setType("Science");
        ref.push().setValue(article);
        Toast.makeText(UserActivity.this, "Added!", Toast.LENGTH_SHORT).show();
    }

    private void syncCalendar() {

        //Check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 1);
        }

        //If permissions not given
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please give calendar permissions", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.TITLE,
        };

        // The indices for the projection array above.
        final int PROJECTION_ID_INDEX = 0;
        final int PROJECTION_BEGIN_INDEX = 1;
        final int PROJECTION_TITLE_INDEX = 2;

        // Date range searched
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Calendar.HOUR_OF_DAY, 0);
        beginTime.set(Calendar.MINUTE, 0);
        long startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.MONTH, 1);
        long endMillis = endTime.getTimeInMillis();

        // Construct query with desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        // Submit query
        Cursor cur =  getContentResolver().query(builder.build(), INSTANCE_PROJECTION, null, null, null);


        List<CalendarEvent> events = new ArrayList<>();

        while (cur.moveToNext()) {

            // Get the field values
            long id = cur.getLong(PROJECTION_ID_INDEX);
            long start = cur.getLong(PROJECTION_BEGIN_INDEX);
            String title = cur.getString(PROJECTION_TITLE_INDEX);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(start);

            // Format date and time to string values
            SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm", Locale.getDefault());

            String date = DATE_FORMATTER.format(calendar.getTime());
            String time = TIME_FORMATTER.format(calendar.getTime());

            events.add(new CalendarEvent(id, date, time, title));
        }

        //Save to database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("calendar").child(uid);
        ref.setValue(events);

        Toast.makeText(this, "Calendar successfully synced!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //Handle navigation
        NavHandler.handleNav(item, UserActivity.this, firebaseAuth);

        //close navigation drawer
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for authorization
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}