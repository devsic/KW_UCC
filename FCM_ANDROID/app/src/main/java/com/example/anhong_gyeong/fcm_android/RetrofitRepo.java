package com.example.anhong_gyeong.fcm_android;

public class RetrofitRepo {
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