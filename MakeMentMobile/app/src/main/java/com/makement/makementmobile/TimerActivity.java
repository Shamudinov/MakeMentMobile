package com.makement.makementmobile;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.makement.makementmobile.databinding.ActivityMainBinding;
import com.makement.makementmobile.databinding.ActivityTimerBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TimerActivity extends AppCompatActivity {

    private String TaskName = "none";

    private int position = -1;

    private TaskAdapter adapter;

    private int TaskId = -1;

    private Date BeginDate = null;

    private long TotalTime = 0;

    private int seconds = 0;

    private boolean running;

    private boolean checkBoxTask = false;

    private boolean wasRunning;

    private Runnable run;

    RequestQueue mRequestQueue;

    final Handler handler
            = new Handler();
    final Handler handlerTask
            = new Handler();

    private ActivityTimerBinding binding;
    private AppBarConfiguration mAppBarConfiguration;

    String UserName = "";

    private boolean IsStartService = false;
    private DrawerLayout mDrawerLayout;



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityTimerBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        ListView list = (ListView) findViewById(R.id.tasksList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                TextView textView = (TextView) v;
                String selectedItem = (String)textView.getText();
                getTaskByName(selectedItem);
            }
        });

        final TextView timeView
                = (TextView)findViewById(
                R.id.time_view);
        String time
                = String
                .format(Locale.getDefault(),
                        "%d:%02d:%02d", 0, 0, 0);

        timeView.setText(time);

        mRequestQueue = Volley.newRequestQueue(this);

        setSupportActionBar(binding.appBarNavigation.toolbar);

        Bundle arguments = getIntent().getExtras();
        UserName = arguments.get("email").toString();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.textView);
        navUsername.setText(UserName);

        DrawerLayout drawer = binding.drawerLayout;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_exit)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);


        updateTasks();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickStart(View v) {
        Button button = (Button)findViewById(R.id.start_button);
        String str = (String) button.getText();
        //Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        //intent.putExtra("enabled", true);
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (str == "Stop") {
            setButtonStart("Stop");
        }
        else{
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();
                setButtonStart("Stop");
            }
            else {
                if (checkBoxTask) {
                    String t = Build.VERSION.RELEASE;
                    if (t.contains(".")) {
                        t = t.substring(0, t.indexOf('.'));
                    }
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                (Integer.parseInt(t) < 10 || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                            setButtonStart("Start");

                            new Thread(new Runnable() {
                                public void run() {
                                    Looper.prepare();
                                    try {
                                        changeStatus("https://api.makement.org/Task/ChangeStatus", 1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    Looper.loop();
                                }
                            }).start();
                        } else {
                            Snackbar snack = Snackbar.make(v, "Разрешите приложению использовать геопозицию", Snackbar.LENGTH_LONG);
                            View view = snack.getView();
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                            params.gravity = Gravity.TOP;
                            view.setLayoutParams(params);
                            snack.show();
                            setButtonStart("Stop");

                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                        1);
                        }
                }
                else {
                    Snackbar snack = Snackbar.make(v, "Выберите задание", Snackbar.LENGTH_LONG);
                    View view = snack.getView();
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                    params.gravity = Gravity.TOP;
                    view.setLayoutParams(params);
                    snack.show();
                    setButtonStart("Stop");
                }
            }
        }
    }

    private void setButtonStart(String text) {
        Button button = (Button)findViewById(R.id.start_button);

        Bundle arguments = getIntent().getExtras();
        String token = arguments.get("token").toString();
        Intent intent = new Intent(getApplicationContext(), SendService.class);
        intent.putExtra("token", token);
        intent.putExtra("userName", UserName);

        if (text == "Stop") {
            if (BeginDate != null) {
                TotalTime += new Date().getTime() - BeginDate.getTime();
                BeginDate = null;
            }
            running = false;
            button.setText("Start");
            handler.removeCallbacks(run);
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    try {
                        changeStatus("https://api.makement.org/Task/ChangeStatus", 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Looper.loop();
                }
            }).start();
            adapter.isEnable = false;

            stopService(intent);
            IsStartService = false;
        }
        else {
            if (running == false) {
                runTimer();

            }
            if (position != -1) {

            }
            running = true;
            button.setText("Stop");
            adapter.isEnabled(position);
            adapter.isEnable = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !IsStartService) {
                stopService(intent);
                startForegroundService(intent);
                IsStartService = true;
            }
            else if (!IsStartService) {
                stopService(intent);
                startService(intent);
                IsStartService = true;
            }
        }
    }

    public void onClickFinish(View v) {
        setButtonStart("Stop");
        checkBoxTask = false;
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                public void run() {
                    Looper.prepare();
                    try {
                        changeStatus("https://api.makement.org/Task/ChangeStatus", 2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateTasks();

                    Looper.loop();
                }
            }).start();
    }

    private void changeStatus(String path, int status) throws IOException {
        Bundle arguments = getIntent().getExtras();
        String token = arguments.get("token").toString();

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
            connection.setConnectTimeout(7000);

            wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write("{\n" +
                    "  \"id\": " + TaskId + ",\n" +
                    "  \"status\": " + status + "\n" +
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
    }

    private void runTimer() {
        run = new Runnable() {
            @Override
            public void run() {
                final TextView timeView
                        = (TextView)findViewById(
                        R.id.time_view);
                Date newDate = new Date();
                if (BeginDate == null) {
                    BeginDate = new Date();
                }
                long timer = TotalTime + (newDate.getTime() - BeginDate.getTime());

                String time
                        = String
                        .format("%d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(timer),
                                TimeUnit.MILLISECONDS.toMinutes(timer) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timer)),
                                TimeUnit.MILLISECONDS.toSeconds(timer) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timer)));

                if (TimeUnit.MILLISECONDS.toSeconds(timer) % 60 == 0) {
                    try {
                        getTasks();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                timeView.setText(time);
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (checkBoxTask) {
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        setButtonStart("Stop");
                        checkBoxTask = false;
                    }
                    else {
                        String t = Build.VERSION.RELEASE;
                        if (t.contains(".")) {
                            t = t.substring(0, t.indexOf('.'));
                        }
                        if (ActivityCompat.checkSelfPermission(TimerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                (ActivityCompat.checkSelfPermission(TimerActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED || Integer.parseInt(t) < 10)) {

                        }
                        else {
                            setButtonStart("Stop");
                            checkBoxTask = false;
                        }
                    }
                }
                if (position != -1) {
                    checkBoxTask = true;
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(run, 1000);
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getTasks() throws IOException {
        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, //GET - API-запрос для получение данных
                "https://api.makement.org/Task/GetTasksByToken", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                String[] tasks = new String[response.length()];

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject1 = response.getJSONObject(i);
                        tasks[i] = jsonObject1.getString("text");
                        if (jsonObject1.getInt("status") == 1) {
                            position = i;
                            TaskName = tasks[i];
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                setValues(tasks);
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
                Bundle arguments = getIntent().getExtras();
                String token = arguments.get("token").toString();
                params.put("Authorization", "Bearer "+ token);
                return params;
            }
        };

        mRequestQueue.add(request);
    }
    private void setValues(String[] tasks) {
        ListView tasksList = (ListView) findViewById(R.id.tasksList);

        adapter = new TaskAdapter(TimerActivity.this, android.R.layout.simple_list_item_multiple_choice, tasks);
        if (tasks.length == 0) {
            tasksList.setAdapter(null);
            return;
        }
        tasksList.setAdapter(adapter);
        tasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                checkBoxTask = true;
                String selectedItem = (String) parent.getItemAtPosition(position);
                getTaskByName(selectedItem);
            }
        });
        if (position != -1) {
            checkBoxTask = true;
            tasksList.performItemClick(tasksList.getAdapter().getView(position, null, null),position,tasksList.getAdapter().getItemId(position));
        }
    }

    private void getTaskByName(String name)  {
        name = name.replaceAll(" ", "%20");
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, //GET - API-запрос для получение данных
                "https://api.makement.org/Task/GetTaskByName?taskName=" + name, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int taskId = response.getInt("id");
                    TaskId = taskId;
                    getPeriod(taskId);
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
                Bundle arguments = getIntent().getExtras();
                String token = arguments.get("token").toString();
                params.put("Authorization", "Bearer "+ token);
                return params;
            }
        };
        mRequestQueue.add(request);
    }
    private void getPeriod(int taskId) {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, //GET - API-запрос для получение данных
                "https://api.makement.org/Task/GetPeriodsByTaskId?taskId=" + taskId, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    long totalTime = response.getLong("totalTime");
                    String beginTime = response.getString("beginTime");
                    SetTime(totalTime, beginTime);
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
                Bundle arguments = getIntent().getExtras();
                String token = arguments.get("token").toString();
                params.put("Authorization", "Bearer "+ token);
                return params;
            }
        };

        mRequestQueue.add(request);
    }
    private void SetTime(long totalTime, String beginTime) {
        final TextView timeView
                = (TextView)findViewById(
                R.id.time_view);
        TotalTime = totalTime;
        if (beginTime != "null") {
            String[] TimeArr = beginTime.split("T");
            String Time = TimeArr[0] + " " + TimeArr[1];
            SimpleDateFormat formatter6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                BeginDate = formatter6.parse(Time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            setButtonStart("Start");
            String time
                    = String
                    .format("%d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTime + (new Date().getTime() - BeginDate.getTime())),
                            TimeUnit.MILLISECONDS.toMinutes(totalTime+ (new Date().getTime() - BeginDate.getTime())) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalTime+ (new Date().getTime() - BeginDate.getTime()))),
                            TimeUnit.MILLISECONDS.toSeconds(totalTime+ (new Date().getTime() - BeginDate.getTime())) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTime+ (new Date().getTime() - BeginDate.getTime()))));
            timeView.setText(time);
        }
        else {
            BeginDate = null;
            String time
                    = String
                    .format("%d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTime),
                            TimeUnit.MILLISECONDS.toMinutes(totalTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalTime)),
                            TimeUnit.MILLISECONDS.toSeconds(totalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTime)));
            timeView.setText(time);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateTasks() {
        new Runnable() {
            @Override
            public void run() {
                try {
                    getTasks();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Class.forName("dalvik.system.CloseGuard")
                            .getMethod("setEnabled", boolean.class)
                            .invoke(null, true);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }

                handlerTask.postDelayed(this, 10000);
            }
        }.run();
    }

    private void scrollMyList(ListView list) {
        list.post(new Runnable() {
            @Override
            public void run() {
                //to scroll list programatically
                list.setSelection(list.getCount()-1);

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}