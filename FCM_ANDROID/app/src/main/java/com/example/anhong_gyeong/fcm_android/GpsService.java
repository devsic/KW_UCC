package com.example.anhong_gyeong.fcm_android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.estimote.proximity_sdk.api.ProximityZoneContext;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

// https://m.blog.naver.com/PostView.nhn?blogId=netrance&logNo=110174802599&proxyReferer=https%3A%2F%2Fwww.google.com%2F
public class GpsService extends Service implements Runnable{
    double longitude,latitude;
    LocationManager Im;
    Thread gpsThread;
    private static final String TAG = "TestGps";
    private LocationManager locManager = null;
    private static final int LOCATION_INTERVAL = 3000;
    private static final float LOCATION_DISTANCE = 3f;

    static PublishSubject<Location> gpsData = PublishSubject.create();

    public static Observable<Location> getGpsObservable(){
        return gpsData;
    }
    public GpsService() {

    }
    // location listener
    // location의 변화가 있을때마다 해당 Location을 set해준다.
    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }
        // 위치가 변할 때마다 호출됨.
        // 최신 위치가 location parameter로 전달.
        @Override
        public void onLocationChanged(Location location) {

            gpsData.onNext(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
    // gps, network 에 대한 Listener 2개 생성.
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gpsThread = new Thread(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gpsThread.run();
        return START_NOT_STICKY;
    }

    @Override
    public void run() {
        // LocationManger 객체 생성.
        initializeLocationManager();

        // LocationMager에 대해 listener 등록.
        // network, gps에 대해 각각 리스너를 위에서 만들어줬고 그에 대해 동작 수행.
        try {
            locManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
            Log.d("GPSCALEED","GPS THREAD IS CALLED");
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            locManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    private void initializeLocationManager() {
        if (locManager == null) {
            locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
