package com.yano.interplan.imble3_app


import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.facebook.react.bridge.Arguments

import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule

import com.yano.interplan.imble3_app.DLog.dlog
import com.yano.interplan.imble3_app.NativeBLESpec
import org.json.JSONObject

class BLEModule(reactContext: ReactApplicationContext?) : NativeBLESpec(reactContext),
    BLEInterface {

    private var mManager: BLEManager? = null
    private val foundedDevices: HashMap<String, BluetoothDevice> = HashMap()
    private var lastConnected: String? = null

    init {
        ContextHolder.init(reactContext)
    }

    override fun getName() = NAME

    @SuppressLint("MissingPermission")
    override fun start_scan() {
        dlog("")
        manager.scanStart { d: BluetoothDevice, rssi: Int ->

            foundedDevices[d.address] = d

            val jsonObject: JSONObject = deviceToJson(d)

            jsonObject.put("rssi", rssi)
            val json = jsonObject.toString()

            dlog(json)
            sendEvent(json)
        }

    }

    @SuppressLint("MissingPermission")
    private fun deviceToJson(d: BluetoothDevice): JSONObject {

        val address = d.address
        val name = d.name ?: ""

        val obj = JSONObject().apply {
            put("name", name)
            put("address", address)
        }
        return obj
    }

    override fun stop_scan() {
        dlog("")
        manager.scanStop()
    }

    @SuppressLint("MissingPermission")
    override fun connect(address: String?) {
        val d = foundedDevices.get(address)
        dlog("addr:" + address + " dev:" + d)
        lastConnected = address
        if (d != null) {
            manager.connect(d, this)
        }
    }

    override fun disconnect() {
        manager.disconnect()
    }

    override fun send_data(s: String?) {
        val data: ByteArray = Misc.s2b(s)
        if (data.isNotEmpty())
            manager.sendData(data)
        else
            dlog("empty data -> $s")
    }

    private val manager: BLEManager
        get() {
            if (mManager == null) {
                mManager =
                    BLEManager.getInstance((getReactApplicationContext() as Context?)!!)
            }
            return mManager!!
        }

    private fun sendEvent(data: String) {
        val params = Arguments.createMap()
        params.putString("json", data)

        val context = ContextHolder.getContext()
        val module = context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)

        if (module == null) {
            dlog("")
            return
        }

        module.emit(EVENT_NAME, params)
    }

    override fun onConnected(isConnected: Boolean) {
        val d = foundedDevices[lastConnected] ?: return
        val obj = deviceToJson(d)
        obj.put("connect", isConnected)
        val json = obj.toString()
        sendEvent(json)
        dlog("connected: $isConnected")
    }

    override fun onReceived(data: ByteArray) {
        val d = foundedDevices[lastConnected] ?: return
        val obj = deviceToJson(d)
        obj.put("connect", true)
        if (data[0] == 0x5.toByte()) {
            val dataStr = String.format("%02x", data[4]);
            obj.put("data", dataStr)
            val json = obj.toString()
            sendEvent(json)
        } else {
            dlog("data:" + data[0])
        }
    }

    companion object {
        const val NAME = "BLEModule"
        const val EVENT_NAME = "DEVICE_FOUND"
    }
}

