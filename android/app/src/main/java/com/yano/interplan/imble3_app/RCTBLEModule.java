package com.yano.interplan.imble3_app;

import static com.yano.interplan.imble3_app.DLog.dlog;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

enum BLECallbackKind {
    NONE,
    START,
    STOP,
    STATE,
    SET_UUID,
    GET_UUID,
    BUZZER,
    MAC,
}

public class RCTBLEModule extends ReactContextBaseJavaModule implements BLEModuleInterface {
    private static final String EVENT_NAME = "DEVICE_FOUND";

    RCTBLEModule(ReactApplicationContext context) {
        super(context);
    }

    Map<BLECallbackKind, Callback> map = new HashMap<>();
    Map<String, DeviceData> mData = new HashMap<>();

    @NonNull
    @Override
    public String getName() {
        return "RCTBLEModule";
    }

    @ReactMethod
    public void start_scan(Callback cb) {
        map.clear();
        map.put(BLECallbackKind.START, cb);
        BLEManager.getInstance().start_scan(this);
    }

    @ReactMethod
    public void stop_scan(Callback cb) {
        map.clear();
        map.put(BLECallbackKind.STOP, cb);
        BLEManager.getInstance().stop_scan(this);
    }

    @ReactMethod
    public void set_uuid(String uuid, Callback cb) {
        map.clear();
        map.put(BLECallbackKind.SET_UUID, cb);
        BLEManager.getInstance().set_uuid(uuid, this);
    }

    @ReactMethod
    public void add_address(String address, Callback cb) {
        map.clear();
        map.put(BLECallbackKind.MAC, cb);
        BLEManager.getInstance().add_address(address, this);
    }

    @ReactMethod
    public void remove_address(String address, Callback cb) {
        map.clear();
        map.put(BLECallbackKind.MAC, cb);
        BLEManager.getInstance().remove_address(address, this);

    }

    @ReactMethod
    public void clear_address(Callback cb) {
        map.clear();
        map.put(BLECallbackKind.MAC, cb);
        BLEManager.getInstance().clear_address(this);
    }

    @ReactMethod
    public void get_uuid(Callback cb) {
        map.clear();
        map.put(BLECallbackKind.GET_UUID, cb);
        BLEManager.getInstance().get_uuid(this);
    }

    @ReactMethod
    public void scan_state(Callback cb) {
        map.clear();
        map.put(BLECallbackKind.STATE, cb);
        BLEManager.getInstance().scan_state(this);
    }

    @ReactMethod
    public void buzzer_ring(String address,boolean on, Callback cb) {
        map.clear();
        map.put(BLECallbackKind.BUZZER, cb);
        BLEManager.getInstance().buzzer_ring(address, on, this);
    }

    @Override
    public void onCallback(int result, JSONObject obj) {
        for (BLECallbackKind key : map.keySet()) {
            if (!map.containsKey(key)) continue;
            Callback cb = map.get(key);
            if (cb != null)
                cb.invoke(result, obj.toString());
        }
    }

    @Override
    public void onDeviceFound(String address, int result, JSONObject data) {
        DeviceData dd = mData.get(address);
        if (dd == null) {
            dd = new DeviceData();
        } else {
            if (dd.result == result && dd.address.equals(address) && dd.data.equals(data.toString())) {
                return;
            }
        }

        dd.address = address;
        dd.result = result;
        dd.data = data.toString();
        mData.put(address, dd);

        dlog(address + " " + result + " " + data);
        if (result == 0)
            sendEvent(data.toString());
    }

    private void sendEvent(String data) {
        WritableMap params = Arguments.createMap();
        params.putString("json", data);
        ReactApplicationContext context = ContextHolder.getContext();
        DeviceEventManagerModule.RCTDeviceEventEmitter module = context
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        if (module == null) {
            dlog("");
            return;
        }

        module.emit(EVENT_NAME, params);
    }

    static class DeviceData {
        String address;
        int result;
        String data;
    }
}
