/*
package com.example.anhong_gyeong.fcm_android.Accel;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class Accelerometer implements SensorEventListener {
    private Sensor linearAccelerSensor;

    static PublishSubject<SensorEvent> accelerData = PublishSubject.create();
    public static Observable<SensorEvent> getAccelerObservable(){ return accelerData; }

    public void initSensor(SensorManager sm){
        linearAccelerSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sm.registerListener(this,linearAccelerSensor,10000000);
    }

    // https://www.androidpub.com/1902258 이거에따라 onNext가 달라져야 함.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == linearAccelerSensor){
            Log.d("Accelerometer","Acclerometer onSensorchanged");
            Log.d("Accelerometer",Thread.currentThread().getName());
            accelerData.onNext(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
*/
