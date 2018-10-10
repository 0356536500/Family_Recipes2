package com.myapps.ron.family_recipes.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class MiddleWareForNetwork {

    private static boolean InternetConnection = false;

    public static void setConnection(boolean connection) {
        InternetConnection = connection;
    }

    public static boolean getConnection() {
        return InternetConnection;
    }

    //region APICallHandler



    //endregion



    //region S3


    //endregion


    //region Cognito


    //endregion


    //check internet connection
    public static boolean checkInternetConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            //we are connected to a network
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            InternetConnection = activeNetwork != null && activeNetwork.isConnected();
            return InternetConnection;
            /*return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;*/
        }
        InternetConnection = false;
        return InternetConnection;
    }
}
