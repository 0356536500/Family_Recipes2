package com.myapps.ron.family_recipes.layout;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class MiddleWareForNetwork {

    private static boolean internetConnection = false;

    public static void setConnection(boolean connection) {
        internetConnection = connection;
    }

    public static boolean getConnection() {
        return internetConnection;
    }


    //check internet connection
    public static boolean checkInternetConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            //we are connected to a network
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            internetConnection = activeNetwork != null && activeNetwork.isConnected();
            return internetConnection;
            //return false;
            /*return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;*/
        }
        return internetConnection;
    }
}
