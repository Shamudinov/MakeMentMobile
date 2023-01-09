package com.makement.makementmobile;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SendService extends Service {
    LocationRequest locationRequest;

    LocationCallback locationCallBack;

    FusedLocationProviderClient fusedLocationProviderClient;
    String token;
    Thread Saves;
    Boolean IsRun = false;
    String UserName = "";
    Boolean IsLocation = true;
    Boolean IsApps = true;
    RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestQueue = Volley.newRequestQueue(this);
        setOptions();

        locationRequest = new LocationRequest();

        locationRequest.setInterval(240000);

        locationRequest.setFastestInterval(120000);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallBack = new LocationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                setOptions();

                Location location = locationResult.getLastLocation();


                String content = "";
                try {
                    if (IsLocation) {
                        content = saveLocation("https://api.makement.org/Track/SaveLocation", location);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        Saves = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                Looper.prepare();
                String t = Build.VERSION.RELEASE;
                if (t.contains(".")) {
                    t = t.substring(0, t.indexOf('.'));
                }
                if (ActivityCompat.checkSelfPermission(SendService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        (ActivityCompat.checkSelfPermission(SendService.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED || Integer.parseInt(t) < 10)) {
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SendService.this);
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
                }

//                if(LocalTime.now().isAfter(LocalTime.parse("18:00"))) {
//                    Notification notification = new NotificationCompat.Builder(SendService.this, "ChannelId")
//                            .setSmallIcon(R.mipmap.logo)
//                            .setContentTitle("MakeMent")
//                            .setContentText("Остановите своё задание!!!")
//                            .setAutoCancel(true)
//                            .setOngoing(true)
//                            .build();
//                }
                Looper.loop();
            }
        });
    }
  //  @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(intent.getExtras().containsKey("token"))
            token = intent.getStringExtra("token");
        if (intent.getExtras().containsKey("userName"))
            UserName = intent.getStringExtra("userName");
        Saves.start();
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("ChannelId", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
            Notification notification =
                    new Notification.Builder(this, "ChannelId")
                            .setContentTitle("MakeMent")
                            .setContentText("Running")
                            .setSmallIcon(R.mipmap.logo)
                            .setContentIntent(pendingIntent)
                            //.setTicker(getText(R.string.ticker_text))
                            .build();

            startForeground(1, notification);
        }
        else {
            Notification notification = new NotificationCompat.Builder(this, "ChannelId")
                    .setSmallIcon(R.mipmap.logo)
                    .setContentTitle("MakeMent")
                    .setContentText("Running")
                    .setOngoing(true)
                    .build();
            startForeground(1, notification);
        }
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String saveLocation(String path, Location location) throws IOException {
        SQLiteDatabase db = getBaseContext().openOrCreateDatabase("MakementMobileDB.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS location (latitude DOUBLE, longitude DOUBLE, altitude DOUBLE, accuracy DOUBLE, speed DOUBLE, " +
                "date MEDIUMTEXT, UserName MEDIUMTEXT);");

        String ans = "";

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            Cursor query = db.rawQuery("SELECT * FROM location;", null);
            while(query.moveToNext()){

                double latitude = query.getDouble(0);
                double longitude = query.getDouble(1);
                double altitude = query.getDouble(2);
                double accuracy = query.getDouble(3);
                double speed = query.getDouble(4);
                String date = query.getString(5);
                String email = query.getString(6);

                sendLocation(path, latitude, longitude, altitude, accuracy, speed, date, email);
            }
            db.execSQL("DELETE FROM location");
            double altitude = -1;
            if (location.hasAltitude()) {
                altitude = location.getAltitude();
            }

            double speed = -1;
            if (location.hasSpeed()) {
                speed = location.getSpeed();
            }
            Date localDate = new Date(); //For reference
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String date = df.format(localDate);
            ans = sendLocation(path, location.getLatitude(), location.getLongitude(), altitude, location.getAccuracy(), speed, date, UserName);
        }
        else {
            double altitude = -1;
            if (location.hasAltitude()) {
                altitude = location.getAltitude();
            }

            double speed = -1;
            if (location.hasSpeed()) {
                speed = location.getSpeed();
            }
            Date localDate = new Date(); //For reference
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String date = df.format(localDate);
            try {
                db.execSQL("INSERT INTO location VALUES (" + location.getLatitude() + ", " + location.getLongitude() + ", " + altitude + ", "
                        + location.getAccuracy() + ", " + speed + ", \'" + date + ", " + UserName + "\');");
            }
            catch (SQLException e){
                e.printStackTrace();
            }
        }
        return  ans;
    }
    private String sendLocation(String path, double latitude, double longitude, double altitude, double accuracy, double speed, String date, String email) throws IOException {
         BufferedReader reader=null;
        InputStream stream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter wr = null;

        try {
            URL url=new URL(path);
            connection =(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.addRequestProperty("Authorization", "Bearer " + token);
            connection.setReadTimeout(10000);



            wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write("{\n" +
                    "  \"latitude\": " + latitude + ",\n" +
                    "  \"longitude\": " + longitude + ",\n" +
                    "  \"altitude\": " + altitude + ",\n" +
                    "  \"accuracy\": "+ accuracy +",\n" +
                    "  \"speed\": " + speed + ",\n" +
                    "  \"date\": \"" + date + "\",\n" +
                    "  \"email\": \"" + email + "\"\n" +
                    "}");
            wr.flush();
            connection.connect();
            stream = connection.getInputStream();
            reader= new BufferedReader(new InputStreamReader(stream));
            StringBuilder buf=new StringBuilder();
            String line;
            while ((line=reader.readLine()) != null) {
                buf.append(line).append("\n");
            }
            return(buf.toString());
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return "";
    }

    private void setOptions() {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, //GET - API-запрос для получение данных
                "https://api.makement.org/Organization/GetCompanyOption", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    IsLocation = response.getBoolean("isTrackLocation");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { // в случае возникновеня ошибки
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer "+ token);
                return params;
            }
        };
        mRequestQueue.add(request);
    }
}
