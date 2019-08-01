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


/**
 * postGPS -> service로 뺄 것.
 *
 * lambda로 코드 깔끔하게 수정할 것.
 */
public class MainActivity extends AppCompatActivity {
    Button buttonGps,buttonFcm;
    TextView textViewSubScore,textViewDistance,textViewTop,textviewDemoScore,textviewSubdistance;
    Retrofit retrofit;
    RetrofitService service;
    CompositeDisposable myCompositeDisposable;
    ArrayList<String> beaconList;
    String bId;
    MediaPlayer player;
    float scoreHighway,scoreGeneral,finalScore;
    float currentSpeed = 0;
    double accel = 0;
    // 위도, 경도
    double longitude, latitude;
    ImageView imageArrow;

    short mDegree;
    double lAccX,lAccY,lAccZ;
    ///
    TextView textViewSpeed,textViewGps,textViewAccel;
    ///
    ConstraintLayout layOut;
    ///
    boolean flag,sleepFlag,otherSleepFlag;
    ///
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ////////////////

        buttonGps = findViewById(R.id.button_gps);
        buttonFcm = findViewById(R.id.button_fcm);

        textViewSubScore = findViewById(R.id.textview_subscore);
        textViewDistance = findViewById(R.id.textview_distance);
        textViewTop = findViewById(R.id.textview_top);

        //////////////
        textViewSpeed = findViewById(R.id.textview_speed);
        textViewGps = findViewById(R.id.textview_temp_gps);
        textViewAccel = findViewById(R.id.textview_accel);
        textviewDemoScore = findViewById(R.id.textview_demo_score);
        textviewSubdistance = findViewById(R.id.textview_subdistance);
        ////////////////
        imageArrow = findViewById(R.id.image_arrow);

        player = MediaPlayer.create(this,R.raw.beep);

        myCompositeDisposable = new CompositeDisposable();
        beaconList = new ArrayList<>();

