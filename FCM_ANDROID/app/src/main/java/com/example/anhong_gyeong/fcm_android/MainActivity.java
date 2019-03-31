package com.example.anhong_gyeong.fcm_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * postGPS -> service로 뺄 것.
 *
 * lambda로 코드 깔끔하게 수정할 것.
 */
public class MainActivity extends AppCompatActivity {
    Button buttonGps,buttonFcm;
    TextView textViewFcm;
    Retrofit retrofit;
    RetrofitService service;
    CompositeDisposable myCompositeDisposable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonGps = findViewById(R.id.button_gps);
        buttonFcm = findViewById(R.id.button_fcm);
        textViewFcm = findViewById(R.id.textview_fcm);
        myCompositeDisposable = new CompositeDisposable();
        initRetrofit();

        // gps logging. service로 뺄 것.
        buttonGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postGpsData();
            }
        });
        // fcm call. List에 존재하는 비콘 id로 다 보내고 list clear.
        buttonFcm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostFcmData();
            }
        });
        // fcm Message 받았을 때 main에서의 동작 구현.
        ReceiveFcm();

        // 권한 설정 부분 + 비콘 모니터링 시작 부분
        /**
         * Function0 : 비콘 모니터링
         * Function1 : 권한 요청. Requirement class보면 여러 권한들 존재.
         *  비콘 모니터링 시작은 서비스로 빼줘야 할 듯. Function0에 startService(Monitoring)넣어주기
         */
        final MyApplication application = (MyApplication) getApplication();
        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(this,
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                Log.d("app", "requirements fulfilled");
                                // Monitoring하여 설정한 범위로 Notification주는 함수.
                                application.enableBeaconNotifications();
                                return null;
                            }
                        },
                        new Function1<List<? extends Requirement>, Unit>() {// Requirement class까보면 권한들 있음. 즉, 이 함수는 권한 요청용 함수.
                            @Override
                            public Unit invoke(List<? extends Requirement> requirements) {
                                Log.e("app", "requirements missing: " + requirements);
                                return null;
                            }
                        },
                        new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "requirements error: " + throwable);
                                return null;
                            }
                        });
    }

    public void initRetrofit(){
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.43.82:8080/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RetrofitService.class);

    }

    /**
     * onNext : FCM Message 받았을 때 gps값을 통하여 logic 구현.
     * subscribeOn을 좀 더 고민해볼 것. FCM service가 어떻게 구현돼있는지 모르겠음. subscribeOn을 통해 새로운 스레드를 또 만들어줄 필요가 있나?
     * FireBaseMessagingService의 onMessageReceived가 호출되어 data(Observable객체)가 발행됐을 때 subscribeWith로 구독하여 소비하는 과정.
     */
    public void ReceiveFcm(){
        myCompositeDisposable.add(FireBaseMessagingService.getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Map<String, String>>() {
                    @Override
                    public void onNext(Map<String, String> stringStringMap) {
                        textViewFcm.setText(stringStringMap.get("gps"));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    /**
     * gps logging해주는 함수. Service로 만들어서 빼줄 것.
     */
    public void postGpsData() {
        //body에 넣을 데이터
        JSONObject paramObject = new JSONObject();
        try {
            paramObject.put("user_id", "PMJ");
            paramObject.put("gps", "android_gps_example2");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        myCompositeDisposable.add(service.postGps(paramObject.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<RetrofitRepo>() {
                    @Override
                    public void onNext(RetrofitRepo retrofitRepo) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Log.d("postGps","Completed postGps");
                    }
                })
        );


        // Rxjava사용하여 io thread하나 파서 하는게 나을 듯.
        /*
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
        */
    }
    /**
     * FCM message를 전송해주는 함수. 비콘 ID를 list로 관리 + 서비스에서 스코어링 임계치 넘은 event가 발생시에 PostFcmData실행. 그냥 subscribe(PostFcmData())해줘도 될 듯.
     */
    public void PostFcmData() {
        // body에 넣을 데이터
        JSONObject paramObject = new JSONObject();
        try {
            paramObject.put("beacon_id", "temp");
            paramObject.put("user_id", "PMJ");
            paramObject.put("score", "android_fcmScore_example");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        myCompositeDisposable.add(service.postFcm(paramObject.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<RetrofitRepo>() {
                    @Override
                    public void onNext(RetrofitRepo retrofitRepo) {
                        Log.d("postFCM","Completed postFCM");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Log.d("postFCM","Completed postFCM");
                    }
                })
        );

        /*
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
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myCompositeDisposable != null) {
            myCompositeDisposable.clear();
        }
    }
}


