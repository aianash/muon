package com.shoplane.muon.common.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * Created by ravmon on 24/8/15.
 */
public class JsonReadUtil {
    private static final String TAG = JsonReadUtil.class.getSimpleName();

    public static int getRequestId(String s) {
        int reqId = -1;
        try {
            JSONObject object = new JSONObject(s);
            reqId = object.getInt("requestid");
        } catch (JSONException je) {
            Log.e(TAG, "Failed to get requestid");
        }
        return reqId;
    }
}
