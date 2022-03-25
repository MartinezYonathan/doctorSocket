package com.labsteck.doctor.clinics.ocket;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

public class PrincipalActivity extends AppCompatActivity {

    private static  final String PREFS_KEY = "BOTONSOCKETINICIO";
    private static  final String ESTADO_BOTON = "ESTADOBOTONINICIO";
    private static  final String PREFS_KEY_FIN = "BOTONSOCKETFIN";
    private static  final String ESTADO_BOTON_FIN = "ESTADOBOTONFIN";
    public static  String username = "";
    public static Timer t;
    private static HashMap<String, String> params;
    private static Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://socket-cmp-dev.herokuapp.com/");
        } catch (URISyntaxException e) {}
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        username = getIntent().getExtras().getString("username");

        Toast.makeText(PrincipalActivity.this, username, Toast.LENGTH_SHORT).show();

        Button buttonIniciar = findViewById(R.id.buttonIniciar);
        Button buttonFin = findViewById(R.id.buttonFin);
        buttonIniciar.setEnabled(obtener_estado_boton_inicio());
        buttonFin.setEnabled(obtener_estado_boton_fin());

        mSocket.connect();
        Log.d(TAG, "Servicio socket creado...");

        Intent intent = new Intent(PrincipalActivity.this, MyService.class);
        buttonIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                params = new HashMap<String, String>();
                params.put("room", "global");
                params.put("user", username);
                JSONObject json = new JSONObject(params);

                mSocket.emit("online", json);

                IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                BroadcastReceiver mReceiver = new ScreenReceiver();
                registerReceiver(mReceiver, filter);

                guardar_estado_boton_inicio(false);
                buttonIniciar.setEnabled(false);
                guardar_estado_boton_fin(true);
                buttonFin.setEnabled(true);
            }
        });
        buttonFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                guardar_estado_boton_inicio(true);
                buttonIniciar.setEnabled(true);
                buttonFin.setEnabled(false);
                guardar_estado_boton_fin(false);

                mSocket.close();
                Log.d(TAG, "Servicio socket terminado...");
            }
        });
    }

    public static class ScreenReceiver extends BroadcastReceiver {

        public static boolean pantalla = true;

        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                Log.e("estado", "Pantalla Apagada");
                t = new Timer();

                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {

                        if(!mSocket.connected()) {

                            mSocket.connect();

                            params = new HashMap<String, String>();
                            params.put("room", "global");
                            params.put("user", username);
                            JSONObject json = new JSONObject(params);

                            mSocket.emit("online", json);
                            Log.e("estado", "Socket se reconecto....");
                        }else{
                            Log.e("estado", "Socket en linea....");
                        }

                    }
                };
                t.schedule(timerTask, 10000,10000);
                pantalla = false;
            }
            else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                Log.e("estado", "Pantalla Encendida");
                t.cancel();
                if(!mSocket.connected()) {

                    mSocket.connect();

                    params = new HashMap<String, String>();
                    params.put("room", "global");
                    params.put("user", username);
                    JSONObject json = new JSONObject(params);

                    mSocket.emit("online", json);
                    Log.e("estado", "Socket se reconecto....");
                }else{
                    Log.e("estado", "Socket en linea....");
                }
                Toast.makeText(context, "La Pantalla está Encendida", Toast.LENGTH_LONG).show();

                pantalla = true;
            }
        }

    }

    public static class MyService extends Service {
        @Override
        public void onCreate() {
            mSocket.connect();
            Log.d(TAG, "Servicio socket creado...");
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if(!mSocket.connected()) {
                String username = intent.getExtras().getString("username");
                params = new HashMap<String, String>();
                params.put("room", "global");
                params.put("user", username);
                JSONObject json = new JSONObject(params);

                mSocket.emit("online", json);
            }else{
                Log.i("MyService", "socket conectado");
            }
            return START_STICKY;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mSocket.close();
            Log.d(TAG, "Servicio socket terminado...");
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        public void refreshSocket() {
            //linea para recivir la llamada a un doctor
            mSocket.on("online", testing);

            //linea para recivir la respuesta a la llamada a un doctor
            mSocket.on("refreshPagellamada",testing);
        }

        private Emitter.Listener testing = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e("Response", args[0].toString());
            }
        };
    }

    public void guardar_estado_boton_inicio(Boolean valor) {
        SharedPreferences settings = getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putBoolean(ESTADO_BOTON,valor);
        editor.apply();
    }

    public boolean obtener_estado_boton_inicio(){
        SharedPreferences settings = getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        return settings.getBoolean(ESTADO_BOTON,true);

    }

    public void guardar_estado_boton_fin(Boolean valor) {
        SharedPreferences settings = getSharedPreferences(PREFS_KEY_FIN, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putBoolean(ESTADO_BOTON_FIN,valor);
        editor.apply();
    }

    public boolean obtener_estado_boton_fin(){
        SharedPreferences settings = getSharedPreferences(PREFS_KEY_FIN, MODE_PRIVATE);
        return settings.getBoolean(ESTADO_BOTON_FIN,false);

    }
}
