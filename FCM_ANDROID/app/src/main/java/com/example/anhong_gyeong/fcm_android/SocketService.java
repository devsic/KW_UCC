package com.example.anhong_gyeong.fcm_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.estimote.proximity_sdk.api.ProximityZoneContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class SocketService extends Service implements Runnable{
    private ServerSocket server;
    Thread SocketThread;
    Boolean istrue=true;
    final int port = 12345;
    static PublishSubject<String> socket_data = PublishSubject.create();
    public static Observable<String> getSocketObservable(){
        return socket_data;
    }

    public SocketService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        SocketThread = new Thread(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SocketThread.start();
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
            server=new ServerSocket(port);
            Socket socket= null;
            Log.d("Green","success socket");
            while(istrue) {
                socket = server.accept();
                Log.d("Green","success accept");
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String str = reader.readLine();
                    //a.setText(str);
                    Log.d("Green","socket:"+str);
                    socket_data.onNext(str);
                    //System.out.println(str);
                    //istrue=false;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
