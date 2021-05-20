package com.guyson.smartmirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.guyson.smartmirror.adapter.NewsAdapter;
import com.guyson.smartmirror.adapter.TwitterAdapter;
import com.guyson.smartmirror.fragment.AddTwitterFragment;
import com.guyson.smartmirror.model.NewsArticle;
import com.guyson.smartmirror.model.TwitterArticle;
import com.guyson.smartmirror.model.User;
import com.guyson.smartmirror.util.NavHandler;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TwitterActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private RecyclerView recyclerView;
    private TwitterAdapter twitterAdapter;
    private SearchView searchView;
    private ProgressBar mProgressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private String configurableEmotion;
    private String uid;
    private User user;

    private List<TwitterArticle> twitterArticles, customArticles, systemArticles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        // Setup Authorization
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Login"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(TwitterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };

        if(firebaseAuth.getCurrentUser()!=null){
            uid = firebaseAuth.getCurrentUser().getUid();
        }

        mProgressBar = findViewById(R.id.progressbar);

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

        //Identify which emotion to be configured
        getConfigureType();

        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        twitterAdapter = new TwitterAdapter(this, twitterArticles, user, configurableEmotion);
        recyclerView.setAdapter(twitterAdapter);

        //Firebase database Reference to current user's User object
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseAuth.getCurrentUser().getUid());

        //Show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        //Initialize lists
        systemArticles = new ArrayList<>();
        customArticles = new ArrayList<>();
        twitterArticles = new ArrayList<>();

        //Get user object
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if(user!=null) {
                    twitterAdapter.setUser(user);
                    getAllTwitterArticles();
                }
                else {
                    Toast.makeText(TwitterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TwitterActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    //Identify which emotion is to be configured
    private void getConfigureType() {

        Intent intent = getIntent();
        configurableEmotion = intent.getStringExtra("configurableEmotion");

        //Change title
        if(configurableEmotion.equals("happy")){
            mToolbar.setTitle("Configure Happy");
        }else if(configurableEmotion.equals("sad")){
            mToolbar.setTitle("Configure Sad");
        }else{
            mToolbar.setTitle("Configure Neutral");
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Handle side drawer navigation
        NavHandler.handleNav(item, TwitterActivity.this, firebaseAuth);

        //close navigation drawer
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void getAllTwitterArticles() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("tweets");

        //Show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<TwitterArticle> list = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    TwitterArticle article = postSnapshot.getValue(TwitterArticle.class);
                    list.add(article);
                }
                systemArticles = list;

                List<TwitterArticle> temp_list = new ArrayList<>();
                temp_list.addAll(systemArticles);
                temp_list.addAll(customArticles);

                twitterArticles = temp_list;
                twitterAdapter.setArticles(twitterArticles);
                mProgressBar.setVisibility(View.INVISIBLE);

                getCustomArticles();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                Toast.makeText(TwitterActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void getCustomArticles() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("custom_tweets").child(uid);

        //Show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<TwitterArticle> list = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    TwitterArticle article = postSnapshot.getValue(TwitterArticle.class);
                    list.add(article);
                }
                customArticles = list;

                List<TwitterArticle> temp_list = new ArrayList<>();
                temp_list.addAll(systemArticles);
                temp_list.addAll(customArticles);

                twitterArticles = temp_list;
                twitterAdapter.setArticles(twitterArticles);
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                Toast.makeText(TwitterActivity.this, "Something went wrong while getting custom articles!", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                twitterAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                twitterAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.add) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            AddTwitterFragment fragment = new AddTwitterFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, fragment).addToBackStack(null).commit();
        }

        return super.onOptionsItemSelected(item);
    }
}