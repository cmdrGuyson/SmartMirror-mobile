package com.guyson.smartmirror.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.guyson.smartmirror.FacialRecognitionActivity;
import com.guyson.smartmirror.R;
import com.guyson.smartmirror.TwitterActivity;
import com.guyson.smartmirror.model.TwitterArticle;
import com.guyson.smartmirror.service.RetrofitClientInstance;
import com.guyson.smartmirror.service.RetrofitClientInstanceTwitter;
import com.guyson.smartmirror.service.TwitterClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTwitterFragment extends DialogFragment {

    private TextInputEditText mUsernameEditText, mDescriptionEditText;
    private MaterialButton mButton;
    private ProgressBar mProgressBar;

    private List<TwitterArticle> articles;
    private String uid;
    
    private boolean customArticlesLoaded, systemArticlesLoaded, hasErrors;

    private final TwitterClient twitterClient = RetrofitClientInstanceTwitter.getRetrofitInstance().create(TwitterClient.class);

    //Database reference to get custom articles
    DatabaseReference custom_ref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_twitter, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle("Add Custom Twitter");

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        setHasOptionsMenu(true);

        //Get user
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        custom_ref = FirebaseDatabase.getInstance().getReference().child("custom_tweets").child(uid);

        //Initialize articles
        articles = new ArrayList<>();

        //Initialize Views
        mUsernameEditText = rootView.findViewById(R.id.input_username);
        mDescriptionEditText = rootView.findViewById(R.id.input_description);
        mButton = rootView.findViewById(R.id.add_button);
        mProgressBar = rootView.findViewById(R.id.progressbar);

        //Get existing twitter profiles
        getExistingArticles();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSubmit();
            }
        });

        return rootView;
    }

    private void getExistingArticles() {

        //Database reference to get system articles
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("tweets");

        //Show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        //Get system twitter articles (feeds)
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    TwitterArticle article = postSnapshot.getValue(TwitterArticle.class);
                    articles.add(article);
                }
                systemArticlesLoaded = true;
                if(systemArticlesLoaded && customArticlesLoaded) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if(!hasErrors) mButton.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                Toast.makeText(getActivity(), "Something went wrong while getting articles!", Toast.LENGTH_SHORT).show();
                systemArticlesLoaded = true;
                hasErrors = true;
                if(systemArticlesLoaded && customArticlesLoaded) mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        //Get custom twitter articles (feeds)
        custom_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    TwitterArticle article = postSnapshot.getValue(TwitterArticle.class);
                    articles.add(article);
                }
                customArticlesLoaded = true;
                if(systemArticlesLoaded && customArticlesLoaded) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if(!hasErrors) mButton.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                Toast.makeText(getActivity(), "Something went wrong while getting custom articles!", Toast.LENGTH_SHORT).show();
                customArticlesLoaded = true;
                hasErrors = true;
                if(systemArticlesLoaded && customArticlesLoaded) mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void handleSubmit() {
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mButton.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

        final String username = mUsernameEditText.getText().toString().trim();
        final String description = mDescriptionEditText.getText().toString().trim();

        //Validate user input
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(description)) {
            Toast.makeText(getActivity(), "Please enter valid input!", Toast.LENGTH_SHORT).show();
        }
        else if(username.trim().split("\\s+").length != 1) {
            Toast.makeText(getActivity(), "Please enter valid username!", Toast.LENGTH_SHORT).show();
        }
        else if(isFeedExisting(username)) {
            Toast.makeText(getActivity(), "This twitter feed is already in the system!", Toast.LENGTH_SHORT).show();
        }
        else {

            //Show progress
            mProgressBar.setVisibility(View.VISIBLE);

            //Bearer token
            String token = "Bearer " + getActivity().getResources().getString(R.string.twitter_token);

            //Check if twitter feed exists
            Call<ResponseBody> call = twitterClient.getUser(token, username);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()) {

                        //Check if response contains user
                        try {

                            JSONObject obj = new JSONObject(response.body().string());
                            if (obj.getJSONObject("data") != null) {

                                String pushID = custom_ref.push().getKey();

                                //Add feed to database
                                TwitterArticle article = new TwitterArticle();
                                article.setType("Custom");
                                article.setKeyword(username);
                                article.setTitle("@"+username);
                                article.setDescription(description);
                                article.setImageUrl("https://firebasestorage.googleapis.com/v0/b/smartmirror-c227b.appspot.com/o/twitter.jpg?alt=media&token=5f0c1f80-bb0b-4399-96bc-fd02222faef4");
                                article.setUid(pushID.substring(1));

                                custom_ref.child(pushID).setValue(article);

                                Toast.makeText(getActivity(), "Successfully added!", Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                                dismiss();

                            } else {
                                Toast.makeText(getActivity(), "This feed does not exist!", Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }



                        }catch(Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "This feed does not exist!", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }

                    }else{
                        Toast.makeText(getActivity(), "Something failed while searching twitter!", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(getActivity(), "Something went wrong while searching twitter!", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            });

        }

    }

    private boolean isFeedExisting(String handle) {
        //Check whether feed is already existing in system
        for (TwitterArticle article : articles) {
            if (article.getKeyword().toLowerCase().equals(handle.toLowerCase())) return true;
        }
        return false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.add_twitter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_close) {
            dismiss();
            return true;
        } else if (id == android.R.id.home) {
            dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}