package com.guyson.smartmirror;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.guyson.smartmirror.model.AuthenticationRequest;
import com.guyson.smartmirror.model.AuthenticationResponse;
import com.guyson.smartmirror.service.RetrofitClientInstance;
import com.guyson.smartmirror.service.UserClient;
import com.guyson.smartmirror.util.ExtraUtilities;
import com.guyson.smartmirror.util.NavHandler;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacialRecognitionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Button selectButton, captureButton;
    private ProgressBar mProgressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String uid;

    private final UserClient userClient = RetrofitClientInstance.getRetrofitInstance().create(UserClient.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial_recognition);

        // Setup Authorization
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //If user is not logged in direct user to "Login"
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(FacialRecognitionActivity.this, MainActivity.class);
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

        //Setup buttons
        selectButton = findViewById(R.id.select_button);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImages();
            }
        });

        captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImages();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Handle navigation
        NavHandler.handleNav(item, FacialRecognitionActivity.this, firebaseAuth);

        //close navigation drawer
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void captureImages() {

        //Check if permissions are setup
        if (ActivityCompat.checkSelfPermission(FacialRecognitionActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FacialRecognitionActivity.this, new String[] {Manifest.permission.CAMERA}, 200);
            return;
        }

        //Open camera activity
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
        this.startActivity(intent);

    }

    private void selectImages() {

        //Check if permissions are setup
        if (ActivityCompat.checkSelfPermission(FacialRecognitionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FacialRecognitionActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return;
        }

        if (ActivityCompat.checkSelfPermission(FacialRecognitionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FacialRecognitionActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            return;
        }

        //Open activity to select images
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK) {
            List<Bitmap> bitmaps = new ArrayList<>();

            ClipData clipData = data.getClipData();

            // If user selects less than 10 images
            if (clipData == null || clipData.getItemCount() < 10) {
                Toast.makeText(this, "Please select at least 10 images", Toast.LENGTH_SHORT).show();
            }
            else if(clipData.getItemCount() == 10) {

                for (int i=0; i<clipData.getItemCount(); i++){
                    Uri uri = clipData.getItemAt(i).getUri();
                    try (InputStream stream = getContentResolver().openInputStream(uri)) {
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);

                        //Compress and add to bitmap list
                        bitmaps.add(ExtraUtilities.reduceBitmapSize(bitmap, 501126));
                    }
                    catch(Exception e){
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                uploadFiles(bitmaps);
            }
            else{
                Toast.makeText(this, "Please select only 10 images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFiles(List<Bitmap> bitmaps) {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        List<File> files = getFilesFromBitmaps(bitmaps);

        if(files == null) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        else{

            //Add all files to multipart form data
            for (File f : files) {
                builder.addFormDataPart("files[]", f.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), f));
            }

            //Show progress bar
            mProgressBar.setVisibility(View.VISIBLE);

            //Build request body and make request
            MultipartBody requestBody = builder.build();

            //Handle submit
            handleAuthAndSubmit(requestBody);

        }
    }

    private void handleAuthAndSubmit(final MultipartBody requestBody) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter your password");

        // Set up input field
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();

                // Validate input
                if(TextUtils.isEmpty(password)) {
                    Toast.makeText(FacialRecognitionActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();
                } else {
                    //Get email
                    SharedPreferences sharedPrefs = FacialRecognitionActivity.this.getSharedPreferences("auth_preferences",Context.MODE_PRIVATE);
                    String email = sharedPrefs.getString("email", null);

                    AuthenticationRequest request = new AuthenticationRequest(email, password);

                    //Authentication call
                    Call<AuthenticationResponse> auth_call = userClient.authenticateUser(request);

                    //Enqueue call
                    auth_call.enqueue(new Callback<AuthenticationResponse>() {
                        @Override
                        public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                            // If response is successful
                            if (response.isSuccessful()) {

                                //Handle Face recognition setup request
                                handleUploadRequest(requestBody, "Bearer "+ response.body().getToken());
                            }
                            //Invalid username or password
                            else if (response.code() == 403) {
                                Toast.makeText(FacialRecognitionActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                            } else {
                                Toast.makeText(FacialRecognitionActivity.this, "There was an error while authenticating", Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                            Toast.makeText(FacialRecognitionActivity.this, "Something went wrong while authenticating", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                }



            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        builder.show();
    }

    private void handleUploadRequest(MultipartBody requestBody, String token) {
        Call<ResponseBody> call = userClient.setupFaceRecognition(token ,requestBody.parts());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Successfully added
                if (response.code()==200) {
                    // Get reference to user object
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("user").child(uid).child("configuredFaceRecognition");

                    //Set "Configured Face Recognition" to true
                    reference.setValue(true);

                    Toast.makeText(FacialRecognitionActivity.this, "Successfully setup face recognition!", Toast.LENGTH_SHORT).show();
                }
                else {

                    try {

                        // Capture an display specific messages
                        JSONObject obj = new JSONObject(response.errorBody().string());
                        Toast.makeText(FacialRecognitionActivity.this, obj.getString("error"), Toast.LENGTH_SHORT).show();

                    }catch(Exception e) {
                        Toast.makeText(FacialRecognitionActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                    }
                }
                //Hide progress bar
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(FacialRecognitionActivity.this, "Something went wrong when contacting server!", Toast.LENGTH_SHORT).show();
                //Hide progress bar
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    //Store compressed files on storage and give compressed file list for upload
    private List<File> getFilesFromBitmaps(List<Bitmap> bitmaps) {
        List<File> files = new ArrayList<>();
        Random random = new Random();

        for (int i=0; i<bitmaps.size(); i++) {

            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

            //Create a file with random name
            File file = new File(directory, random.nextInt(9999999)+ i + ".jpg");

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmaps.get(i).compress(Bitmap.CompressFormat.JPEG, 50, bos);
            byte[] bitmapData = bos.toByteArray();

            try {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
                files.add(file);
            }catch(Exception e){
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong while getting files", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        return files;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for authorization
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}