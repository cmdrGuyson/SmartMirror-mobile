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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.guyson.smartmirror.model.LocationCity;
import com.guyson.smartmirror.util.NavHandler;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String uid;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ProgressBar mProgressBar;
    private AutoCompleteTextView cityDropdown;

    private List<String> cities_strings = new ArrayList<>();
    private List<LocationCity> cities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Setup Authorization
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Login"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(LocationActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };

        if(firebaseAuth.getCurrentUser()!=null){
            uid = firebaseAuth.getCurrentUser().getUid();
        }

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

        mProgressBar = findViewById(R.id.progressbar);

        setupDropdown();

    }

    private void setupDropdown() {

        Gson gson = new Gson();
        LocationCity[] _cities = gson.fromJson(loadJSONFromAsset(), LocationCity[].class);
        cities = Arrays.asList(_cities);

        for (LocationCity city : cities) {
            cities_strings.add(city.getName()+" ("+city.getCountry()+")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_menu_popup_item,
                cities_strings);

        cityDropdown = findViewById(R.id.city_dropdown);
        cityDropdown.setAdapter(adapter);
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Handle navigation
        NavHandler.handleNav(item, LocationActivity.this, firebaseAuth);

        //close navigation drawer
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}