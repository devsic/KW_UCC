package com.example.anhong_gyeong.fcm_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class SensorSocketService extends Service implements Runnable{
    private ServerSocket sensorServer;
    Thread sensorSocketThread;
    Boolean istrue=true;
    final int sensorPort = 12344;
    static PublishSubject<String> sensorSocketData = PublishSubject.create();
    public static Observable<String> getSensorSocketObservable(){
        return sensorSocketData;
    }

    public SensorSocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorSocketThread = new Thread(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorSocketThread.start();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void run() {
        try {
            sensorServer=new ServerSocket(sensorPort);
            Socket socket= null;
            socket = sensorServer.accept();
            while(istrue) {
                try {
                    //습도,온도,가속도
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String str = reader.readLine();
                    sensorSocketData.onNext(str);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
