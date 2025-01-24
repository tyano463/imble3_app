import { View, Text, TouchableOpacity } from "react-native"
import { useNavigation, useGlobalSearchParams } from 'expo-router';

import { useEffect, useState } from "react";

export default function DeviceScreen() {
    const { address, name } = useGlobalSearchParams();
    const [connection_state, setConnectionState] = useState(false)
    const navigation = useNavigation()

    const strConnectionState = (val) => {
        return val ? "Connect" : "Disconnect"
    }

    const connect = () => {

    }

    useEffect(() => {
        if (name.length > 0)
            navigation.setOptions({ title: name })
    }, [navigation])

    return (
        <>
            <Text>{name}</Text>
            <Text>{address}</Text>
            <TouchableOpacity onPress={() => { connect(connection_state) }}>
                <Text>{strConnectionState(!connection_state)}</Text>
            </TouchableOpacity>
        </>
    )
}