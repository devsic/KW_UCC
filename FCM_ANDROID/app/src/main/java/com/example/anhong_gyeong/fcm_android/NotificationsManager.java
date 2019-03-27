package com.example.anhong_gyeong.fcm_android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class NotificationsManager {

    private Context context;
    private NotificationManager notificationManager;
    private Notification helloNotification;
    private Notification goodbyeNotification;
    private int notificationId = 1;//여기 beaconid 넣어주면 될 듯함.

    public NotificationsManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //this.helloNotification = buildNotification("Hello", "You're near your beacon");
        this.goodbyeNotification = buildNotification("Bye bye", "You've left the proximity of your beacon");
    }

    private Notification buildNotification(String title, String text) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel contentChannel = new NotificationChannel(
                    "content_channel", "Things near you", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(contentChannel);
        }

        return new NotificationCompat.Builder(context, "content_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    public void startMonitoring() {
        // 변화 측정 위한 Observer
        ProximityObserver proximityObserver =
                new ProximityObserverBuilder(context, ((MyApplication) context).cloudCredentials)
                        .onError(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "proximity observer error: " + throwable);
                                throwable.printStackTrace();
                                return null;
                            }
                        })
                        .withBalancedPowerMode()
                        .withEstimoteSecureMonitoringDisabled()
                        .withTelemetryReportingDisabled()
                        .build();
        // zone 설정
        ProximityZone zone = new ProximityZoneBuilder()
                .forTag("monitoringexample-8mi")
                //.inFarRange()
                .inCustomRange(50.0)
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityContext) {
                        /**
                         * 비콘의 범위내로 들어왔을 때 notify해줌.
                         * proximityContext.getDeviceId() : 비콘의 id. 이거 저장해뒀다가 임계치 넘었을 때 notify를 해주는것이 아닌 server로 fcm call.
                         * 여기서 list에 비콘 Id put만 해둘 것.
                         * 나중에 스코어링 넘어갈 시에 list를 참조하여 postFCM
                         */

                        helloNotification = buildNotification("Hello", proximityContext.getDeviceId());
                        notificationManager.notify(notificationId, helloNotification);
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityContext) {
                        notificationManager.notify(notificationId, goodbyeNotification);
                        return null;
                    }
                })
                .build();
        // Observer 객체에 zone등록하여 observing시작.
        // zone 여러개 설정해서 여러개 등록하는 것도 가능한듯.
        proximityObserver.startObserving(zone);
    }

}
