package com.guyson.smartmirror.service;

import com.guyson.smartmirror.model.AuthenticationRequest;
import com.guyson.smartmirror.model.AuthenticationResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserClient {

    @Multipart
    @POST("face-setup")
    Call<ResponseBody> setupFaceRecognition(@Header("Authorization") String token, @Part List<MultipartBody.Part> files);

    @POST("auth")
    Call<AuthenticationResponse> authenticateUser(@Body AuthenticationRequest request);

}
