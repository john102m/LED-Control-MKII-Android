package com.example.ledcontrollmkii;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    WebSocketClient webSocketClient = null;
    private boolean websocketConnected = false;

    Context context;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.placeholder);

        String token = MyFirebaseMessagingService.getToken(this);
        Log.e("newToken", token);

        textView.setText("Hey Dudes");

        createWebSocketClient();

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    public void onClick(View view) {

        if (!websocketConnected) {

            //showToast("Not connected!");
            // Create a notification
            View rootLayout = findViewById(R.id.rootLayout);
            Snackbar snackbar = Snackbar.make(rootLayout, "Websocket not connected!", Snackbar.LENGTH_SHORT);
            snackbar.show();
            //showNotification(this, "Websocket not connected!");

            return;
        }
        //DO_AUDIO = false;

        String msg = "";
        Button button = (Button) view;
        String buttonText = button.getText().toString();
        switch (buttonText.toUpperCase()) {
            case "OFF":
                msg = "B19";
                break;

            case "MODE-":
                msg = "B7";
                break;
            case "MODE+":
                msg = "B8";
                break;
            case "AUDIO":
                msg = "B9";
                //DO_AUDIO = true;
                break;
            case "SHUFFLE":
                msg = "B11";
                break;
            case "RANDOM":
                msg = "B12";
                break;
            case "SLOWER":
                msg = "B13";
                break;
            case "FASTER":
                msg = "B14";
                break;

            default:
                msg = buttonText;

        }

        webSocketClient.send(msg);
        //String text = "You clicked " + buttonText;
        //Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public static void showNotification(@NotNull Context context, String msg) {
        Log.i(TAG, "showNotification:" + context.toString());

        String channelId = "com.example.ledcontrollmkii.urgent";
        CharSequence channelName = "My_Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        // Create a notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification using Notification.Builder
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, channelId)
                    .setContentTitle("LED Controller")
                    .setContentText(msg)
                    .setSmallIcon(R.drawable.custome_shape_1);
        }

        // Show the notification
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.notify(1001, builder.build());
    }
    private void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
            // of course you need to be connected to your own WiFi for this
            uri = new URI("ws://192.168.1.220:81/websocket");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                TextView textView = findViewById(R.id.placeholder);
                textView.setText("Connected");
                websocketConnected = true;
                //webSocketClient.send("B9");
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;
                // showNotification(getApplicationContext() , message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TextView textView = findViewById(R.id.placeholder);
                            textView.setText(message);
                            if(message.contains("[Event]"))
                            {
                                showNotification(getApplicationContext(), message);
                            }
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
                TextView textView = findViewById(R.id.placeholder);
                textView.setText("Connection Error");
                //textView.setText(e.getMessage().toString());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
                websocketConnected = false;
            }
        };
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(1000);
        webSocketClient.connect();
    }
}