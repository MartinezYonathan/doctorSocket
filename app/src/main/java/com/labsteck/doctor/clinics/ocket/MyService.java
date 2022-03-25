package com.labsteck.doctor.clinics.ocket;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class MyService extends Service {

   private HashMap<String, String> params;
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://socket-cmp-dev.herokuapp.com/");
        } catch (URISyntaxException e) {}
    }

    @Override
    public void onCreate() {

        mSocket.connect();

        Log.d(TAG, "Servicio creado...");
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
        Log.d(TAG, "Servicio terminado...");
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
        //mSocket.on("refreshPagellamada","");
    }

    private Emitter.Listener testing = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("Response", args[0].toString());
        }
    };
}