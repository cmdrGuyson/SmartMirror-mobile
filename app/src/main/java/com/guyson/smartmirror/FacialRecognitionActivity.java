package com.guyson.smartmirror;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.guyson.smartmirror.service.RetrofitClientInstance;
import com.guyson.smartmirror.service.UserClient;
import com.guyson.smartmirror.utils.ExtraUtilities;
import com.guyson.smartmirror.utils.NavHandler;

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
import retrofit2.http.Multipart;

public class FacialRecognitionActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Button selectButton;

    private UserClient userClient = RetrofitClientInstance.getRetrofitInstance().create(UserClient.class);

    private int STORAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial_recognition);

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

        //Setup buttons
        selectButton = findViewById(R.id.select_button);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImages();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Handle navigation
        NavHandler.handleNav(item, FacialRecognitionActivity.this);

        //close navigation drawer
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void selectImages() {

        if (ActivityCompat.checkSelfPermission(FacialRecognitionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FacialRecognitionActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return;
        }

        if (ActivityCompat.checkSelfPermission(FacialRecognitionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(FacialRecognitionActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
        }

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

            for (File f : files) {
                builder.addFormDataPart("files[]", f.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), f));
            }

            MultipartBody requestBody = builder.build();
            Call<ResponseBody> call = userClient.setupFaceRecognition(requestBody.parts(), "Alvaro");

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(FacialRecognitionActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(FacialRecognitionActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    //Store compressed files on storage and give compressed file list for upload
    private List<File> getFilesFromBitmaps(List<Bitmap> bitmaps) {
        List<File> files = new ArrayList<>();
        Random random = new Random();

        for (int i=0; i<bitmaps.size(); i++) {

            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File file = new File(directory, random.nextInt(9999999)+ i + ".jpg");
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
}