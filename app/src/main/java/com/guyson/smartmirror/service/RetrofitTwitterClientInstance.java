package com.guyson.smartmirror.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**Singleton class to initialize retrofit instance**/
public class RetrofitTwitterClientInstance {

    private static Retrofit retrofit;
    private static final String BASE_URL = "https://api.twitter.com/2/";

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