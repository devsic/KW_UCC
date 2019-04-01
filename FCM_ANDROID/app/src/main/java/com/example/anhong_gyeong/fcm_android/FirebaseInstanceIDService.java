package com.example.anhong_gyeong.fcm_android;
import com.google.firebase.iid.FirebaseInstanceId;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInstanceID";
    public static String refreshedToken;


    @Override

    public void onTokenRefresh() {

        refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);

    }



    private void sendRegistrationToServer(String token) {

    }

}

