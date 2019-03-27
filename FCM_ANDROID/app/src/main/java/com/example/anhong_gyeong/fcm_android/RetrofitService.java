package com.example.anhong_gyeong.fcm_android;




import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitService {
    // body : parameter로 Json 객체를 전달.
    @Headers("Content-Type: application/json")
    @POST("infos/gps/")
    Observable<Retrofit> postGps(@Body String body);

    @Headers("Content-Type: application/json")
    @POST("fcm/")
    Call<RetrofitRepo> postFcm(@Body String body);

    /*
    @GET("users")
    Call<List<RetrofitRepo>> getUserID();
    */
}
