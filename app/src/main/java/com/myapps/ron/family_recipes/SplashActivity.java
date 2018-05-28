package com.myapps.ron.family_recipes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.myapps.ron.family_recipes.utils.SharedPreferencesHandler;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        writeToSharedPref();

        final String userParam = SharedPreferencesHandler.getString(this, "username");
        final String passParam = SharedPreferencesHandler.getString(this, "password");

        final Map<String, String> params = new HashMap<String, String>();
        params.put("username", userParam);
        params.put("password", passParam);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                if (SharedPreferencesHandler.getBoolean(getApplicationContext(), "rememberMe") == true) {
                                                                                                        // POST  /login
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getString(R.string.url_login), new JSONObject(params),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    if (response == null) {
                                        try {
                                            if (response.getInt(getApplicationContext().getString(R.string.server_response)) == 1) {
                                                Toast.makeText(getApplicationContext(), R.string.right_credentials, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                finish();
                                            } else if (response.getInt(getApplicationContext().getString(R.string.server_response)) == 0) {
                                                Toast.makeText(getApplicationContext(), R.string.wrong_credentials, Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                            }
                                        } catch (JSONException e) {
                                            Toast.makeText(getApplicationContext(), "response error", Toast.LENGTH_LONG).show();
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error in getting json
                            Log.e(TAG, "Error: " + error.getMessage());
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/json");
                            return params;
                        }

                    };
                    MyApplication.getInstance().addToRequestQueue(request);
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void writeToSharedPref() {
        SharedPreferencesHandler.writeString(getApplicationContext(), "username", "hello");
        SharedPreferencesHandler.writeString(getApplicationContext(), "password", "world");
        SharedPreferencesHandler.writeBoolean(getApplication(), "rememberMe", true);
    }
}
