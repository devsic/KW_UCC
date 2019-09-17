package com.example.anhong_gyeong.fcm_android;

// retrofit request의 결과를 받아올 data class
public class RetrofitRepo {
    // user_id와 gps는 Server에서 받아올 변수명과 동일 해야함.
    String user_id;
    String gps;
    public String getUser() {
        return user_id;
    }

    public String getGps() {
        return gps;
    }

}