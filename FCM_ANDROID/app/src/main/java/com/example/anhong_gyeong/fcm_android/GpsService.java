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

public class GpsService extends Service implements Runnable{
    double longitude,latitude;
    LocationManager Im;
    Thread gpsThread;
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 10f;

    static PublishSubject<Location> gps_data = PublishSubject.create();

    public static Observable<Location> getGpsObservable(){
        return gps_data;
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
        @Override
        public void onLocationChanged(Location location) {
            //mLastLocation.set(location);// 여기서 그냥 postGps해주면 될듯.
            gps_data.onNext(location);
            Log.d(TAG,"onLocationChanged call"+location.getLatitude() +","+ location.getLongitude());
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
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


}
