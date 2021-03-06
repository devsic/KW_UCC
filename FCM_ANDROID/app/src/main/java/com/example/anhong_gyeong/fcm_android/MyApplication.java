package com.example.anhong_gyeong.fcm_android;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MyApplication extends Application {
    // cloud에 있는 앱 ID,TOKEN 사용하여 만들어 준 것.
    public EstimoteCloudCredentials cloudCredentials = new EstimoteCloudCredentials("anhongkyung-s-your-own-app-nal", "271adf23dd0ba6562d80219e91812091");
    private NotificationsManager notificationsManager;

    public void enableService() {
        //notificationsManager = new NotificationsManager(this);
        //notificationsManager.startMonitoring();

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
