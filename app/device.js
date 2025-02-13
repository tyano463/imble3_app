import { View, ImageBackground, Dimensions, Image, Text, TouchableOpacity, StyleSheet, NativeEventEmitter, NativeModules } from "react-native"
import { useNavigation, useGlobalSearchParams } from 'expo-router';

import { useEffect, useState } from "react";
import ble from "./ble";
import * as D from './definitions'

export default function DeviceScreen() {
    const { address, name } = useGlobalSearchParams();
    const [connection_state, setConnectionState] = useState(false)
    const navigation = useNavigation()
    const [mode, setMode] = useState("io")
    const [volt, setVolt] = useState(0)
    const [data, setData] = useState([false, false])
    const [isToggled, setIsToggled] = useState(false)
    const [aspectRatio, setAspectRatio] = useState(1)

    const BG_IMAGE = require('../assets/images/bg.png')
    const screenWidth = Dimensions.get('window').width
    const image = Image.resolveAssetSource(BG_IMAGE)

    const handleToggle = () => {
        setIsToggled(!isToggled)
    }
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
                    console.log("sw data:" + switch_data)
                    setData(switch_data.slice(0, 2))
                } else if (result.volt && result.volt.length >= 4) {
                    const volt_value = parseInt(result.volt, 16)
                    setVolt(((3 * volt_value) / 65535).toFixed(2))
                }
            }
        })
        setAspectRatio(image.height / image.width)
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
            ble.send_data("05000000" + s);
        }
    }

    const getButtonStr = (val) => {
        return val ? D.MESSAGE_BUTTON_PUSSHED : ''
    }

    return (
        <>
            <Image source={BG_IMAGE}
                style={[styles.backgroundImage, { width: screenWidth, height: screenWidth * aspectRatio }]}
                resizeMode="cover" />
            <View style={styles.overlay}>
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

                <View style={[styles.ad_frame, !isToggled && { opacity: 0 }]} >
                    <Text style={styles.ad_text}>{"電圧: " + volt + "v"}</Text>
                </View>
                <View style={styles.toggle_container}>
                    <TouchableOpacity
                        style={[styles.switch, isToggled ? styles.toggledOn : styles.toggledOff]}
                        onPress={handleToggle}
                    >
                        <View style={[styles.toggleCircle, isToggled ? styles.circleOn : styles.circleOff]} />
                    </TouchableOpacity>
                    <Text style={styles.label}>{isToggled ? 'A/D mode' : 'I/O mode'}</Text>
                </View>
                <View style={[styles.io_frame, isToggled && { opacity: 0 }]}>


                    <View style={styles.led_frame}>
                        <TouchableOpacity onPress={() => { send_data(0) }} style={styles.led_button}>
                            <Text style={styles.led_text}>{D.MESSAGE_TAP_SUGGEST}</Text>
                        </TouchableOpacity>
                        <TouchableOpacity onPress={() => { send_data(1) }} style={styles.led_button}>
                            <Text style={styles.led_text}>{D.MESSAGE_TAP_SUGGEST}</Text>
                        </TouchableOpacity>
                    </View>
                    <View style={styles.switch_container}>
                        <View style={[styles.box, { opacity: 0 }]} >
                            <Text style={[styles.sw_text, { opacity: 0 }]} >{D.MESSAGE_BUTTON_PUSSHED}</Text>
                        </View>
                        {data.map((value, index) => (
                            <View key={index} style={[styles.box, !value && { opacity: 0 }]}>
                                <Text style={styles.sw_text}>{D.MESSAGE_BUTTON_PUSSHED}</Text>
                            </View>
                        ))}
                    </View>
                </View>
            </View >
        </>
    )
}
const styles = StyleSheet.create({
    backgroundImage: {
        ...StyleSheet.absoluteFillObject,
        marginTop: 80,
    },
    overlay: {
        flex: 1,
        justifyContent: 'flex-start',
        alignItems: 'center',
    },
    container: {
        flex: 1,
    },
    button: {
        backgroundColor: '#ccc',
        paddingVertical: 20,
        paddingHorizontal: 30,
        borderRadius: 10,
        elevation: 8,
        shadowColor: '#000',
        shadowOffset: { width: 2, height: 4 },
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
    toggle_container: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        marginTop: 10,
        marginBottom: 10,
        paddingLeft: 30,
        height: '10%',
    },
    switch_container: {
        flexDirection: 'row',
        justifyContent: 'space-around',
        alignItems: 'center',
        height: '40%',
        position: 'absolute',
        marginTop: '30%',
    },
    switch: {
        width: 30,
        height: 70,
        borderRadius: 30,
        padding: 5,
        backgroundColor: '#ccc',
        marginRight: 10,
    },
    toggledOn: {
        backgroundColor: '#e74c3c',
    },
    toggledOff: {
        backgroundColor: '#4cd137',
    },
    toggleCircle: {
        width: 20,
        height: 20,
        borderRadius: 10,
        backgroundColor: 'white',
    },
    circleOn: {
        marginTop: 0,
    },
    circleOff: {
        marginTop: 40,
    },
    label: {
        fontSize: 18,
        backgroundColor: '#ffffff99',
    },
    ad_frame: {
        width: '80%',
        height: '10%',
        marginTop: '58%',
        backgroundColor: 'rgba(255, 255, 255, 0.3)',
        borderRadius: 8,
        alignSelf: 'center',
    },
    ad_text: {
        color: '#fff',
        fontSize: 40,
        alignSelf: 'center',
    },
    io_frame: {
        width: '80%',
        backgroundColor: 'rgba(255, 255, 255, 0.3)',
        borderRadius: 16,
        alignSelf: 'center',
    },
    box: {
        width: '30%',
        height: '70%',
        borderColor: '#000',
        borderRadius: 8,
        backgroundColor: '#191970',
        alignItems: 'center',
        justifyContent: 'center',
        marginHorizontal: 5,
    },
    sw_text: {
        color: 'white',
        fontSize: 24,
        fontWeight: 'bold',
    },
    led_frame: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        padding: 10,
        height: '46%',
        marginLeft: '20%',
    },
    led_button: {
        backgroundColor: 'rgba(255,100,100,0.8)',
        width: '50%',
        height: '70%',
        borderColor: '#000',
        borderRadius: 8,
        justifyContent: 'center',
        paddingLeft: '5%',
        paddingRight: '5%',
        alignItems: 'center',
        marginHorizontal: 5,
    },
    led_text: {
        color: '#fff',
        fontSize: 24,
        fontWeight: 'bold',
    },
})