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

            // 여기 while(1)로 감싸주기.
            // accept시에 thread 새로 생성 및 Runnable 등록. 이렇게 하면 센서도 같이 받을 수 있을듯.
            // 하지만, 센서와 sleep을 표시할 flag를 라즈베리 파이에서 추가로 전송 해줘야 할 듯.
            // socket이 끊길시에 처리는 client에서 해줘야 한다고 함. https://stackoverflow.com/questions/151590/how-to-detect-a-remote-side-socket-close
            socket = server.accept();
            while(istrue) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String str = reader.readLine();
                    socket_data.onNext(str);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
