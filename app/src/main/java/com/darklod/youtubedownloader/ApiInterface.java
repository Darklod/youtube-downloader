package com.darklod.youtubedownloader;

/**
 * Created by giuse on 09/09/2017.
 */

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("search")
    Call<SearchInfo> getVideosByKeyword(@Query("part") String part,
                                        @Query("q") String keyword,
                                        @Query("maxResults") int maxResults,
                                        @Query("key") String apiKey);
}