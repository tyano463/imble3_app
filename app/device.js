import { View, Text, TouchableOpacity, StyleSheet, NativeEventEmitter, NativeModules } from "react-native"
import { useNavigation, useGlobalSearchParams } from 'expo-router';

import { useEffect, useState } from "react";
import ble from "./ble";

export default function DeviceScreen() {
    const { address, name } = useGlobalSearchParams();
    const [connection_state, setConnectionState] = useState(false)
    const navigation = useNavigation()
    const [mode, setMode] = useState("io")
    const [volt, setVolt] = useState(0)
    const [data, setData] = useState([false, false])

    const strConnectionState = (val) => {
        return val ? "Connect" : "Disconnect"
    }

    connect = (st) => {
        if (st) {
            ble.connect(address)
        } else {
            ble.disconnect(address)
        }
    }

    function toSwitch(hexString) {
        const d = parseInt(hexString, 16);

        const sw = [];

        for (let i = 0; i < 4; i++) {
            sw[i] = (d & (1 << i)) !== 0;
        }

        return sw;
    }

    useEffect(() => {
        connect(true)

        const eventEmitter = new NativeEventEmitter(NativeModules.DeviceFound)
        const eventListener = eventEmitter.addListener('DEVICE_FOUND', event => {
            if (event && event.json) {
                const result = JSON.parse(event.json)
                console.log(result)
                if (result.connect) {
                    setConnectionState(true)
                } else {
                    setConnectionState(false)
                }
                if (result.data && result.data.length >= 2) {
                    console.log("data:" + result.data)
                    const switch_data = toSwitch(result.data)
                    setData(switch_data.slice(-2))
                }
            }
        })
        return () => {
            eventListener.remove()
            ble.disconnect(address)
        }
    }, [])

    useEffect(() => {
        if (name.length > 0)
            navigation.setOptions({ title: name })
    }, [navigation])

    send_data = (index) => {
        if (0 <= index && index <= 3) {
            const s = (1 << index).toString(16).padStart(2, '0');
            ble.send_data(s);
        }
    }

    return (
        <>
            <Text>{name}</Text>
            <Text>{address}</Text>
            <View style={styles.button_frame}>
                <TouchableOpacity onPress={() => { connect(true) }} style={[
                    styles.button,
                    connection_state && styles.button_disabled,
                ]}>
                    <Text>{strConnectionState(true)}</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={() => { connect(false) }} style={[
                    styles.button,
                    !connection_state && styles.button_disabled,
                ]} >

                    <Text>{strConnectionState(false)}</Text>
                </TouchableOpacity>
            </View>

            <Text>{"電圧:" + volt + "v"}</Text>

            <View style={styles.switch_container}>
                {data.map((value, index) => (
                    <View key={index} style={styles.box}>
                        <Text style={styles.text}>{"sample:" + value}</Text>
                    </View>
                ))}
            </View>

            <Text>{"doko?"}</Text>
            <TouchableOpacity onPress={() => { send_data(2) }} >
            </TouchableOpacity>
            <TouchableOpacity onPress={() => { send_data(3) }} >
            </TouchableOpacity>
        </>
    )
}
const styles = StyleSheet.create({
    container: {
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
    button_disabled: {
        backgroundColor: '#ddd',
        shadowColor: 'transparent',
        borderColor: '#bbb',
    },
    button_text: {
        color: "#000",
    },
    button_text_disabled: {
        color: '#bbb',
    },
    button_frame: {
        flexDirection: 'row',
        justifyContent: 'space-evenly',
        alignItems: 'center',
        padding: 10,
        backgroundColor: '#f2f2f2', // 背景色
    },
    switch_container: {
        flexDirection: 'row',
        justifyContent: 'space-around',
        padding: 16,
    },
    box: {
        width: 100,
        height: 100,
        justifyContent: 'center',
        alignItems: 'center',
        borderColor: '#000',
        borderRadius: 8,
    },
    text: {
        color: 'white',
        fontSize: 16,
        fontWeight: 'bold',
    },
})