import NativeBLE from "./specs/NativeBLE";

const EMPTY = '<empty>'

const ble = {
    start_scan: async (cb) => {
        console.log("start_scan")
        NativeBLE?.start_scan()
    },
    stop_scan: async (cb) => {
        console.log("stop_scan")
        NativeBLE?.stop_scan()
    },
    send_data: async(s) => {
        NativeBLE?.send_data(s)
    },
    connect: async(addr) => {
        console.log("connect")
        NativeBLE?.connect(addr)
    },
    disconnect: async(addr) => {
        console.log("disconnect")
        NativeBLE?.disconnect()
    }
}

export default ble