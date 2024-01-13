package com.example.ledcontrollmkii.services;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.ledcontrollmkii.R;
import com.example.ledcontrollmkii.activity.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String NOTIFICATION_REPLY = "NotificationReply";
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

        RemoteMessage.Notification notification = remoteMessage.getNotification();

        //showNotification(notification, params);


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

    private void showNotification(RemoteMessage.Notification notification, Map<String, String> data) {

        Log.i("Notification", "showNotification:");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setAutoCancel(true)
                .setLights(ContextCompat.getColor(this, R.color.purple), 200, 200)
                .setContentTitle("LED Controller")
                .setContentText("HEY MAN")
                .setColor(ContextCompat.getColor(this, R.color.purple))
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS);

        //Notification builtNotification = notification.build();
        //builtNotification.flags |= Notification.FLAG_SHOW_LIGHTS;


        try{
            String picture_url = data.get("picture_url");
            if(picture_url != null && !"".equals(picture_url)){
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream((url.openConnection().getInputStream()));
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText((notification.getBody()))
                );

            }

        }catch (IOException e){
            e.printStackTrace();
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notificationBuilder.build());


        //NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        //notificationManager.notify(1, builtNotification);
    }

}
