package com.example.anhong_gyeong.fcm_android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class AccelerService extends Service implements SensorEventListener{
    private Sensor linearAccelerSensor;
    private SensorManager sm;
    static PublishSubject<SensorEvent> accelerData = PublishSubject.create();
    public static Observable<SensorEvent> getAccelerObservable(){
        return accelerData;
    }

    public AccelerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 중력 가속도를 제외한 가속도 센서 등록.
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        linearAccelerSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sm.registerListener(this,linearAccelerSensor,10000000);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == linearAccelerSensor){
            accelerData.onNext(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
