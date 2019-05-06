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

import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class BeaconService extends Service implements Runnable {
    private Context context;
    Thread BeaconThread;
    static PublishSubject<ProximityZoneContext[]> beacon_data = PublishSubject.create();

    public static Observable<ProximityZoneContext[]> getBeaconObservable(){
        return beacon_data;
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
        Log.d("BeaconService","Service onDestroy called");

    }

    @Override
    public void run() {

        ProximityObserver proximityObserver =
                new ProximityObserverBuilder(context, ((MyApplication) context).cloudCredentials)
                        .onError(throwable -> {
                            Log.e("app", "proximity observer error: " + throwable);
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
                    //beacon_data.onNext(proximityContext.getDeviceId());
                    //beacon_data.onComplete();
                    Log.d("BeaconOnEnter",proximityContext.getDeviceId());
                    return null;
                })
                .onExit(proximityContext -> {
                    Log.d("BeaconOnExit",proximityContext.getDeviceId());
                    return null;
                })
                .onContextChange(proximityZoneContexts -> {
                    ProximityZoneContext[] contextsArray = proximityZoneContexts.toArray(new ProximityZoneContext[0]);
                    beacon_data.onNext(contextsArray);
                    /*String beacon1ID = contextsArray[0].getDeviceId();
                    Log.d("BeaconOnContext",beacon1ID);
                    */

                    /*
                    while(iter.hasNext()){
                        Log.d("BeaconOnContext",iter.getClass().getName());
                    }*/
                    /*HashSet<ProximityZoneContext> temp = new HashSet<ProximityZoneContext>();
                    temp.addAll(proximityZoneContexts);
                    Iterator<ProximityZoneContext> iter= temp.iterator();
                    while(iter.hasNext()){
                        Log.d("BeaconOnContext",iter.toString());
                    }*/
                    /*Iterator<ProximityZoneContext> iter = (Iterator<ProximityZoneContext>) proximityZoneContexts.iterator();
                    while(iter.hasNext()){
                        Log.d("BeaconOnContext",iter.toString());
                    }*/
                    //Log.d("BeaconOnContext",proximityZoneContexts.toString());
                    return null;
                })
                .build();
        proximityObserver.startObserving(zone);
    }
}
