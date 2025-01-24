import { NativeModules } from "react-native";

const native = NativeModules.BLEModule

const ble = { 
    start_scan: async (cb) => {
        console.log("start_scan" + native)
        native.start_scan(cb)
    },  
    stop_scan: async (cb) => {
        native.stop_scan(cb)
    },  
    set_uuid: async (uuid, cb) => {
        native.set_uuid(uuid, cb) 
    },  
    get_uuid: async (cb) => {
        native.get_uuid(cb)
    },  
    scan_state: async(cb) => {
        native.scan_state(cb)
    },  
    buzzer_ring: async(address, cb) => {
        native.buzzer_ring(1, address, cb) 
    }   
}

export default ble