package com.example.imhyungbin.server;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    TextView a;
    Button b;
    Thread thread;
    int port = 12345;
    String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        a = (TextView) findViewById(R.id.textView1);
        a.setText("success");
        b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                thread = new ServerThread();
                thread.start();
            }

        });
    }

    public class ServerThread extends Thread {
        private ServerSocket server;
        Boolean istrue=true;

        public void run(){
            try {
                server=new ServerSocket(port);
                Socket socket= null;
                Log.d("Green","success socket");
                 while(istrue) {
                     socket = server.accept();
                     Log.d("Green","success accept");
                     try {
                         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         str = reader.readLine();
                         a.setText(str);
                         Log.d("Green","socket:"+str);


                         System.out.println(str);
                         istrue=false;

                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

