package com.example.anhong_gyeong.fcm_android;




import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitService {
    @Headers("Content-Type: application/json")
    @POST("infos/gps/")
    Observable<RetrofitRepo> postGps(@Body String body);

    @Headers("Content-Type: application/json")
    @POST("fcm/")
    Observable<RetrofitRepo> postFcm(@Body String body);

    /*
    @GET("users")
    Call<List<RetrofitRepo>> getUserID();
    */
}
