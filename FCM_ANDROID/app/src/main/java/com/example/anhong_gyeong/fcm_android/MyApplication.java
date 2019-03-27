package com.example.anhong_gyeong.fcm_android;

import android.app.Application;

import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MyApplication extends Application {
    // cloud에 있는 앱 ID,TOKEN 사용하여 만들어 준 것.
    public EstimoteCloudCredentials cloudCredentials = new EstimoteCloudCredentials("anhongkyung-s-your-own-app-nal", "271adf23dd0ba6562d80219e91812091");
    private NotificationsManager notificationsManager;

    public void enableBeaconNotifications() {
        notificationsManager = new NotificationsManager(this);
        notificationsManager.startMonitoring();
    }

}
