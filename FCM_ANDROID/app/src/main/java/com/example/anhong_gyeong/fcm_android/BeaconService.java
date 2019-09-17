package com.example.anhong_gyeong.fcm_android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class BeaconService extends Service implements Runnable {
    private Context context;
    Thread BeaconThread;
    static PublishSubject<ProximityZoneContext[]> beaconData = PublishSubject.create();

    public static Observable<ProximityZoneContext[]> getBeaconObservable(){
        return beaconData;
    }
    public BeaconService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context=getApplicationContext();
        BeaconThread = new Thread(this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        BeaconThread.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //proximityObserver.stop();
        super.onDestroy();
    }

    @Override
    public void run() {

        ProximityObserver proximityObserver =
                new ProximityObserverBuilder(context, ((MyApplication) context).cloudCredentials)
                        .onError(throwable -> {
                            throwable.printStackTrace();
                            return null;
                        })
                        .withBalancedPowerMode()
                        .withEstimoteSecureMonitoringDisabled()
                        .withTelemetryReportingDisabled()
                        .build();

        ProximityZone zone = new ProximityZoneBuilder()
                .forTag("monitoringexample-8mi")
                .inCustomRange(5.0)
                .onEnter(proximityContext -> {
                    Log.d("BeaconOnEnter",proximityContext.getDeviceId());
                    return null;
                })
                .onExit(proximityContext -> {
                    Log.d("BeaconOnExit",proximityContext.getDeviceId());
                    return null;
                })
                // Enter는 beacon이 접근했을 때 동작. 하지만 해당 비콘이 범위 밖으로 exit해야 다시 실행 됨. 따라서 conTextChange를 사용하는것.
                .onContextChange(proximityZoneContexts -> {
                    ProximityZoneContext[] contextsArray = proximityZoneContexts.toArray(new ProximityZoneContext[0]);
                    beaconData.onNext(contextsArray);
                    return null;
                })
                .build();
        proximityObserver.startObserving(zone);
    }
}
