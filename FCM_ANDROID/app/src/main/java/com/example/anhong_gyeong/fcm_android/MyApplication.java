package com.example.anhong_gyeong.fcm_android;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;

public class MyApplication extends Application {
    // cloud에 있는 앱 ID,TOKEN 사용하여 만들어 준 것.
    public EstimoteCloudCredentials cloudCredentials = new EstimoteCloudCredentials("anhongkyung-s-your-own-app-nal", "271adf23dd0ba6562d80219e91812091");

    public void enableService() {
        if(!isMyServiceRunning(BeaconService.class)) {
            Intent intent = new Intent(this, BeaconService.class);
            startService(intent);
            Log.d("BeaconService","Service is first running");
        }
        else{
            Log.d("BeaconService","Service is already running");
        }

        if(!isMyServiceRunning(SocketService.class)) {
            Intent intent = new Intent(getApplicationContext(), SocketService.class);
            startService(intent);
            Log.d("SocketService","SocketService is first running");
        }
        else{
            Log.d("SocketService","SocketService is already running");
        }

        if(!isMyServiceRunning(SensorSocketService.class)) {
            Intent intent = new Intent(getApplicationContext(), SensorSocketService.class);
            startService(intent);
            Log.d("SensorSocketService","SensorSocketService is first running");
        }
        else{
            Log.d("SensorSocketService","SensorSocketService is already running");
        }

        if(!isMyServiceRunning(AccelerService.class)) {
            Intent intent = new Intent(getApplicationContext(), AccelerService.class);
            startService(intent);
            Log.d("AccelerService","AccelerService is first running");
        }
        else{
            Log.d("AccelerService","AccelerService is already running");
        }

        if(!isMyServiceRunning(GpsService.class)) {
            Intent intent = new Intent(getApplicationContext(), GpsService.class);
            startService(intent);
            Log.d("GpsService","GpsService is first running");
        }
        else{
            Log.d("GpsService","GpsService is already running");
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
