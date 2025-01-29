import { View, Image, Text } from 'react-native';
import { DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { useFonts } from 'expo-font';
import { Stack } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { useEffect } from 'react';
import 'react-native-reanimated';

// Prevent the splash screen from auto-hiding before asset loading is complete.
SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const APP_NAME = 'IMBLE3 デモアプリ'
  const [loaded] = useFonts({
    SpaceMono: require('../assets/fonts/SpaceMono-Regular.ttf'),
  });

  useEffect(() => {
    if (loaded) {
      SplashScreen.hideAsync();
    }
  }, [loaded]);

  if (!loaded) {
    return null;
  }

  return (
    <ThemeProvider value={DefaultTheme}>
      <Stack>
        <Stack.Screen name="index" options={{
          headerTitle: () => (
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <Image source={require("../assets/images/icon_orig.png")} style={{ width: 40, height: 40 }} />
              <Text style={{ marginLeft: 8, fontSize: 24, fontWeight: "bold" }}>{APP_NAME}</Text>
            </View>
          ),
        }} />
        <Stack.Screen name="device" options={{ title: 'Unknown Device Name' }} />
      </Stack>
    </ThemeProvider>
  );
}
