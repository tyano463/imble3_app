package com.yano.interplan.imble3_app;

import org.json.JSONObject;

public interface BLEModuleInterface {
    void onCallback(int result, JSONObject obj);
    void onDeviceFound(String address, int result, JSONObject data);
}
