package com.example.anhong_gyeong.fcm_android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;

public class AccelerService extends Service{
/*    private Sensor linearAccelerSensor;
    private SensorManager sm;
    private Thread accelerThread;
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
        accelerThread = new Thread(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        accelerThread.start();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void run() {
        //sm.registerListener(this,linearAccelerSensor,SensorManager.SENSOR_DELAY_NㅈORMAL);
        // 가속도 센서에 listener등록.
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        linearAccelerSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Log.d("THREADTEST",Thread.currentThread().getName());
        sm.registerListener(this,linearAccelerSensor,10000000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == linearAccelerSensor){
            Log.d("ServiceAccelerometer","Acclerometer onSensorchanged");
            Log.d("ServiceAccelerometer",Thread.currentThread().getName());

            accelerData.onNext(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }*/
////////////////////////////////////////////////////////////////////////////////
    private SensorManager sm;
    private Accelerometer am;
    CompositeDisposable myCompositeDisposable;
    private double lAccX,lAccY,lAccZ;
    public AccelerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 중력 가속도를 제외한 가속도 센서 등록
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        am = new Accelerometer();
        am.initSensor(sm);
        myCompositeDisposable = new CompositeDisposable();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myCompositeDisposable.add(Accelerometer.getAccelerObservable()
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.io())
                .subscribeWith(new DisposableObserver<SensorEvent>() {
                    @Override
                    public void onNext(SensorEvent sensorEvent) {

                        //textViewAccel.setText(sensorEvent.toString());
                        //Log.d("ServiceAccelerometer",sensorEvent.toString());

                        lAccX = sensorEvent.values[0];
                        lAccY = sensorEvent.values[1];
                        lAccZ = sensorEvent.values[2];

                        lAccX = Math.round(lAccX*100)/100.0;
                        lAccY = Math.round(lAccY*100)/100.0;
                        lAccZ = Math.round(lAccZ*100)/100.0;

                        double accel = Math.sqrt((lAccX * lAccX) + (lAccY * lAccY) + (lAccZ * lAccZ));
                        accel = Math.round(accel*100)/100.0;
                        //textViewAccel.setText(lAccX+" "+lAccY+" "+lAccZ);
                        Log.d("ServiceAccelerometer",String.valueOf(accel));
                        Log.d("ServiceAccelerometer",Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                })

        );
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myCompositeDisposable.clear();
    }
}
