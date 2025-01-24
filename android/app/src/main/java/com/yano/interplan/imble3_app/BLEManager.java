package com.yano.interplan.imble3_app;

import static com.yano.interplan.imble3_app.DLog.b2s;
import static com.yano.interplan.imble3_app.DLog.dlog;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BLEManager {
    private final static byte BUZZER_ON = 0x10;
    private final static byte BUZZER_OFF = 0x20;
    private static BLEManager instance;
    private final BluetoothAdapter mAdapter;
    private BluetoothLeScanner mScanner;
    private ScanCallback mScanCallback;
    private ScanSettings mScanSettings;
    private static BLEModuleInterface callback;
    private UUID mUuid = UUID.fromString("386a8080-8e47-f49f-737b-2c9d44cd4680");
    private final UUID sUuidW = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private final UUID sUuidD = UUID.fromString("12345678-1234-1234-1234-123456789abd");
    private final List<String> mAddresses = new ArrayList<>();
    private boolean scanning;
    private final List<SendData> mSendList = new ArrayList<>();
    private final Object buzzer_lock = new Object();
    private BluetoothGattCallback mGattCallback;

    private BLEManager() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter != null && mAdapter.isEnabled()) {
            mScanner = mAdapter.getBluetoothLeScanner();
        }
        setScanCallback();
        setScanSettings();
        setGattCallback();
    }

    public static BLEManager getInstance() {
        if (instance == null) {
            instance = new BLEManager();
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void start_scan(BLEModuleInterface cb) {
        dlog("scanner:" + mScanner);
        if (mScanner != null) {
            callback = cb;
            scanning = true;
            mScanner.startScan(null, mScanSettings, mScanCallback);
        }
    }

    @SuppressLint("MissingPermission")
    public void stop_scan(BLEModuleInterface cb) {
        if (mScanner != null) {
            callback = cb;
            scanning = false;
            mScanner.stopScan(mScanCallback);
        }
    }

    public void scan_state(BLEModuleInterface cb) {
        if (cb != null) {
            JSONObject json = new JSONObject();
            try {
                json.put("state", scanning);
                cb.onCallback(0, json);
            } catch (Exception e) {
                dlog("" + e);
            }
        }
    }

    private void setGattCallback() {
        mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                super.onConnectionStateChange(gatt, status, newState);
                if (status == BluetoothGatt.STATE_CONNECTED) {
                    dlog("");
                    gatt.discoverServices();
                } else {
                    dlog("");
                }
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                super.onServicesDiscovered(gatt, status);
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    dlog("");
                    return;
                }
                BluetoothGattService service = gatt.getService(sUuidW);
                if (service == null) {
                    dlog("");
                    return;
                }
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(sUuidD);
                if (characteristic == null) {
                    dlog("");
                    return;
                }

                BluetoothDevice device = gatt.getDevice();
                if (device == null) {
                    dlog("");
                    return;
                }
                String address = device.getAddress();
                int index = findAddressIndex(address);
                SendData send_data = mSendList.get(index);
                if (send_data == null) {
                    dlog("");
                    return;
                }

                if (send_data.data == null || send_data.data.length == 0) {
                    dlog("");
                    return;
                }

                characteristic.setValue(send_data.data);
                gatt.writeCharacteristic(characteristic);
                dlog("write characteristic");
                send_data.data = null;
                synchronized (buzzer_lock) {
                    mSendList.remove(send_data);
                }
                if (!mSendList.isEmpty() && !scanning) {
                    start_scan(null);
                }
            }

            @Override
            public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
                super.onCharacteristicRead(gatt, characteristic, value, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
                super.onCharacteristicChanged(gatt, characteristic, value);
            }

            @Override
            public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
                super.onDescriptorRead(gatt, descriptor, status, value);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
            }

            @Override
            public void onServiceChanged(@NonNull BluetoothGatt gatt) {
                super.onServiceChanged(gatt);
            }
        };
    }

    public int findAddressIndex(String address) {
        if (address == null || address.isEmpty()) return -1;
        int pos = 0;
        synchronized (buzzer_lock) {
            for (SendData data : mSendList) {
                if (address.equals(data.address)) {
                    return pos;
                }
                pos++;
            }
        }
        return -1;
    }

    public void buzzer_ring(String address, boolean on, BLEModuleInterface cb) {
        if (cb == null) return;
        int index = findAddressIndex(address);

        if (index < 0) {
            synchronized (buzzer_lock) {
                mSendList.add(new SendData(address, on));
            }
        } else {
            synchronized (buzzer_lock) {
                SendData data = mSendList.get(index);
                if (data == null) return;
                data.ring = on;
            }
        }
        if (!scanning) {
            start_scan(null);
        }
    }

    public void set_uuid(String sUuid, BLEModuleInterface cb) {
        callback = cb;
        mUuid = UUID.fromString(sUuid);
    }

    private void setScanSettings() {
        mScanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
    }

    private void setScanCallback() {
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (result == null) return;
                BluetoothDevice device = result.getDevice();
                @SuppressLint("MissingPermission") String name = device.getName();
                String address = device.getAddress();
                int rssi = result.getRssi();
                ScanRecord record = result.getScanRecord();
                if (record == null) return;
                if (mUuid == null) return;
                List<ParcelUuid> uuids = record.getServiceUuids();
                if (uuids == null) return;
                boolean matched = false;
                for (ParcelUuid pUuid : uuids) {
                    if (mUuid.compareTo(pUuid.getUuid()) == 0) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) return;

                // int buzzer_index = findAddressIndex(address);
                // if (buzzer_index >= 0) {
                //     synchronized (buzzer_lock) {
                //         SendData buzzer = mSendList.get(buzzer_index);
                //         if (buzzer != null) {
                //             byte param = buzzer.ring ? BUZZER_ON : BUZZER_OFF;
                //             buzzer.data = new byte[]{param};
                //             connect_write(device, buzzer.data);
                //         }
                //     }
                // }

                if (callback == null) return;
                JSONObject json = new JSONObject();
                int ret = -1;
                try {
                    byte[] bmf = record.getManufacturerSpecificData(0x0212);
                    String mf = b2s(bmf);
                    json.put("mf", mf);
                    if (name == null)
                        name = "";
                    json.put("name", name);
                    json.put("address", address);
                    json.put("rssi", rssi);
                    ret = 0;
                } catch (Exception e) {
                    dlog("" + e);
                }

                dlog(name + " " + address + " " + ret + " " + json);
                callback.onDeviceFound(address, ret, json);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                dlog("");
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void connect_write(BluetoothDevice device, byte[] data) {
        if (device == null) return;
        if (data == null || data.length == 0) return;

        Context context = ContextHolder.getContext();
        if (context == null) {
            dlog("");
            return;
        }

        device.connectGatt(context, false, mGattCallback);
    }

    public void add_address(String _address, BLEModuleInterface cb) {
        if (_address == null) return;
        callback = cb;
        String address = _address.toUpperCase();
        if (!mAddresses.contains(address))
            mAddresses.add(address);
    }

    public void remove_address(String _address, BLEModuleInterface cb) {
        if (_address == null) return;
        callback = cb;
        String address = _address.toUpperCase();
        mAddresses.remove(address);
    }

    public void clear_address(BLEModuleInterface cb) {
        callback = cb;
        mAddresses.clear();
    }

    @SuppressLint("MissingPermission")
    public void pause_scan() {
        if (mScanner != null) {
            if (scanning) {
                mScanner.stopScan(mScanCallback);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void resume_scan() {
        if (mScanner != null) {
            if (scanning) {
                mScanner.startScan(null, mScanSettings, mScanCallback);
            }
        }
    }

    public void get_uuid(BLEModuleInterface cb) {
        callback = cb;
        JSONObject json = new JSONObject();
        try {
            json.put("uuid", mUuid.toString());
        } catch (Exception e) {
            dlog("" + e);
        }
        callback.onCallback(0, json);
    }

    static class SendData {
        public String address;
        public boolean ring;
        public byte[] data;

        public SendData(String address, boolean ring) {
            this.address = address;
            this.ring = ring;
        }
    }
}
