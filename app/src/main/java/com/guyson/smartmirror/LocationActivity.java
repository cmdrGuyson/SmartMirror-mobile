package com.guyson.smartmirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.guyson.smartmirror.model.LocationCity;
import com.guyson.smartmirror.model.User;
import com.guyson.smartmirror.util.NavHandler;

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
    private Button mButton;

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

        mButton = findViewById(R.id.confirm_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmLocation();
            }
        });

    }

    //Populate dropdown with list of cities
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

        // If user has already configured location show as selected

        //Firebase database Reference to current user's User object
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(uid);

        //Show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        //Get user object
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(user != null && cities != null) {
                    int locationId = user.getLocationId();
                    if (locationId != -1 ){
                        // Find and set location
                        for(int i=0; i<cities.size(); i++) {
                            if(cities.get(i).getId() == locationId){
                                cityDropdown.setText(cities.get(i).getName()+" ("+cities.get(i).getCountry()+")");
                                break;
                            }
                        }
                    }
                }
                else {
                    Toast.makeText(LocationActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LocationActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void confirmLocation() {
        String location = cityDropdown.getText().toString();

        //Validate input
        if(TextUtils.isEmpty(location) || !cities_strings.contains(location)) {
            Toast.makeText(this, "Please select valid city from dropdown", Toast.LENGTH_SHORT).show();
        }else {
            //Get selected city id
            int index = cities_strings.indexOf(location);
            int id = cities.get(index).getId();

            //Update user in database

            // Get reference to user object
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("user").child(uid).child("locationId");

            //Set "Configured Face Recognition" to true
            reference.setValue(id);

            Toast.makeText(LocationActivity.this, "Successfully configured location!", Toast.LENGTH_SHORT).show();

        }
    }

    // Read JSON file of cities from assets and create string
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