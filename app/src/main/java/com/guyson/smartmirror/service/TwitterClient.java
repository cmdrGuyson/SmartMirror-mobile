package com.guyson.smartmirror.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface TwitterClient {
    @GET("https://api.twitter.com/2/users/by/username/{username}")
    Call<ResponseBody> getUser(@Header("Authorization") String token, @Path("username") String username);
}
