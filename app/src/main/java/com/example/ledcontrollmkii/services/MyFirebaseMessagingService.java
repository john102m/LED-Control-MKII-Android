package com.example.ledcontrollmkii.services;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.ledcontrollmkii.activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("newToken", token);
        //Add your token in your sharepreferences.
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fcm_token", token).apply();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> params = remoteMessage.getData();
        JSONObject object = new JSONObject(params);
        Log.e("JSON_OBJECT", object.toString());


//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            public void run() {
//                try {
//                    MainActivity.showNotification(getApplicationContext(), object.getString("bodyText"));
//                } catch (JSONException e) {
//                    //MainActivity.showNotification(getApplicationContext(),"Unknown message");
//                    Log.e("Exception", "Unknown message");
//                }
//            }
//        });


        Log.i("new message", remoteMessage.getData().toString());

    }
    //Whenewer you need FCM token, just call this static method to get it.
    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fcm_token", "empty");
    }
}
