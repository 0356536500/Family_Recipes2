package com.myapps.ron.family_recipes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.myapps.ron.family_recipes.utils.SharedPreferencesHandler;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    EditText username;
    EditText password;
    Button loginButton;
    Button registerButton;
    Context context;
    CheckBox cb_remember_me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Login");

        username = (EditText) findViewById(R.id.user_search);
        password = (EditText) findViewById(R.id.pswd);
        loginButton = (Button) findViewById(R.id.loginBtn);
        registerButton = (Button) findViewById(R.id.registerBtn);
        cb_remember_me = (CheckBox) findViewById(R.id.cb_remember);
        context = this.getApplicationContext();

        username.setText(SharedPreferencesHandler.getString(context, "username"));
        password.setText(SharedPreferencesHandler.getString(context, "password"));
    }

    public void login(final View v) {

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username.getText().toString());
        params.put("password", password.getText().toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, getString(R.string.url_login), new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response == null) {
                            try {
                                if (response.getInt(getApplicationContext().getString(R.string.server_response)) == 1) {

                                    if (cb_remember_me.isChecked()) {

                                        SharedPreferencesHandler.writeString(context, "username", username.getText().toString());
                                        SharedPreferencesHandler.writeString(context, "password", password.getText().toString());
                                        SharedPreferencesHandler.writeBoolean(context, "rememberMe", true);

                                    }
                                    Toast.makeText(getApplicationContext(), R.string.right_credentials, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else if (response.getInt(getApplicationContext().getString(R.string.server_response)) == 0) {
                                    Toast.makeText(getApplicationContext(), R.string.wrong_credentials, Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
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

    public void register(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

}