        /////////////////
        scoreHighway = 100;
        scoreGeneral = 100;
        finalScore = 100;
        currentSpeed = 0;
        accel = 0;
        /////////////////
        layOut = findViewById(R.id.backGroundLayout);
        /////////////////
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
                            if ( Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                                ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, 0 );
                                application.enableService();
                            }
                            else{
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


        buttonGps.setOnClickListener(v -> {
            player.start();
            textViewTop.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
            textViewTop.startAnimation(anim);

            /**
             * 화면 번쩍임
             */
            Animation backgroundAnim = new AlphaAnimation(0.0f, 1.0f);
            backgroundAnim.setDuration(50); //You can manage the time of the blink with this parameter
            backgroundAnim.setStartOffset(20);
            backgroundAnim.setRepeatMode(Animation.REVERSE);
            backgroundAnim.setRepeatCount(10);
            //backgroundAnim.setRepeatCount(Animation.INFINITE);
            layOut.startAnimation(backgroundAnim);

        });
        /**
         * 자동화 해놨으므로 지워줘도 됨.
         */
        // fcm call. List에 존재하는 비콘 id로 다 보내고 list clear.
        buttonFcm.setOnClickListener(v -> {
            //PostFcmData();

            SharedPreferences prefs = getSharedPreferences("RefreshedPreference", MODE_PRIVATE);
            String refreshedToken = prefs.getString("RefreshedToken", "");
            Log.d("RefreshedPreference",refreshedToken);
            /**
             * PostFcmData는 버튼 클릭이 아닌, ReceiveFlag()함수에서 call해줄것임. 이건 데모용.
             * "Score"에는 스코어링한 data가 들어갈 것.
             * 지금은 비콘이 3개라서 i%3해준것.
             */
            int tempSize = beaconList.size();
            for(int i=0; i<tempSize; i++) {
                  PostFcmData(beaconList.get(i),refreshedToken,"Score");
            }
            //list.clear() 해줘야 함.
            //textViewFcm.setText(refreshedToken);
            //ReceiveFlag();
        });
        ReceiveSleepFlag();
        ReceiveSensorData();
        ReceiveAccelerometer();
        // fcm Message 받았을 때 main에서의 동작 구현.
        ReceiveFcm();
        // beacon 범위내에 들어올 때 beaconId list에 저장.
        SaveBeaconId();

        SharedPreferences prefs = getSharedPreferences("RefreshedPreference", MODE_PRIVATE);
        String refreshedToken = prefs.getString("RefreshedToken", "");
        postGpsData(refreshedToken);

    }

    public void initRetrofit(){
        retrofit = new Retrofit.Builder()
                //10.20.24.87


                //192.168.200.168
                //.baseUrl("http://10.20.24.87:8080/")
                //.baseUrl("http://192.168.200.122:8080/")
                .baseUrl("http://192.168.0.19:8080/") // ucc iptime
                //.baseUrl("http://192.168.43.82:8080/") // 3189
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RetrofitService.class);

    }
    public void ReceiveAccelerometer(){
        myCompositeDisposable.add(AccelerService.getAccelerObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<SensorEvent>() {
                    @Override
                    public void onNext(SensorEvent sensorEvent) {

                        // .setText(sensorEvent.toString());

                        lAccX = sensorEvent.values[0];
                        lAccY = sensorEvent.values[1];
                        lAccZ = sensorEvent.values[2];

                        lAccX = Math.round(lAccX*100)/100.0;
                        lAccY = Math.round(lAccY*100)/100.0;
                        lAccZ = Math.round(lAccZ*100)/100.0;

                        accel = Math.sqrt((lAccX * lAccX) + (lAccY * lAccY) + (lAccZ * lAccZ));
                        accel = Math.round(accel*100)/100.0;
                        //textViewAccel.setText(lAccX+" "+lAccY+" "+lAccZ);
                        textViewAccel.setText(String.valueOf(accel));
                        Log.d("MainAccelerometer",String.valueOf(accel));

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
    public void calculateScore(){
        if(finalScore -20.0 <= 0){
            finalScore = 0;
        }
        else {
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
    public void ReceiveSleepFlag(){
        myCompositeDisposable.add(SocketService.getSocketObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<String>() {
                    /**
                     * @param s : socket으로 받은 data. 실제 구현시에는 받자마자 발행이 아닌, 데이터 처리 후 임게치 초과시에 데이터 발행.
                     *          이것 외에도, 내 스코어를 표시하기 위한 데이터 발행이 추가되어야 할듯.
                     *          위 2개는 결국 스코어링을 어떻게 하냐에 따라 달라질 듯
                     */
                    @Override
                    public void onNext(String s) {
                        //calculate flag
                        flag = true;
                        // 내 sleepFlag. fcm data에 put하여 전송할 용도.
                        sleepFlag = true;

                        calculateScore();
                        /*String fScore = String.valueOf(finalScore);
                        textviewDemoScore.setText(fScore);*/


                        // 공유 메모리. 내 아이디만 들어가 있음.
                        SharedPreferences prefs = getSharedPreferences("RefreshedPreference", MODE_PRIVATE);
                        String refreshedToken = prefs.getString("RefreshedToken", "");

                        int tempSize = beaconList.size();

                        for(int i=0; i<tempSize; i++) {
                            PostFcmData(beaconList.get(i),refreshedToken,s);
                        }
                        //list.clear() 해줘야 함.
                        /**
                         * 이 아래로 다 지워줘야 됨.
                         *//*
                        imageArrow.setRotation(250);

                        // textViewDistance : 거리
                        // textViewScore : 점수
                        textViewDistance.setText(Double.toString(51.3));
                        textViewDistance.setVisibility(View.VISIBLE);
                        textViewSubScore.setVisibility(View.VISIBLE);
                        textviewSubdistance.setVisibility(View.VISIBLE);
                        //////////


                        player.start();
                        textViewTop.setVisibility(View.VISIBLE);
                        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
                        textViewTop.startAnimation(anim);

                        *//**
                         * 화면 번쩍임
                         *//*
                        Animation backgroundAnim = new AlphaAnimation(0.0f, 1.0f);
                        backgroundAnim.setDuration(50); //You can manage the time of the blink with this parameter
                        backgroundAnim.setStartOffset(20);
                        backgroundAnim.setRepeatMode(Animation.REVERSE);
                        backgroundAnim.setRepeatCount(10);
                        //backgroundAnim.setRepeatCount(Animation.INFINITE);
                        layOut.startAnimation(backgroundAnim);*/


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
    public void ReceiveSensorData(){
        myCompositeDisposable.add(SensorSocketService.getSensorSocketObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<String>() {
                    /**
                     * @param s : socket으로 받은 data. 실제 구현시에는 받자마자 발행이 아닌, 데이터 처리 후 임게치 초과시에 데이터 발행.
                     *          이것 외에도, 내 스코어를 표시하기 위한 데이터 발행이 추가되어야 할듯.
                     *          위 2개는 결국 스코어링을 어떻게 하냐에 따라 달라질 듯
                     */
                    @Override
                    public void onNext(String s) {
                        // 온도,습도,가속도,
                        Log.d("SensorSocket_Main","Received Sensor data in MainActivity.");
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
     * ProximityContext의 변화가 있을때 마다 데이터가 발행
     */
    public void SaveBeaconId(){
        myCompositeDisposable.add(BeaconService.getBeaconObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<ProximityZoneContext[]>() {
                    @Override
                    public void onNext(ProximityZoneContext[] proximityZoneContexts) {
                        for(int i=0; i<proximityZoneContexts.length; i++) {
                            bId = proximityZoneContexts[i].getDeviceId();
                            if (!beaconList.contains(bId)) {
                                beaconList.add(bId);
                                Log.d("OnNext Beacon add: ", bId);
                                //PostFcmData(bId,FirebaseInstanceIDService.refreshedToken,"ScoreData");
                            }
                        }
                        for(int i=0; i<beaconList.size(); i++) {
                            Log.d("Beacon List "+i,beaconList.get(i));
                        }
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
     * onNext : FCM Message 받았을 때 gps값을 통하여 logic 구현.
     * FireBaseMessagingService의 onMessageReceived가 호출되어 data(Observable객체)가 발행됐을 때 subscribeWith로 구독하여 소비하는 과정.
     */
    public void ReceiveFcm(){
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
                        distance = (Math.round(distance*10000)/10000.0);

                        // 화살표 돌리는 코드
                        // imageArrow : 화살표 이미지 뷰
                        imageArrow.setRotation(mDegree);

                        // textViewDistance : 거리
                        // textViewScore : 점수
                        textViewDistance.setText(Double.toString(distance)+"M");
                        textViewDistance.setVisibility(View.VISIBLE);// 거리
                        textviewSubdistance.setVisibility(View.VISIBLE); //거리에

                        textViewSubScore.setText("위험 차량 주행중"); // 현재 주행중
                        textviewDemoScore.setVisibility(View.INVISIBLE); // 내 스코어. 데이터 받았을 때는 INVISIBLE
                        //textViewScore.setText(stringStringMap.get("score"));


                        // sleep = false or sleep = true로 전달됨.
                        // sleepFlag는 다른 차량의 졸음 신호임.
                        otherSleepFlag = Boolean.valueOf(stringStringMap.get("sleep"));
                        //Log.d("ReceiveFCM",stringStringMap.get("sleep"));
                        Log.d("RECEIVEFCM","sleepFlag: " + String.valueOf(sleepFlag));
                        /**
                         * 화면 번쩍임
                         */
                        if(otherSleepFlag) {
                            player.start();
                            textViewTop.setVisibility(View.VISIBLE);// 졸음 감지 차량!!
                            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha);
                            textViewTop.startAnimation(anim);

                            Animation backgroundAnim = new AlphaAnimation(0.0f, 1.0f);
                            backgroundAnim.setDuration(50); //You can manage the time of the blink with this parameter
                            backgroundAnim.setStartOffset(20);
                            backgroundAnim.setRepeatMode(Animation.REVERSE);
                            backgroundAnim.setRepeatCount(10);
                            //backgroundAnim.setRepeatCount(Animation.INFINITE);
                            layOut.startAnimation(backgroundAnim);
                            otherSleepFlag = false;
                        }
                        /////////////////

                        Handler delayHandler = new Handler();
                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                imageArrow.setRotation(0);

                                // textViewDistance : 거리
                                // textViewScore : 점수
                                //////
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



    /**
     * Service에서 정해진 초 or 거리의 변화가 생길시에 location changed가 call됨. 여기서 onNext로 location 객체 발행
     * 이 함수에서는 발행된 location객체에서 data를 파싱하여 서버로 post.
     * 내 token id를 가지고 gps 저장.
     */

    public void postGpsData(String userId) {
        //body에 넣을 데이터
        myCompositeDisposable.add(GpsService.getGpsObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableObserver<Location>() {
                            @Override
                            public void onNext(Location location) {
                                Log.d("postGpsData", "postGpsData_first onNext call" + location.getLatitude() + "," + location.getLongitude());
                                ///timer

                                currentSpeed = ((location.getSpeed() * 3600) / 1000);
                                String tempSpeed = String.valueOf((currentSpeed));
                                Log.d("getSpeed()", tempSpeed);
                                textViewSpeed.setText(tempSpeed);
                                calculateScore();
                                /*String fScore = String.valueOf(finalScore);
                                textviewDemoScore.setText(fScore);*/

                                // ReceiveFcm함수에서도 longitude, latitude를 사용하므로 데이터 변화가 있을시마다 저장.
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                String gps_latitude = Double.toString(latitude);
                                String gps_longitude = Double.toString(longitude);
                                String gps = gps_latitude + "," + gps_longitude;
                                //////////////
                                Log.d("GPSCALLED", gps);
                                textViewGps.setText(gps);

                                JSONObject paramObject = new JSONObject();
                                try {
                                    paramObject.put("user_id", userId);
                                    paramObject.put("gps", gps);
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
                                                Log.d("postGps", "Completed postGps");
                                            }
                                        })
                                );
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
     * FCM message를 전송해주는 함수. 비콘 ID를 list로 관리 + 서비스에서 스코어링 임계치 넘은 event가 발생시에 PostFcmData실행. 그냥 subscribe(PostFcmData())해줘도 될 듯.
     * beaconId : 상대방 beaconId. 이를 통해 서버에 저장된 target token id를 접근
     * user_id : 내 gps를 얻기 위한 token id.
     */
    public void PostFcmData(String beaconId,String userId,String score) {
        // body에 넣을 데이터
        JSONObject paramObject = new JSONObject();
        try {
            paramObject.put("beacon_id", beaconId);
            paramObject.put("user_id", userId);
            paramObject.put("score", score);
            paramObject.put("sleepFlag",sleepFlag);
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
    }
    /**
     * @function 두 gps 좌표를 입력받아 방향을 구하는 함수
     * @param P1_lat P1의 latitude
     * @param P1_lon P1의 longitude
     * @param P2_lat P2의 latitude
     * @param P2_lon P2의 longitude
     * @return P1에서 P2를 보는 방향을 360분법으로 반환
     */
    public short getDirection(double P1_lat, double P1_lon, double P2_lat, double P2_lon) {
        // 현재 위치 : gps는 지구 중심 기반 각도이기 때문에 radian 각도로 변환
        double Cur_lat_radian = P1_lat * (3.141592/180);
        double Cur_lon_radian = P1_lon * (3.141592/180);

        // 목표 위치 : 마찬가지로 radian 각도로 변환
        double Dest_lat_radian = P2_lat * (3.141592/180);
        double Dest_lon_radian = P2_lon * (3.141592/180);

        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_lat_radian)*Math.sin(Dest_lat_radian) +
                Math.cos(Cur_lat_radian)*Math.cos(Dest_lat_radian) *
                        Math.cos(Cur_lon_radian - Dest_lon_radian));

        // 목적지의 방향 ( radian)
        double radian_Direction = Math.acos((Math.sin(Dest_lat_radian) - Math.sin(Cur_lat_radian) *
                Math.cos(radian_distance)) / (Math.cos(Cur_lat_radian) * Math.sin(radian_distance)));

        // 실제 방향 구하기
        double true_D = 0;
        if(Math.sin(Dest_lon_radian - Cur_lon_radian) < 0) {
            true_D = radian_Direction * (180/3.141592);
            true_D = 360 - true_D;
        } else {
            true_D = radian_Direction * (180/3.141592);
        }

        return (short) true_D;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myCompositeDisposable != null) {
            myCompositeDisposable.clear();
        }
    }
}


