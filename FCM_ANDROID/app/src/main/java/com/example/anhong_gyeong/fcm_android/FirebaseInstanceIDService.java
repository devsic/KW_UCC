package com.example.anhong_gyeong.fcm_android;
import com.google.firebase.iid.FirebaseInstanceId;

import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInstanceID";
    @Override

    public void onTokenRefresh() {

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        // 앱 설치시에 fcm token 발급.
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);

        SharedPreferences pref = getSharedPreferences("RefreshedPreference", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        // 공유 메모리에 저장.  
        editor.putString("RefreshedToken",refreshedToken);
        editor.commit();
    }

    private void sendRegistrationToServer(String token) {

    }

}

