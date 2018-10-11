package com.myapps.ron.family_recipes.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.db.RecipesDBHelper;
import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.APICallsHandler;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.utils.SharedPreferencesHandler;

import java.util.List;
import java.util.Locale;


public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_TIME_OUT = 2200;

    private ProgressDialog waitDialog;
    // User Details
    private String username;
    private String password;

    RecipesDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppHelper.init(getApplicationContext());

        if(((MyApplication)getApplication()).checkInternetConnection())
            findCurrent();
        else if(SharedPreferencesHandler.getString(this, Constants.USERNAME) != null &&
                SharedPreferencesHandler.getString(this, Constants.PASSWORD) != null){
            Toast.makeText(this, "you are offline", Toast.LENGTH_LONG).show();
            launchMain();
        }
        else
            Toast.makeText(this, "Please connect to internet", Toast.LENGTH_SHORT).show();
        //writeToSharedPref();

        /*dbHelper = new RecipesDBHelper(this);
        APICallsHandler.getAllRecipes("0", null, new MyCallback<List<Recipe>>() {
            @Override
            public void onFinished(List<Recipe> result) {
                writeToDB(result);
                readFromDB();
            }
        });*/

        /*new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
            if (SharedPreferencesHandler.getBoolean(getApplicationContext(), "rememberMe")) {

            }
            }
        }, SPLASH_TIME_OUT);*/
    }

    private void writeToDB(List<Recipe> recipes) {
        Log.e(TAG, "writing to db");
        for (Recipe item : recipes) {
            if(dbHelper.recipeExists(item.getId()))
                dbHelper.updateRecipeServerChanges(item);
            else
                dbHelper.insertRecipe(item);
        }
    }

    private void readFromDB() {
        Log.e(TAG, "reading from db");
        List<Recipe> recipes = dbHelper.getAllRecipes(null);
        for (Recipe item : recipes) {
            Log.e(TAG, item.toString());
        }
    }

    private void writeToSharedPref() {
        SharedPreferencesHandler.writeString(getApplicationContext(), "username", "hello");
        SharedPreferencesHandler.writeString(getApplicationContext(), "password", "world");
        SharedPreferencesHandler.writeBoolean(getApplication(), "rememberMe", true);
    }

    private void findCurrent() {
        CognitoUser user = AppHelper.getPool().getCurrentUser();
        username = user.getUserId();
        //user saved in cache
        if(username != null) {
            Log.e(TAG, "username = " + username);
            AppHelper.setUser(username);
            user.getSessionInBackground(authenticationHandler);
        }
        //re-signing
        else {
            signInUser();
        }
    }

    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            Log.d(TAG, " -- Auth Success");
            AppHelper.setCurrSession(cognitoUserSession);
            AppHelper.newDevice(device);
            Log.e(TAG, "IDToken: " + cognitoUserSession.getIdToken().getJWTToken());
            Log.e(TAG, "AccessToken: " + cognitoUserSession.getAccessToken().getJWTToken());

            AppHelper.setIdentityProvider(getApplicationContext(), cognitoUserSession);

            closeWaitDialog();

            launchMain();
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
            closeWaitDialog();
            Locale.setDefault(Locale.US);
            getUserAuthentication(authenticationContinuation, username);
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            closeWaitDialog();
            //mfaAuth(multiFactorAuthenticationContinuation);
        }

        @Override
        public void onFailure(Exception e) {
            closeWaitDialog();
            //launchLogin();
            signInUser();
            Log.e(TAG, "Sign-in failed, " + AppHelper.formatException(e));
            //showDialogMessage("Sign-in failed", AppHelper.formatException(e));
        }

        /**
         * For Custom authentication challenge, implement your logic to present challenge to the
         * user and pass the user's responses to the continuation.
         */
        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            if ("NEW_PASSWORD_REQUIRED".equals(continuation.getChallengeName())) {
                Log.i(TAG, "NEW PASSWORD REQUIRED");
                // This is the first sign-in attempt for an admin created user
               /* newPasswordContinuation = (NewPasswordContinuation) continuation;
                AppHelper.setUserAttributeForDisplayFirstLogIn(newPasswordContinuation.getCurrentUserAttributes(),
                        newPasswordContinuation.getRequiredAttributes());
                closeWaitDialog();
                firstTimeSignIn();*/
            } else if ("SELECT_MFA_TYPE".equals(continuation.getChallengeName())) {
                closeWaitDialog();
                Log.i(TAG, "SELECT MFA TYPE");
                /*mfaOptionsContinuation = (ChooseMfaContinuation) continuation;
                List<String> mfaOptions = mfaOptionsContinuation.getMfaOptions();
                selectMfaToSignIn(mfaOptions, continuation.getParameters());*/
            }
        }
    };

    private void launchMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void signInUser() {
        username = SharedPreferencesHandler.getString(this, Constants.USERNAME);
        password = SharedPreferencesHandler.getString(this, Constants.PASSWORD);
        if(username != null && password != null) {
            AppHelper.setUser(username);

            showWaitDialog("Signing in...");
            AppHelper.getPool().getUser(username).getSessionInBackground(authenticationHandler);
        }
        else
            launchLogin();
    }

    private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
        if(username != null) {
            this.username = username;
            AppHelper.setUser(username);
        }
        if(this.password == null) {
            password = SharedPreferencesHandler.getString(this, Constants.PASSWORD);
            if(password == null) {
                Log.e(TAG, "enter password1");
                return;
            }

            if(password.length() < 1) {
                Log.e(TAG, "enter password2");
                return;
            }
        }
        AuthenticationDetails authenticationDetails = new AuthenticationDetails(this.username, password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }


    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        }
        catch (Exception e) {
            //
        }
    }

    private boolean checkInternetConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                //we are connected to a network
                return true;
            }
            else {
                return false;
            }
        }
        return false;
    }

}
