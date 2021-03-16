package com.guyson.smartmirror.service;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserClient {

    @Multipart
    @POST("face-setup/{uid}")
    Call<ResponseBody> setupFaceRecognition(@Part List<MultipartBody.Part> files, @Path("uid") String uid);


}
