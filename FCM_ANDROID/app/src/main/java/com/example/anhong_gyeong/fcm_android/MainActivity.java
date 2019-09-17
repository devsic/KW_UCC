package com.example.anhong_gyeong.fcm_android;


import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorEvent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.ProximityZoneContext;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    Button buttonGps, buttonFcm;
    TextView textViewSubScore, textViewDistance, textViewTop, textviewDemoScore, textviewSubdistance;
    Retrofit retrofit;
    RetrofitService service;
    CompositeDisposable myCompositeDisposable;
    ArrayList<String> beaconList;
    String bId;
    MediaPlayer player;
    float scoreHighway, scoreGeneral, finalScore;
    float currentSpeed = 0;
    double accel = 0;
    double longitude, latitude;
    ImageView imageArrow;
    short mDegree;
    double lAccX, lAccY, lAccZ;
    TextView textViewSpeed, textViewGps, textViewAccel;
    ConstraintLayout layOut;
    boolean flag, sleepFlag, otherSleepFlag;
    String BASE_URL = "http://192.168.0.19:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonGps = findViewById(R.id.button_gps);
        buttonFcm = findViewById(R.id.button_fcm);

        textViewSubScore = findViewById(R.id.textview_subscore);
        textViewDistance = findViewById(R.id.textview_distance);
        textViewTop = findViewById(R.id.textview_top);

        textViewSpeed = findViewById(R.id.textview_speed);
        textViewGps = findViewById(R.id.textview_temp_gps);
        textViewAccel = findViewById(R.id.textview_accel);
        textviewDemoScore = findViewById(R.id.textview_demo_score);
        textviewSubdistance = findViewById(R.id.textview_subdistance);
        imageArrow = findViewById(R.id.image_arrow);

        layOut = findViewById(R.id.background_layout);

        player = MediaPlayer.create(this, R.raw.beep);

        myCompositeDisposable = new CompositeDisposable();
        beaconList = new ArrayList<>();

        scoreHighway = 100;
        scoreGeneral = 100;
        finalScore = 100;
        currentSpeed = 0;
        accel = 0;

        flag = false;
        sleepFlag = false;
        otherSleepFlag = false;
        /**
         * Function0 : 비콘 모니터링
         * Function1 : 권한 요청. Requirement class보면 여러 권한들 존재.
         *  비콘 모니터링 시작은 서비스로 빼줘야 할 듯. Function0에 startService(Monitoring)넣어주기
         */
        final MyApplication application = (MyApplication) getApplication();
        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(this,
                        () -> {
                            Log.d("app", "requirements fulfilled");
                            // 권한 요청을 모두 확인 받은 후에 service 시작
                            // 서비스 종료 후 다시 시작되는 경우에 이미 권한은 허용돼있음. 따라서 else문을 통해 서비스 다시 시작.
                            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                                application.enableService();
                            } else {
                                application.enableService();
                            }
                            return null;
                        },
                        requirements -> {
                            Log.e("app", "requirements missing: " + requirements);
                            return null;
                        },
                        throwable -> {
                            Log.e("app", "requirements error: " + throwable);
                            return null;
                        });


        initRetrofit();

        // gps 전송을 위한 버튼이지만, 현재 demo용으로 코드 수정 해놓았음.
//        buttonGps.setOnClickListener(v -> {
//            player.start();
//            textViewTop.setVisibility(View.VISIBLE);
//            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
//            textViewTop.startAnimation(anim);
//
//            Animation backgroundAnim = new AlphaAnimation(0.0f, 1.0f);
//            backgroundAnim.setDuration(50); //You can manage the time of the blink with this parameter
//            backgroundAnim.setStartOffset(20);
//            backgroundAnim.setRepeatMode(Animation.REVERSE);
//            backgroundAnim.setRepeatCount(10);
//            layOut.startAnimation(backgroundAnim);
//
//        });

        // demo용 code
        // fcm call. List에 존재하는 비콘 id로 다 보내고 list clear.
        buttonFcm.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("RefreshedPreference", MODE_PRIVATE);
            String refreshedToken = prefs.getString("RefreshedToken", "");
            Log.d("RefreshedPreference", refreshedToken);

            int tempSize = beaconList.size();
            for (int i = 0; i < tempSize; i++) {
                PostFcmData(beaconList.get(i), refreshedToken, "Score");
            }
