package com.myapps.ron.family_recipes;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.myapps.ron.family_recipes.utils.SharedPreferencesHandler;


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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Login");

        username = findViewById(R.id.user_search);
        password = findViewById(R.id.pswd);
        loginButton = findViewById(R.id.loginBtn);
        registerButton = findViewById(R.id.registerBtn);
        cb_remember_me = findViewById(R.id.cb_remember);
        context = this.getApplicationContext();

        username.setText(SharedPreferencesHandler.getString(context, "username"));
        password.setText(SharedPreferencesHandler.getString(context, "password"));
    }
}
