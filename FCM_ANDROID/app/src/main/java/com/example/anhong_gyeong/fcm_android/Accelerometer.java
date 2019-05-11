package com.example.anhong_gyeong.fcm_android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class Accelerometer implements SensorEventListener {
    private Sensor linearAccelerSensor;
    private SensorManager sm;

    static PublishSubject<SensorEvent> accelerData = PublishSubject.create();
    public static Observable<SensorEvent> getAccelerObservable(){
        return accelerData;
    }

    public void initSensor(SensorManager sm){
        linearAccelerSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
