package com.example.anhong_gyeong.fcm_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    Button button_gps,button_fcm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_gps = findViewById(R.id.button_gps);
        button_fcm = findViewById(R.id.button_fcm);
        button_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postGpsData();
            }
        });
        button_fcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostFcmData();
            }
        });
    }

    public void postGpsData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.82:8080/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService service = retrofit.create(RetrofitService.class);

        JSONObject paramObject = new JSONObject();
        try {
            paramObject.put("user_id", "PMJ");
            paramObject.put("gps", "android_gps_example");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Call<RetrofitRepo> call = service.postGps(paramObject.toString());
        call.enqueue(new Callback<RetrofitRepo>() {
            @Override
            public void onResponse(Call<RetrofitRepo> call, Response<RetrofitRepo> response) {
                if (response.isSuccessful()) {
                    //Log.d("TEST",response.body().toString());
                    // Get response body
                } else if (response.errorBody() != null) {
                    // Get response errorBody
                    try {
                        String errorBody = response.errorBody().string();
                        Log.d("onResponse", "ERROR:" + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Log.d("onResponse","ERROR:"+response.body().toString());
            }

            @Override
            public void onFailure(Call<RetrofitRepo> call, Throwable t) {/////////////여기 콜됨.
                //for getting error in network put here Toast, so get the error on network
                Log.d("OnFailure", t.getMessage());
            }
        });
    }


    public void PostFcmData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.82:8080/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService service = retrofit.create(RetrofitService.class);
        //
        JSONObject paramObject = new JSONObject();
        try {
            paramObject.put("beacon_id", "temp");
            paramObject.put("user_id", "AHK");
            paramObject.put("score", "android_fcmScore_example");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //
        Call<RetrofitRepo> call = service.postFcm(paramObject.toString());
        call.enqueue(new Callback<RetrofitRepo>() {
            @Override
            public void onResponse(Call<RetrofitRepo> call, Response<RetrofitRepo> response) {
                if (response.isSuccessful()) {
                    //Log.d("TEST",response.body().toString());
                    // Get response body
                } else if (response.errorBody() != null) {
                    // Get response errorBody
                    try {
                        String errorBody = response.errorBody().string();
                        Log.d("onResponse", "ERROR:" + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Log.d("onResponse","ERROR:"+response.body().toString());
            }

            @Override
            public void onFailure(Call<RetrofitRepo> call, Throwable t) {/////////////여기 콜됨.
                //for getting error in network put here Toast, so get the error on network
                Log.d("OnFailure", t.getMessage());
            }
        });
    }

}
        /*
        Call<List<RetrofitRepo>> call=service.getUserID();
        call.enqueue(new Callback<List<RetrofitRepo>>() {
            @Override
            public void onResponse(Call<List<RetrofitRepo>> call, Response<List<RetrofitRepo>> response) {
                //response.body() have your LoginResult fields and methods  (example you have to access error then try like this response.body().getError() )
                if (response.isSuccessful()) {
                    for(RetrofitRepo temp : response.body()) {
                        Log.d("TEST", temp.getUser().toString());
                    }
                    // Get response body
                } else if (response.errorBody() != null) {
                    // Get response errorBody
                    try {
                        String errorBody = response.errorBody().string();
                        Log.d("onResponse","ERROR:"+errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //Log.d("onResponse","ERROR:"+response.body().toString());
            }

            @Override
            public void onFailure(Call<List<RetrofitRepo>> call, Throwable t) {

            }
        });
*/

