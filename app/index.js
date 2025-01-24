import { Image, StyleSheet, View, Text, TouchableOpacity, NativeEventEmitter, NativeModules, FlatList, } from 'react-native';

import { Link } from 'expo-router'
import { useEffect, useState } from 'react';
import ble from './native'

const dummy_data1 = {
  address: "C1:8E:45:A8:77:62"
  , name: "WestLK90-123456"
  , rssi: -90
}
const dummy_data2 = {
  address: "C1:8E:45:A8:77:64"
  , name: "WestLK90-123457"
  , rssi: -89
}

let before_str = ""

export default function HomeScreen() {
  const [data, setData] = useState([dummy_data1, dummy_data2])
  const [scan, setScan] = useState(false)

  useEffect(() => {
    const eventEmitter = new NativeEventEmitter(NativeModules.DeviceFound)
    let eventListener = eventEmitter.addListener('DEVICE_FOUND', event => {
      console.log("event received")
      if (event && event.json) {
        const newDevice = JSON.parse(event.json)
        setData(prevData => {
          // 同じaddressを持つデバイスを探す
          const index = prevData.findIndex(device => device.address === newDevice.address)

          if (index !== -1) {
            // 存在する場合は上書き
            const updatedData = [...prevData]
            updatedData[index] = newDevice
            return updatedData
          } else {
            // 存在しない場合は新しく追加
            return [...prevData, newDevice]
          }
        })
      }
    })
    return () => {
      eventListener.remove()
    }
  })

  const renderItem = ({ item }) => {
    // console.log("item:" + JSON.stringify(item))
    const mf = (item.mf || "").trim()
    // console.log("name:" + item.name + " address:" + item.address)
    if (before_str != ("" + mf)) {
      console.log("mf:" + mf)
      before_str = "" + mf
    }
    return (
      <Link style={styles.line} href={"./device?address=" + item.address + "&name=" + item.name}>
        <View style={styles.row}>
          <View style={styles.textContainer}>
            <Text style={styles.itemName}>{item.name}</Text>
          </View>
          <View style={styles.rightText}>
            <Text style={styles.itemAddress}>{`[${item.address}]`}</Text>
            <Text style={styles.itemRssi}>{" " + item.rssi}</Text>
          </View>
        </View>
      </Link>
    )
  }

  const startScan = (val) => {
    if (val) {
      setScan(false)
      ble.stop_scan(() => { })
    } else {
      setScan(true)
      ble.start_scan(() => { })
    }
  }

  const strScanState = (val) => {
    return val ? "SCAN" : "STOP"
  }
  return (
    <View>
      <Text>Hello world</Text>
      <TouchableOpacity onPress={() => { startScan(scan) }}>
        <Text>{strScanState(!scan)}</Text>
      </TouchableOpacity>
      <FlatList data={data} renderItem={renderItem} keyExtractor={(item) => item.address} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  button: {
    backgroundColor: '#ccc', // ボタンの背景色
    paddingVertical: 20,
    paddingHorizontal: 30,
    borderRadius: 10,
    elevation: 8, // Android用の立体感
    shadowColor: '#000',
    shadowOffset: { width: 2, height: 4 }, // iOS用の立体感
    shadowOpacity: 0.3,
    shadowRadius: 10,
    borderColor: '#888',
    borderWidth: 1,
  },
  button_text: {
    color: "#000",
  },
  button_frame: {
    flexDirection: 'row',
    justifyContent: 'space-evenly',
    alignItems: 'center',
    padding: 10,
    backgroundColor: '#f2f2f2', // 背景色
  },
  line: {
    flexDirection: 'row',
    marginBottom: 10,
    padding: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#ddd',
  },
  row: {
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  icon_batt: {
    width: 20,
    height: 40,
    marginRight: 10,
  },
  icon: {
    width: 40,
    height: 40,
    marginRight: 10,
  },
  textContainer: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  rightText: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: "right"
  },
  itemName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: "#000",
  },
  itemAddress: {
    fontSize: 14,
    color: '#555',
  },
  itemRssi: {
    fontSize: 14,
    textAlign: 'right',
    color: "#000",
  },
  buzzerContainer: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'flex-end',
  },
  button_buzzer: {
    backgroundColor: '#EEE'
  },
  itemBuzzer: {
    fontSize: 20,
  }
})
