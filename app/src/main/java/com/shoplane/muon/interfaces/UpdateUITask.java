package com.shoplane.muon.interfaces;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by ravmon on 11/10/15.
 */
public interface UpdateUITask {
    public boolean getActivityAvailableStatus();
    public void updateUI(JSONObject dataToUpdate);
}
