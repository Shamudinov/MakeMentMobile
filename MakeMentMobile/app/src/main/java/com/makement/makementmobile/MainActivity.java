package com.makement.makementmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView contentView = (TextView) findViewById(R.id.content);
        Button btnFetch = (Button)findViewById(R.id.login);
        Intent intent = new Intent(this, TimerActivity.class);
        AccountManager am = AccountManager.get(this);

        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);

        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        String content = "";
                        try{
                            content = getContent("https://api.makement.org/User/Login");
                        }
                        catch (IOException ex){
                            Snackbar snack = Snackbar.make(v, "Логин или пароль указан не верно", Snackbar.LENGTH_LONG);
                            View view = snack.getView();
                            FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                            params.gravity = Gravity.TOP;
                            view.setLayoutParams(params);
                            snack.show();
                        }
                        finally {
                            if (content != null && content != "") {
                                try {
                                    JSONObject json = new JSONObject(content);
                                    intent.putExtra("token", json.getString("token"));
                                    EditText login = findViewById(R.id.username);
                                    intent.putExtra("email", login.getText());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                startActivity(intent);
                            }
                        }
                    }
                }).start();
            }
        });

        androidx.appcompat.app.AlertDialog.Builder a_builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);

        a_builder.setMessage("Privacy Policy\n" +
                "Makement created the Makement app as a free app. This SERVICE is provided by Makement free of charge and is intended for use as is.\n" +
                "This page is used to inform visitors about our policies for collecting, using, and disclosing Personal Information if someone chooses to use our Service.\n" +
                "If you choose to use our Service, you agree to the collection and use of information in accordance with this policy. The personal information we collect is used to provide and improve the Service. We will not use or share your information with anyone, except as described in this Privacy Policy.\n" +
                "The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which are available at creation, unless otherwise defined in this Privacy Policy.\n" +
                "The app does use third-party services that may collect information used to identify you.\n" +
                "Link to the privacy policy of third-party service providers used by the app\n" +
                "Google Play Services\n" +
                "Log data\n" +
                "We want to inform you that whenever you use our Service, in the event of an error in the app, we collect data and information (through third-party products) on your phone under the name Log Data. This log data may include information such as your device's Internet Protocol address (“IP”), device name, operating system version, application configuration when using our Service, time and date of your use of the Service, and other statistics.\n" +
                "Cookies\n" +
                "Cookies are files with a small amount of data that are usually used as anonymous unique identifiers. They are sent to your browser from the websites you visit and stored in your device's internal memory.\n" +
                "This Service does not use these \"cookies\" explicitly. However, the app may use third-party code and libraries that use \"cookies\" to collect information and improve its services. You have the option to either accept or reject these cookies and find out when the cookie is sent to your device. If you choose to opt out of our cookies, you may not be able to use some parts of this service.\n" +
                "Service Providers\n" +
                "We may hire third-party companies and individuals for the following reasons:\n" +
                "To facilitate our service;\n" +
                "Provide Services on our behalf;\n" +
                "To perform services related to the Service; or\n" +
                "To help us analyze how our Service is used.\n" +
                "We want to inform users of this Service that these third parties have access to their Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are required not to disclose or use this information for any other purpose.\n" +
                "Safety\n" +
                "We value your trust in providing us with your Personal Information, so we strive to use commercially acceptable means to protect it. But remember that no method of transmission over the Internet or method of electronic storage is 100% secure and reliable, and we cannot guarantee its absolute security.\n" +
                "Links to Other sites\n" +
                "This Service may contain links to other sites. If you click on a third-party link, you will be redirected to this site. Please note that these external sites are not managed by us. Therefore, we strongly recommend that you read the Privacy Policies of these websites. We have no control over, and assume no responsibility for, the content, privacy policies, or practices of any third-party sites or services.\n" +
                "\n" +
                "\n" +
                "Information we collect\n" +
                "The term \" Location”\n" +
                "When you use our app to provide features of our app, we collect data about your location, with your prior permission:\n" +
                "This app collects location data to enable \"geolocation\" even when the app is closed or not in use. While using our app, the location function works in the background. When you close the app, the location function will work. To stop the location function, you need to click on the \"Finish\" button, after that the app will stop the location function.\n" +
                "We use this information to provide features of our service, improve and customize our service. The information can be uploaded to the company's servers and / or the service provider's servers, or simply stored on your device.\n" +
                "For a better experience when using our Service, we may require you to provide us with certain personal information, including, but not limited to the use of the apps, geolocation. The information we request will be stored by us and used in accordance with this privacy policy.\n" +
                "Children's privacy\n" +
                "These Services are not intended for persons under the age of 13. We do not knowingly collect personal information from children under the age of 13. If we find that a child under the age of 13 has provided us with personal information, we will immediately delete it from our servers. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact us so that we can take the necessary steps.\n" +
                "Changes to This Privacy Policy\n" +
                "We may update our Privacy Policy from time to time. Therefore, you are encouraged to periodically review this page for any changes. We will notify you of any changes by posting the new Privacy Policy on this page.\n" +
                "This policy is effective from 2021-05-20\n" +
                "Contact us\n" +
                "If you have any questions or suggestions about our Privacy Policy, do not hesitate to contact us at makement.org@gmail.com.")
                .setCancelable(false)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        SharedPreferences.Editor editor = getSharedPreferences("mypref", MODE_PRIVATE).edit();
                        editor.putBoolean("dontshow", true);
                        editor.commit();
                        String t = Build.VERSION.RELEASE;
                        if (t.contains(".")) {
                            t = t.substring(0, t.indexOf('.'));
                        }

                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                (Integer.parseInt(t) < 10 || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    1);
                        }
                        dialog.cancel();
                    }
                });

        SharedPreferences prefs = getSharedPreferences("mypref", MODE_PRIVATE);
        boolean show = prefs.getBoolean("dontshow", false);
        if(show == false){
            a_builder.show();
        }
    }

    public void loginApi(View view) {
        System.setProperty("http.proxyHost", "176.126.164.157");
        System.setProperty("http.proxyPort", "228");

        String email = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();

        try {
            String baseUrl = "https://api.makement.org/api/Authorization/Login";
            URL url = new URL(baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());

            String ap = "{\n" +
                    "  \"email\": " + email + ",\n" +
                    "  \"password\": +" + password + "\n" +
                    "}";
            OutputStreamWriter ap_osw= new OutputStreamWriter(conn.getOutputStream());
            ap_osw.write(ap);
            ap_osw.flush();
            ap_osw.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuilder response = new StringBuilder();

            String output = "";

            while ((output = br.readLine()) != null) {
                response.append(output);
                response.append('\r');
            }
            String mes = response.toString();
            Log.i("INFO", mes);
            conn.disconnect();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getContent(String path) throws IOException {
        String email = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        BufferedReader reader=null;
        InputStream stream = null;
        HttpURLConnection connection = null;
        OutputStreamWriter wr = null;
        try {
            URL url=new URL(path);
            connection =(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(7000);

            wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write("{\n" +
                    "  \"email\": \"" + email + "\",\n" +
                    "  \"password\": \"" + password + "\"\n" +
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
            Intent intent = new Intent(this, TimerActivity.class);
            return(buf.toString());
        }
        finally {
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
}