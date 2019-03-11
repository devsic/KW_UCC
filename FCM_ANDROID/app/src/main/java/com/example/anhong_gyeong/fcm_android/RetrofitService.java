package com.example.anhong_gyeong.fcm_android;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitService {

    @Headers("Content-Type: application/json")
    @POST("infos/gps/")
    Call<RetrofitRepo> postGps(@Body String body);

    /*
    @GET("users")
    Call<List<RetrofitRepo>> getUserID();
    */
}
