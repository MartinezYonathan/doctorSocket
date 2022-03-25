package com.labsteck.doctor.clinics.ocket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static  final String PREFS_KEY = "BOTONLOGIN";
    private static  final String USER_KEY = "USUARIOLOGIN";
    private static  final String ESTADO_BOTON = "ESTADOBOTON";
    private static  final String ESTADO_USUARIO = "ESTADOUSUARIO";

    private NotificationManagerCompat notificationManagerCompat;
    static TextView textView;

    static EditText username;
    static EditText password;
    static Button boton;

    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.edtUsuario);
        password = findViewById(R.id.edtPassword);
        boton = findViewById(R.id.btnLogin);

        if (obtener_estado_boton_login()) {
            Intent intent = new Intent(getApplicationContext(), PrincipalActivity.class);
            intent.putExtra("username", obtener_usuario_login());
            startActivity(intent);
        } else {
            boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guardar_estado_boton(true);
                    guardar_usuario_login(username.getText().toString());
                    login("http://198.199.105.120:8090/api/V1/service-cmp-access/oauth/token");
                }
            });
        }
        //textView = findViewById(R.id.textView1);
    }

    public void login(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), PrincipalActivity.class);
                    intent.putExtra("username", username.getText().toString());
                    intent.putExtra("password", password.getText().toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                params.put("grant_type", "password");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Authorization", "Basic ZnJvbnRlbmRhcHA6MTIzNDU=");
                Log.d("Network", headers.toString());
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void guardar_estado_boton(Boolean valor) {
        SharedPreferences settings = getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putBoolean(ESTADO_BOTON,valor);
        editor.apply();
    }

    public boolean obtener_estado_boton_login(){
        SharedPreferences settings = getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        return settings.getBoolean(ESTADO_BOTON,false);

    }

    public void guardar_usuario_login(String usuario) {
        SharedPreferences settings = getSharedPreferences(USER_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = settings.edit();
        editor.putString(ESTADO_USUARIO,usuario);
        editor.apply();
    }

    public String obtener_usuario_login(){
        SharedPreferences settings = getSharedPreferences(USER_KEY, MODE_PRIVATE);
        return settings.getString(ESTADO_USUARIO,"null");

    }
}