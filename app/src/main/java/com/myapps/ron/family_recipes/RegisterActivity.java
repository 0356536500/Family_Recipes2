package com.myapps.ron.family_recipes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    EditText editFullName, editUserName, editPassword, editEmail;
    Button register;
    String responseStatus = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindUI();

    }
    private void bindUI() {

        editFullName = findViewById(R.id.register_fullname);
        editUserName = findViewById(R.id.register_username);
        editPassword = findViewById(R.id.register_email);
        editEmail = findViewById(R.id.register_email);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Register");
        register = findViewById(R.id.register_button);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerLogic();
            }
        });
    }

    private void registerLogic() {
        if(editFullName.getText().toString() != "" && editUserName.getText() != null
                && editPassword.getText() != null && editEmail.getText() != null){
            signUp();
        }
    }

    private void signUp() {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", editUserName.getText().toString());
            jsonBody.put("password", editPassword.getText().toString());
            jsonBody.put("name", editFullName.getText().toString());
            jsonBody.put("email", editEmail.getText().toString());

            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.url_signup), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("VOLLEY", response);

                    if (!response.contains("already exists") || responseStatus.equals("200")) {
                        Toast.makeText(getApplicationContext(), R.string.right_credentials, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.wrong_credentials, Toast.LENGTH_LONG).show();
                        //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                    Toast.makeText(getApplicationContext(), "error: " + getString(R.string.wrong_credentials), Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        responseStatus = responseString;
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            MyApplication.getInstance().addToRequestQueue(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void signUp1(){
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", editUserName.getText().toString());
        params.put("password", editPassword.getText().toString());
        params.put("name", editFullName.getText().toString());
        params.put("email", editEmail.getText().toString());

        StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.url_signup),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null) {

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
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }



        };
        MyApplication.getInstance().addToRequestQueue(request);
    }


}
