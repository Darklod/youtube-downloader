package com.darklod.youtubedownloader;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by giuse on 09/09/2017.
 */

public class ApiClient {
    public static final String API_KEY = "AIzaSyAfvjAcsDcf1Odj_GZe1fZCpY9FpRivgwU";
    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
