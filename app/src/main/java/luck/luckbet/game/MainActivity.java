package luck.luckbet.game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); Handler handler = new Handler(); //управление потоками
        handler.postDelayed(new Runnable() { //Выполнить Runnable с задержкой
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
                finish(); //закрыть активность
            }
        }, 2000);
    }
}