package com.myapps.ron.family_recipes.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
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
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MiddleWareForNetwork;
import com.myapps.ron.family_recipes.network.cognito.AppHelper;
import com.myapps.ron.family_recipes.utils.SharedPreferencesHandler;

import java.util.Locale;


public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    //private static final int SPLASH_TIME_OUT = 2200;

    private ProgressDialog waitDialog;
    // User Details
    private String username;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Launcher);
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash);

        AppHelper.init(getApplicationContext());

        if(MiddleWareForNetwork.checkInternetConnection(this))
            findCurrent();
        else if(SharedPreferencesHandler.getString(this, Constants.USERNAME) != null &&
                SharedPreferencesHandler.getString(this, Constants.PASSWORD) != null){
            Toast.makeText(this, "you are offline", Toast.LENGTH_LONG).show();
            launchMain();
        }
        else
            Toast.makeText(this, "Please connect to internet and then log in", Toast.LENGTH_LONG).show();
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
            //Log.e(TAG, "IDToken: " + cognitoUserSession.getIdToken().getJWTToken());
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
        if (getIntent() != null) {
            Intent receivedIntent = getIntent();
            String recipeId = receivedIntent.getStringExtra(com.myapps.ron.family_recipes.utils.Constants.RECIPE_ID);
            if (recipeId != null && !"".equals(recipeId)) {
                Intent recipeIntent = new Intent(SplashActivity.this, RecipeActivity.class);
                recipeIntent.putExtra(com.myapps.ron.family_recipes.utils.Constants.RECIPE_ID, recipeId);
                recipeIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(recipeIntent);
                finish();
            } else {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
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

            String message = "Signing in...";
            showWaitDialog(message);
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

}
