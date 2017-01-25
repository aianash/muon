package com.shoplane.muon.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by ravmon on 24/8/15.
 */
public class NetworkConnectionUtil {
    private final String TAG = NetworkConnectionUtil.class.getSimpleName();

    public static boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (null != networkInfo) && networkInfo.isConnectedOrConnecting();
    }

}
