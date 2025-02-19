package com.yano.interplan.imble3_app

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import com.yano.interplan.imble3_app.DLog.dlog
import java.util.UUID

class BLEManager(private val context: Context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: BLEManager? = null

        @JvmStatic
        fun getInstance(context: Context): BLEManager {
            return instance ?: synchronized(this) {
                instance ?: BLEManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private var mConnectionCallback: BLEInterface? = null
    private val bluetoothAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null
    private var readCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private var scanCallback: ((BluetoothDevice, Int) -> Unit)? = null
    private val leScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            result.device?.let { device ->
                var imble = false
                val record = result.scanRecord
                val uuids = record?.serviceUuids?.iterator() ?: emptyList<ParcelUuid>().iterator()
                for (uuid in uuids) {
                    dlog("address:${device.address} uuid:$uuid")
                    if (isImBleUuid(uuid)) {
                        imble = true
                        break
                    }
                }
                if (imble) {
                    scanCallback?.invoke(device, result.rssi)
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            results.forEach { result ->
                result.device?.let { device ->
                    scanCallback?.invoke(device, result.rssi)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // スキャン失敗時の処理（必要に応じて拡張可能）
        }
    }

    private fun isImBleUuid(uuid: ParcelUuid?): Boolean {
        val sUuid = uuid?.toString() ?: return false
        return (UuidList.SERVICE_IMBLE == sUuid
                || UuidList.SERVICE_IMBLE2 == sUuid
                || UuidList.SERVICE_IMBLE3 == sUuid)
    }

    // スキャン開始
    @SuppressLint("MissingPermission")
    fun scanStart(callback: (BluetoothDevice, Int) -> Unit) {
        scanCallback = callback
        bluetoothLeScanner.startScan(leScanCallback)
    }

    // スキャン停止
    @SuppressLint("MissingPermission")
    fun scanStop() {
        bluetoothLeScanner.stopScan(leScanCallback)
        scanCallback = null
    }

    // 接続
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice, connectionCallback: BLEInterface) {
        dlog("" + device)
        mConnectionCallback = connectionCallback
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                dlog("$status -> $newState")
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    gatt.discoverServices()
                    mConnectionCallback?.onConnected(true)
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    mConnectionCallback?.onConnected(false)
                    bluetoothGatt = null
                    mConnectionCallback = null
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                val iterator =
                    gatt?.services?.iterator() ?: emptyList<BluetoothGattService>().iterator()
                dlog("iterator:$iterator")
                for (service in iterator) {
                    dlog("service:$service")
                    dlog("uuid:${service.uuid}")
                    for (char in service.characteristics) {
                        if (char.uuid.equals(UUID.fromString(UuidList.CHAR_READ))
                            || char.uuid.equals(UUID.fromString(UuidList.CHAR_READ2))
                            || char.uuid.equals(UUID.fromString(UuidList.CHAR_READ3))
                        ) {
                            readCharacteristic = char
                            gatt?.setCharacteristicNotification(readCharacteristic, true)
                            val descriptor =
                                readCharacteristic?.getDescriptor(UUID.fromString(UuidList.CLIENT_CHARACTERISTIC_CONFIG))
                            descriptor?.let {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    gatt?.writeDescriptor(
                                        it,
                                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                    )
                                } else {
                                    it.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                                    gatt?.writeDescriptor(it)
                                }
                                dlog("notify set")
                            }
                            dlog("read found:" + char.uuid)
                        } else if (char.uuid.equals(UUID.fromString(UuidList.CHAR_WRITE))
                            || char.uuid.equals(UUID.fromString(UuidList.CHAR_WRITE2))
                            || char.uuid.equals(UUID.fromString(UuidList.CHAR_WRITE3))
                        ) {
                            writeCharacteristic = char
                            dlog("write found:" + char.uuid)
                        }
                    }
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                dlog("")
                mConnectionCallback?.onReceived(value)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                dlog("to $characteristic st:$status")
            }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                dlog("" + value)
                if (value != null)
                    mConnectionCallback?.onReceived(value)
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                val value = characteristic.value
                dlog("" + value)
                if (value != null)
                    mConnectionCallback?.onReceived(value)
            }

            override fun onDescriptorWrite(gatt: BluetoothGatt , descriptor:BluetoothGattDescriptor, status:Int) {
                dlog("status:$status")
            }
        })
    }

    // 切断
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt = null
    }

    // データ送信
    @SuppressLint("MissingPermission")
    fun sendData(
        data: ByteArray,
    ) {
        val characteristic = writeCharacteristic

        if (characteristic != null) {
            val bluetoothGatt = bluetoothGatt ?: return

            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bluetoothGatt.writeCharacteristic(
                    characteristic,
                    data,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                characteristic.value = data
                bluetoothGatt.writeCharacteristic(characteristic)
            }

            dlog("Write success: $success")
        }
    }
}