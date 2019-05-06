package com.example.anhong_gyeong.fcm_android;

public class RetrofitRepo {
    // RetrofitRepo는 Observable<RetrofitRepo>로 사용
    // retrofit request의 결과를 받아올 data class
    // 따라서 user_id와 gps는 Server에서 받아올 변수명과 동일 해야함.
    String user_id;
    String gps;
    //timestamp값 따로 안 넣어줘도 되는지?
    public String getUser() {
        return user_id;
    }

    public String getGps() {
        return gps;
    }

}
/*
public class RetrofitRepo {
    String login;

    public String getUser() {
        return login;
    }
}
*/