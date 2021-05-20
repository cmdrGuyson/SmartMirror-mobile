package com.guyson.smartmirror.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**Singleton class to initialize retrofit instance for twitter API**/
public class RetrofitClientInstanceTwitter {

    private static Retrofit retrofit;
    private static final String BASE_URL = "http://d8dd2d6e9ae6.ngrok.io/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}