package com.guyson.smartmirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.guyson.smartmirror.model.NewsArticle;
import com.guyson.smartmirror.model.TwitterArticle;
import com.guyson.smartmirror.util.NavHandler;

public class UserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Button mButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

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

        mButton = findViewById(R.id.add);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("tweets");
                TwitterArticle article = new TwitterArticle();
                article.setTitle("@Pulse_LK");
                article.setDescription("Mus mauris vitae ultricies leo integer malesuada nunc. Tempor nec feugiat nisl pretium fusce id. Eu non diam phasellus vestibulum lorem. In ante metus dictum at tempor. In egestas erat imperdiet sed euismod nisi.");
                article.setImageUrl("https://firebasestorage.googleapis.com/v0/b/smartmirror-c227b.appspot.com/o/pulse.jpg?alt=media&token=989a1873-8671-41e0-b2b5-6ef35a22c17f");
                article.setKeyword("pulse_lk");
                article.setType("Lifestyle");
                ref.push().setValue(article);
                Toast.makeText(UserActivity.this, "Added!", Toast.LENGTH_SHORT).show();
            }
        });
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