//            beaconList.clear();
        });

        // 졸음 flag 받은 경우
        ReceiveSleepFlag();
        // 환경 센서값 받은 경우
        ReceiveSensorData();
        // 가속도 센서값 받은 경우
        receiveAccelerometer();
        // fcm Message 받았을 때 main에서의 동작 구현.
        ReceiveFcm();
        // beacon 범위내에 들어올 때 beaconId list에 저장.
        SaveBeaconId();

        // 발급받은 fcm token id. 공유 메모리로 관리해줌.
        SharedPreferences prefs = getSharedPreferences("RefreshedPreference", MODE_PRIVATE);
        String refreshedToken = prefs.getString("RefreshedToken", "");
        postGpsData(refreshedToken);
    }
    //retrofit 초기화
    public void initRetrofit() {
        retrofit = new Retrofit.Builder()
                //10.20.24.87
                //192.168.200.168
                //192.168.200.122
                //192.168.43.82 // 3189
                .baseUrl(BASE_URL) // ucc iptime
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RetrofitService.class);

    }
    // 가속도 센서 값 받았을때의 callback
    public void receiveAccelerometer() {
        myCompositeDisposable.add(AccelerService.getAccelerObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<SensorEvent>() {
                    @Override
                    public void onNext(SensorEvent sensorEvent) {
                        lAccX = sensorEvent.values[0];
                        lAccY = sensorEvent.values[1];
                        lAccZ = sensorEvent.values[2];

                        lAccX = Math.round(lAccX * 100) / 100.0;
                        lAccY = Math.round(lAccY * 100) / 100.0;
                        lAccZ = Math.round(lAccZ * 100) / 100.0;

                        accel = Math.sqrt((lAccX * lAccX) + (lAccY * lAccY) + (lAccZ * lAccZ));
                        accel = Math.round(accel * 100) / 100.0;
                        textViewAccel.setText(String.valueOf(accel));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                })

        );
    }

    // 점수 scoring
    public void calculateScore() {
        if (finalScore - 20.0 <= 0) {
            finalScore = 0;
        } else {
            if (currentSpeed > 60.0) {
                if (accel >= 7.5) {
                    scoreHighway -= 10;
                } else {
                    if (scoreHighway < 100) {
                        scoreHighway += 0.5;
                    }
                }
                if (flag) {
                    scoreHighway -= 20;
                }
                finalScore = scoreHighway;
            } else {
                if (accel >= 7.5) {
                    scoreGeneral -= 10;
                } else {
                    if (scoreGeneral < 100) {
                        scoreGeneral += 0.5;
                    }
                }
                if (flag) {
                    scoreGeneral -= 20;
                }
                finalScore = scoreGeneral;
            }
        }
        String fScore = String.valueOf(finalScore);
        textviewDemoScore.setText(fScore);
    }
    // 수면 flag 받았을 경우
    public void ReceiveSleepFlag() {
        myCompositeDisposable.add(SocketService.getSocketObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        //calculate flag
                        flag = true;
                        // 내 sleepFlag. fcm data에 put하여 전송할 용도.
                        sleepFlag = true;

                        // sleepFlag를 받을 때 마다 점수 계산
                        calculateScore();
                        // 공유 메모리. 내 fcm token id만 저장.
                        SharedPreferences prefs = getSharedPreferences("RefreshedPreference", MODE_PRIVATE);
                        String refreshedToken = prefs.getString("RefreshedToken", "");

                        int tempSize = beaconList.size();

                        // 주위 차량에 알림.
                        for (int i = 0; i < tempSize; i++) {
                            PostFcmData(beaconList.get(i), refreshedToken, s);
                        }
                        //list.clear()
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }
    // 라즈베리 파이로부터 받아오는 센서 값에 대한 처리
    public void ReceiveSensorData() {
        myCompositeDisposable.add(SensorSocketService.getSensorSocketObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        // 온도,습도에 관한 처리부
                        Log.d("SensorSocket_Main", "Received Sensor data in MainActivity.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    // 주위 차량이 접근했을 경우에 대한 callback
    // ProximityContext의 변화로 측정한다.
    public void SaveBeaconId() {
        myCompositeDisposable.add(BeaconService.getBeaconObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<ProximityZoneContext[]>() {
                    @Override
                    public void onNext(ProximityZoneContext[] proximityZoneContexts) {
                        for (int i = 0; i < proximityZoneContexts.length; i++) {
                            bId = proximityZoneContexts[i].getDeviceId();
                            if (!beaconList.contains(bId)) {
                                beaconList.add(bId);
                            }
                        }
                        for (int i = 0; i < beaconList.size(); i++) {
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }


    // FCM의 onMessageReceived가 호출됐을 때의 callback
    // 상대방의 gps를 받아 나와 방향 및 거리를 계산해주는 함수
    public void ReceiveFcm() {
        myCompositeDisposable.add(FireBaseMessagingService.getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Map<String, String>>() {
                    @Override
                    public void onNext(Map<String, String> stringStringMap) {

                        String temp = stringStringMap.get("gps");
                        String[] gpsArray = temp.split("\\,");
                        /**
                         * 앞 2개 : 현재 내 gps
                         * 뒤 2개 : FCM messgage로 전달 받은 상대방 gps
                         * 방향을 구함.
                         */
                        double otherLatitude = Double.parseDouble(gpsArray[0]);
                        double otherLongitude = Double.parseDouble(gpsArray[1]);
                        mDegree = getDirection(latitude, longitude, otherLatitude, otherLongitude);

                        // 거리 비교를 위해 현재 gps로 location 생성
                        Location locationA = new Location("point A");
                        locationA.setLatitude(latitude);
                        locationA.setLongitude(longitude);


                        // locationB = 메시지 보낸 상대방
                        Location locationB = new Location("point B");
                        locationB.setLatitude(otherLatitude);
                        locationB.setLongitude(otherLongitude);

                        // 거리구하기
                        double distance = locationA.distanceTo(locationB);
                        distance = (Math.round(distance * 10000) / 10000.0);

                        // 화살표 돌리는 코드
                        imageArrow.setRotation(mDegree);

                        // textViewDistance : 거리
                        // textViewScore : 점수
                        textViewDistance.setText(Double.toString(distance) + "M");
                        textViewDistance.setVisibility(View.VISIBLE);// 거리
                        textviewSubdistance.setVisibility(View.VISIBLE); //거리에

                        textViewSubScore.setText("위험 차량 주행중"); // 현재 주행중
                        textviewDemoScore.setVisibility(View.INVISIBLE); // 내 스코어. 데이터 받았을 때는 INVISIBLE


                        // sleep = false or sleep = true로 전달됨.
                        // sleepFlag는 다른 차량의 졸음 신호임.
                        otherSleepFlag = Boolean.valueOf(stringStringMap.get("sleep"));

                        // 화면 flashing
                        if (otherSleepFlag) {
                            player.start();
                            textViewTop.setVisibility(View.VISIBLE);// 졸음 감지 차량!!
                            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
                            textViewTop.startAnimation(anim);

                            Animation backgroundAnim = new AlphaAnimation(0.0f, 1.0f);
                            backgroundAnim.setDuration(50); //You can manage the time of the blink with this parameter
                            backgroundAnim.setStartOffset(20);
                            backgroundAnim.setRepeatMode(Animation.REVERSE);
                            backgroundAnim.setRepeatCount(10);
                            layOut.startAnimation(backgroundAnim);
                            otherSleepFlag = false;
                        }
                        // 5초의 delay후 원상태로 복구
                        Handler delayHandler = new Handler();
                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                imageArrow.setRotation(0);
                                textViewDistance.setVisibility(View.INVISIBLE);// 거리
                                textviewSubdistance.setVisibility(View.INVISIBLE); //거리에
                                textViewTop.setVisibility(View.INVISIBLE); // 졸음 감지 차량!!!
                                textViewSubScore.setText("현재 주행중.."); // 현재 주행중
                                textviewDemoScore.setVisibility(View.VISIBLE);
                            }
                        }, 5000);

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

    // gps의 변화가 생길 때 callback.
    // 발행한 location 객체에서 data parsing 후 server로 전송.
    // 내 token id와 함께 server로 전송한다.
    public void postGpsData(String userId) {
        //body에 넣을 데이터
        myCompositeDisposable.add(GpsService.getGpsObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<Location>() {
                    @Override
                    public void onNext(Location location) {

                        currentSpeed = ((location.getSpeed() * 3600) / 1000);
                        String tempSpeed = String.valueOf((currentSpeed));
                        Log.d("getSpeed()", tempSpeed);
                        textViewSpeed.setText(tempSpeed);
                        calculateScore();

                        // ReceiveFcm함수에서도 longitude, latitude를 사용하므로 데이터 변화가 있을시마다 저장.
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        String gps_latitude = Double.toString(latitude);
                        String gps_longitude = Double.toString(longitude);
                        String gps = gps_latitude + "," + gps_longitude;

                        textViewGps.setText(gps);

                        JSONObject paramObject = new JSONObject();
                        try {
                            paramObject.put("user_id", userId);
                            paramObject.put("gps", gps);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // retrofit 호출
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
                                    }
                                })
                        );
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    /**
     *
     * @param beaconId : 주위 차량에 설치된 beacon Id. 이를 이용해 server에서 target의 token id를 찾는다.
     * @param userId : 내 fcm token id. // 서버에 저장된 gps값을 찾기 위해.
     * @param score : 내 점수
     */
    public void PostFcmData(String beaconId, String userId, String score) {
        JSONObject paramObject = new JSONObject();
        try {
            paramObject.put("beacon_id", beaconId);
            paramObject.put("user_id", userId);
            paramObject.put("score", score);
            paramObject.put("sleepFlag", sleepFlag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // sleepFlag 보내줬으므로 다시 false.
        sleepFlag = false;
        myCompositeDisposable.add(service.postFcm(paramObject.toString())
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
                    }
                })
        );
    }

    /**
     * @param P1_lat P1의 latitude
     * @param P1_lon P1의 longitude
     * @param P2_lat P2의 latitude
     * @param P2_lon P2의 longitude
     * @return P1에서 P2를 보는 방향을 360분법으로 반환
     * @function 두 gps 좌표를 입력받아 방향을 구하는 함수
     */
    public short getDirection(double P1_lat, double P1_lon, double P2_lat, double P2_lon) {
        // 현재 위치 : gps는 지구 중심 기반 각도이기 때문에 radian 각도로 변환
        double Cur_lat_radian = P1_lat * (3.141592 / 180);
        double Cur_lon_radian = P1_lon * (3.141592 / 180);

        // 목표 위치 : 마찬가지로 radian 각도로 변환
        double Dest_lat_radian = P2_lat * (3.141592 / 180);
        double Dest_lon_radian = P2_lon * (3.141592 / 180);

        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_lat_radian) * Math.sin(Dest_lat_radian) +
                Math.cos(Cur_lat_radian) * Math.cos(Dest_lat_radian) *
                        Math.cos(Cur_lon_radian - Dest_lon_radian));

        // 목적지의 방향 ( radian)
        double radian_Direction = Math.acos((Math.sin(Dest_lat_radian) - Math.sin(Cur_lat_radian) *
                Math.cos(radian_distance)) / (Math.cos(Cur_lat_radian) * Math.sin(radian_distance)));

        // 실제 방향 구하기
        double true_D = 0;
        if (Math.sin(Dest_lon_radian - Cur_lon_radian) < 0) {
            true_D = radian_Direction * (180 / 3.141592);
            true_D = 360 - true_D;
        } else {
            true_D = radian_Direction * (180 / 3.141592);
        }

        return (short) true_D;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myCompositeDisposable != null) {
            myCompositeDisposable.clear();
        }
    }
}


