package com.shoplane.muon.common.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by ravmon on 9/10/15.
 */
public class JsonWriteUtil {
    private static final String TAG = JsonWriteUtil.class.getSimpleName();

    public static JSONObject writeJSON(Map<String, String> keyValuePair) {
        JSONObject object = new JSONObject();
        try {
            object.put("name", "Jack Hack");
            object.put("score", new Integer(200));
            object.put("current", new Double(152.32));
            object.put("nickname", "Hacker");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create json object");
        }
        return object;
    }

}
