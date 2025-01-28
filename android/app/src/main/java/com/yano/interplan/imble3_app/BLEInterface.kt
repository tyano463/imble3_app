package com.yano.interplan.imble3_app

interface BLEInterface {
    fun onConnected(isConnected: Boolean)
    fun onReceived(data: ByteArray)
}