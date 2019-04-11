package com.example.anhong_gyeong.fcm_android;


import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.ProximityZoneContext;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    TextView textViewScore,textViewDistance;
    Retrofit retrofit;
    RetrofitService service;
    CompositeDisposable myCompositeDisposable;
    ArrayList<String> beaconList;
    String bId;

    // 위도, 경도
    double longitude, latitude;
    LocationManager Im;
    Location location;
    ImageView imageArrow;

    short mDegree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ////////////////

        buttonGps = findViewById(R.id.button_gps);
        buttonFcm = findViewById(R.id.button_fcm);

        textViewScore = findViewById(R.id.textview_score);
        textViewDistance = findViewById(R.id.textview_distance);
        ////////////////
        imageArrow = findViewById(R.id.image_arrow);

        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0);

            Im = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = Im.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        else {
            Im = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = Im.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //Log.d("Location",location.toString());
        }

        ////////////////////
        myCompositeDisposable = new CompositeDisposable();
        beaconList = new ArrayList<>();

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
                            application.enableService();
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

        // gps logging. service로 뺄 것.
        buttonGps.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("RefreshedPreference", MODE_PRIVATE);
            String refreshedToken = prefs.getString("RefreshedToken", "");
            postGpsData(refreshedToken);
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
             */
            for(int i=0; i<beaconList.size()*5; i++) {
                  PostFcmData(beaconList.get(i%3),refreshedToken,"Score");
            }
            //textViewFcm.setText(refreshedToken);
            //ReceiveFlag();
        });
        ReceiveFlag();
        // fcm Message 받았을 때 main에서의 동작 구현.
        ReceiveFcm();
        // beacon 범위내에 들어올 때 beaconId list에 저장.
        SaveBeaconId();
        // 권한 설정 부분 + 비콘 모니터링 시작 부분


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
    public void ReceiveFlag(){
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
                        SharedPreferences prefs = getSharedPreferences("RefreshedPreference", MODE_PRIVATE);
                        String refreshedToken = prefs.getString("RefreshedToken", "");
                        for(int i=0; i<beaconList.size(); i++) {
                            PostFcmData(beaconList.get(i),refreshedToken,s);
                        }


                        //textViewFcm.setText(refreshedToken);
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
                        /**
                         * 여기서 gps 비교해서 방향 출력해주는거 추가.
                         */
                        String temp = stringStringMap.get("gps");
                        String[] gpsArray = temp.split("\\,");
                        // 상대방의 gps와 내 위치를 비교하여 각도 설정. 지금은 서울역으로 고정.
                        mDegree = getDirection(Double.parseDouble(gpsArray[0]), Double.parseDouble(gpsArray[1]), 37.554648, 126.972559);

                        // locationA = 현재위치
                        Location locationA = new Location("point A");
//                        locationA.setLatitude(latitude);
//                        locationA.setLongitude(longitude);
                        //locationA와 차이가 뭐지.
                        locationA.setLatitude(location.getLatitude());
                        locationA.setLongitude(location.getLatitude());


                        // locationB = 서울역 위치
                        Location locationB = new Location("point B");
                        locationB.setLatitude(37.554648);
                        locationB.setLongitude(126.972559);

                        // 거리구하기
                        // 그냥 location.distanceTo는 안되나?
                        double distance = locationA.distanceTo(locationB);

                        // 화살표 돌리는 코드
                        // imageArrow : 화살표 이미지 뷰
                        imageArrow.setRotation(mDegree);

                        // textViewDistance : 거리
                        // textViewScore : 점수
                        textViewDistance.setText(Double.toString(distance));
                        textViewScore.setText(stringStringMap.get("score"));


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
    public void postGpsData(String userId) {
        //body에 넣을 데이터

        String gps_longitude = Double.toString(location.getLongitude());
        String gps_latitude = Double.toString(location.getLatitude());
        String gps = gps_latitude+","+gps_longitude;

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
                        Log.d("postGps","Completed postGps");
                    }
                })
        );
    }
    /**
     * FCM message를 전송해주는 함수. 비콘 ID를 list로 관리 + 서비스에서 스코어링 임계치 넘은 event가 발생시에 PostFcmData실행. 그냥 subscribe(PostFcmData())해줘도 될 듯.
     */
    public void PostFcmData(String beaconId,String userId,String score) {
        // body에 넣을 데이터
        JSONObject paramObject = new JSONObject();
        try {
            paramObject.put("beacon_id", beaconId);
            paramObject.put("user_id", userId);
            paramObject.put("score", score);
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


