package com.shoplane.muon.common.communication;

import android.app.Activity;
import android.util.Log;

import com.shoplane.muon.interfaces.UpdateUITask;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ravmon on 9/10/15.
 */
public class Ring {
    private static final String TAG = Ring.class.getSimpleName();

    private int mMaxActiveRequest;
    private int mSizeOfRing;
    private ConcurrentHashMap<Integer, Boolean> mActiveRequestMap;
    private int mCurrentReqId;
    private ConcurrentHashMap<String, WeakReference<UpdateUITask>> mUpdateTaskMapForGetRequest;
    private ConcurrentHashMap<String, WeakReference<UpdateUITask>> mUpdateTaskMapForPostRequest;
    private ConcurrentHashMap<Integer, String> mRequestIdToUriMap;
    private ConcurrentHashMap<String, Integer> mRequestUriToIdMap;

    public Ring(int maxRequests) {
        this.mMaxActiveRequest = maxRequests;
        this.mSizeOfRing = 2 * maxRequests;
        this.mUpdateTaskMapForGetRequest = new ConcurrentHashMap<>();
        this.mUpdateTaskMapForPostRequest = new ConcurrentHashMap<>();
        this.mRequestIdToUriMap = new ConcurrentHashMap<>();
        this.mRequestUriToIdMap = new ConcurrentHashMap<>();
        this.mCurrentReqId = 1;
        this.mActiveRequestMap = new ConcurrentHashMap<>();
        for (int i = 1; i <= mSizeOfRing; i++) {
            mActiveRequestMap.put(i, false);
        }
    }

    public boolean isRequestActive(int index) {
        return (mActiveRequestMap.get(index));
    }


    public synchronized String registerRequest(JSONObject reqJson,
                                              WeakReference<UpdateUITask> actRef,
                                              String queryPath) {
        // cancel older request if that is not processed
        Integer oldReq = mRequestUriToIdMap.get(queryPath);
        if (oldReq != null) {
            Log.i(TAG, "Removing old request with same path");
            clearRequest(oldReq);
        }

        if (mCurrentReqId == (mSizeOfRing + 1)) {
            mCurrentReqId = 1;
        }

        int newReqId = mCurrentReqId++;

        // add reqid and ts to json request
        try {
            reqJson.put("reqid", newReqId + "");
            reqJson.put("timestamp", System.currentTimeMillis() + "");
        } catch (JSONException je) {
            Log.e(TAG, "Failed to add requestid to get request");
        }

        mActiveRequestMap.put(newReqId, true);
        mRequestIdToUriMap.put(newReqId, queryPath);
        mRequestUriToIdMap.put(queryPath, newReqId);
        mUpdateTaskMapForGetRequest.put(queryPath, actRef);

        // clear old request
        clearRequest((newReqId + mMaxActiveRequest) % mSizeOfRing);

        return reqJson.toString();

    }

    public synchronized void clearRequest(int oldReq) {
        if (mActiveRequestMap.containsKey(oldReq)) {
            if (mActiveRequestMap.get(oldReq)) {
                mActiveRequestMap.put(oldReq, false);
                if (mRequestIdToUriMap.containsKey(oldReq)) {
                    mUpdateTaskMapForGetRequest.remove(mRequestIdToUriMap.get(oldReq));
                    mRequestUriToIdMap.remove(mRequestIdToUriMap.get(oldReq));
                }
                mRequestIdToUriMap.remove(oldReq);
            }
        }
    }

    public WeakReference<UpdateUITask> getTaskToUpdateUIForGetRequest(int reqId) {
        if (mRequestIdToUriMap.containsKey(reqId)) {
            String path = mRequestIdToUriMap.get(reqId);
            if (path != null) {
                if (mUpdateTaskMapForGetRequest.containsKey(path)) {
                    return mUpdateTaskMapForGetRequest.get(path);
                }
            }
        }
        return null;
    }

    public WeakReference<UpdateUITask> getTaskToUpdateUIForPostRequest(String messageType) {
        messageType = messageType.trim().toLowerCase();
        if (mUpdateTaskMapForPostRequest.containsKey(messageType)) {
            return mUpdateTaskMapForPostRequest.get(messageType);
        }

        return null;
    }


    public void addTaskToUpdateUIForPostRequest(String messageType,
                                                        WeakReference<UpdateUITask> actWeakRef) {
        messageType = messageType.trim().toLowerCase();
        mUpdateTaskMapForPostRequest.put(messageType, actWeakRef);
    }

    public void removeTaskToUpdateUIForPostRequest(String messageType) {
        messageType = messageType.trim().toLowerCase();
        mUpdateTaskMapForPostRequest.remove(messageType);
    }
}
