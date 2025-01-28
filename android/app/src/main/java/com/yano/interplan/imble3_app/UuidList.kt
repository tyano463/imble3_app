/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yano.interplan.imble3_app

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
object UuidList {
    private val attributes = HashMap<String, String>()

    //Service UUID
    var SERVICE_IMBLE: String = "ada99a7f-888b-4e9f-8080-07ddc240f3ce"
    var SERVICE_IMBLE2: String = "ada98080-888b-4e9f-9a7f-07ddc240f3ce"
    var SERVICE_IMBLE3: String = "c2068080-562d-765b-f545-284b1448a270"
    var SERVICE_DEVICE_INFO: String = "0000180a-0000-1000-8000-00805f9b34fb"

    //Characteristics UUID
    var CHAR_READ: String = "ada99a7f-888b-4e9f-8081-07ddc240f3ce"
    var CHAR_WRITE: String = "ada99a7f-888b-4e9f-8082-07ddc240f3ce"
    var CHAR_READ2: String = "ada98081-888b-4e9f-9a7f-07ddc240f3ce"
    var CHAR_WRITE2: String = "ada98082-888b-4e9f-9a7f-07ddc240f3ce"
    var CHAR_READ3: String = "c2068081-562d-765b-f545-284b1448a270"
    var CHAR_WRITE3: String = "c2068082-562d-765b-f545-284b1448a270"

    //Descriptors UUID
    var CLIENT_CHARACTERISTIC_CONFIG: String = "00002902-0000-1000-8000-00805f9b34fb"

    init {
        //Services
        attributes[SERVICE_DEVICE_INFO] = "Device Information Service"
        attributes[SERVICE_IMBLE] = "IMBLE Data Exchange Service"
        attributes[SERVICE_IMBLE2] = "IMBLE2 Data Exchange Service"
        attributes[SERVICE_IMBLE3] = "IMBLE3 Data Exchange Service"

        //Characteristics
        attributes["00002a00-0000-1000-8000-00805f9b34fb"] = "Device Name String"
        attributes["00002a01-0000-1000-8000-00805f9b34fb"] = "Appearance"
        attributes["00002a02-0000-1000-8000-00805f9b34fb"] = "Peripheral Privacy Flag"
        attributes["00002a04-0000-1000-8000-00805f9b34fb"] =
            """
            Peripheral Preferred Connection
            Parameters
            """.trimIndent()
        attributes["00002a05-0000-1000-8000-00805f9b34fb"] = "Service Changed"
        attributes["00002a23-0000-1000-8000-00805f9b34fb"] = "System ID"
        attributes["00002a24-0000-1000-8000-00805f9b34fb"] = "Model Number String"
        attributes["00002a25-0000-1000-8000-00805f9b34fb"] = "Serial Number String"
        attributes["00002a26-0000-1000-8000-00805f9b34fb"] = "Firmware Revision String"
        attributes["00002a27-0000-1000-8000-00805f9b34fb"] = "Hardware Revision String"
        attributes["00002a28-0000-1000-8000-00805f9b34fb"] = "Software Revision String"
        attributes["00002a29-0000-1000-8000-00805f9b34fb"] = "Manufacturer Name String"
        attributes["00002a50-0000-1000-8000-00805f9b34fb"] = "PnP ID"
        attributes[CHAR_READ] = "IMBLE Data Read"
        attributes[CHAR_WRITE] = "IMBLE Data Write"
        attributes[CHAR_READ2] = "IMBLE2 Data Read"
        attributes[CHAR_WRITE2] = "IMBLE2 Data Write"
        attributes[CHAR_READ3] = "IMBLE3 Data Read"
        attributes[CHAR_WRITE3] = "IMBLE3 Data Write"
    }

//    fun lookup(uuid: String, defaultName: String): String {
//        val name = attributes[uuid]
//        return name ?: defaultName
//    }
}
