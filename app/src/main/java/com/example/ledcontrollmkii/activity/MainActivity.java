package com.example.ledcontrollmkii.activity;

//import android.annotation.NonNull;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.ledcontrollmkii.R;
import com.example.ledcontrollmkii.ScheduleEntry;
import com.example.ledcontrollmkii.dbhelper.ScheduleDbHelper;
import com.example.ledcontrollmkii.services.DatabaseService;
import com.example.ledcontrollmkii.services.MyFirebaseMessagingService;
import com.google.android.material.snackbar.Snackbar;


import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity {

    private ScheduleDbHelper _scheduleDbHelper = new ScheduleDbHelper(this);
    private DatabaseService _dbService;
    private static final String TAG = "MainActivity";
    WebSocketClient webSocketClient = null;
    private boolean websocketConnected = false;


    Context context;
    TextView textView;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String extra = intent.getStringExtra("source");

        Log.i(TAG, "onNewIntent: string extra " + extra);
        if (extra == null) return;

        Log.i(TAG, "onNewIntent: " + websocketConnected);
        if(websocketConnected) {

            new Thread(() -> {
                // Perform your background task here
                // This code runs on a background thread
                Log.i(TAG, "background thread: " + websocketConnected);
                ArrayList<ScheduleEntry> schedules = _dbService.getSixEntries();

                JSONArray jsonArray = new JSONArray();
                schedules.forEach((s) -> {

                    Log.i(TAG, "background thread: " + s.getScheduleRow() +
                            " " + s.getMode() + " " + s.getEventTime());
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("row", s.getScheduleRow());
                        jsonObject.put("mode", s.getMode());
                        jsonObject.put("time", s.getEventTime());

                        jsonArray.put(jsonObject);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });

                String jsonString = jsonArray.toString();
                // Print the JSON string
                Log.d("JSON Array of Objects", jsonString);
                Log.d("String length", String.valueOf(jsonString.length()));
                String msg = "JSON:" + jsonString;
                webSocketClient.send(msg);

                // If you need to update the UI, use a Handler to post a Runnable to the main thread
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Schedule updated.", Toast.LENGTH_SHORT).show();
                    // Update UI or perform any operation after the background task is complete
                    // This code runs on the main thread
                });
            }).start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        _dbService = new DatabaseService(_scheduleDbHelper);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        //setSupportActionBar(toolbar);
        toolbar.setLogo (R.mipmap.ic_launcher_main);

        //toolbar.getSupportActionBar().setDisplayShowHomeEnabled(true);
        //toolbar.getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_main);

       // textView = (TextView) findViewById(R.id.placeholder);
        //String token = MyFirebaseMessagingService.getToken(this);
        //Log.e("newToken", token);

        //textView.setText("LED Controller");
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
                msg = "Setting schedule";
                //DO_AUDIO = true;
                Intent scheduleIntent = new Intent(this, ScheduleActivity.class);
                scheduleIntent.putExtra("key", 3000); //Optional parameters
                this.startActivity(scheduleIntent);
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

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP & Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        String channelId = "com.example.ledcontrollmkii.urgent";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channelId)
                .setAutoCancel(true)
                .setLights(ContextCompat.getColor(context, R.color.purple), 200, 200)
                .setContentTitle("LED Controller")
                .setContentText(msg)
                .setColor(ContextCompat.getColor(context, R.color.purple))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);

        Notification builtNotification = notification.build();
        builtNotification.flags |= Notification.FLAG_SHOW_LIGHTS;

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builtNotification);
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
                runOnUiThread(() -> {
                    try {
                        TextView textView = findViewById(R.id.placeholder);
                        textView.setText(message);
                        if (message.contains("[Event]")) {
                            //showNotification(getApplicationContext(), message);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
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
                runOnUiThread(() -> {
                    try {
                        TextView textView = findViewById(R.id.placeholder);
                        textView.setText("Failed to connect.");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                websocketConnected = false;
                Log.e("WebSocket", "Connection Error: " + e.getMessage().toString());
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