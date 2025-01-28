package com.yano.interplan.imble3_app;

import static com.yano.interplan.imble3_app.DLog.dlog;

import expo.modules.splashscreen.SplashScreenManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
//import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled;
import com.facebook.react.defaults.DefaultReactActivityDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import expo.modules.ReactActivityDelegateWrapper;

public class MainActivity extends ReactActivity {
    private static final int PERMISSION_REQUEST = 1000;
    private boolean initialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set the theme to AppTheme BEFORE onCreate to support
        // coloring the background, status bar, and navigation bar.
        // This is required for expo-splash-screen.
        setTheme(R.style.AppTheme);
        // @generated begin expo-splashscreen - expo prebuild (DO NOT MODIFY) sync-f3ff59a738c56c9a6119210cb55f0b613eb8b6af
    //    SplashScreenManager.registerOnActivity(this);
        // @generated end expo-splashscreen
        super.onCreate(null);
        if (isGranted()) {
            dlog("granted");
            // initBLE();
        } else {
            requestAndroidPermissions();
        }
    }

    private void initBLE() {
        dlog("");
        if (!initialized) {
            initialized = true;
            // BLEManager.getInstance();
        }
    }

    private void requestAndroidPermissions() {
        List<String> permissions = getCurrentPermissions();

        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST);
    }

    public boolean isGranted() {
        List<String> permissions = getCurrentPermissions();
        if (permissions.isEmpty()) return false;

        boolean check = true;
        for (String permission : permissions) {
            check &= ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return check;
    }

    private List<String> getCurrentPermissions() {
        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        } else {
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        return permissions;
    }

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    public String getMainComponentName() {
        return "main";
    }

    /**
     * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
     * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
     */
    @Override
    public ReactActivityDelegate createReactActivityDelegate() {
        return new DefaultReactActivityDelegate(
                this, Objects.requireNonNull(getMainComponentName()), DefaultNewArchitectureEntryPoint.getFabricEnabled());
    }

//    override fun
//
//    createReactActivityDelegate():
//
//    ReactActivityDelegate {
//        return ReactActivityDelegateWrapper(
//                this,
//                BuildConfig.IS_NEW_ARCHITECTURE_ENABLED,
//                object :DefaultReactActivityDelegate(
//                this,
//                mainComponentName,
//                fabricEnabled
//        ) {
//        })
//    }

    /**
     * Align the back button behavior with Android S
     * where moving root activities to background instead of finishing activities.
     *
     * @see <a href="https://developer.android.com/reference/android/app/Activity#onBackPressed()">onBackPressed</a>
     */
//    override fun
//
//    invokeDefaultOnBackPressed() {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
//            if (!moveTaskToBack(false)) {
//                // For non-root activities, use the default implementation to finish them.
//                super.invokeDefaultOnBackPressed()
//            }
//            return
//        }
//
//        // Use the default back button implementation on Android S
//        // because it's doing more than [Activity.moveTaskToBack] in fact.
//        super.invokeDefaultOnBackPressed()
//    }

    /**
     * @param requestCode  The request code passed in requestPermissions
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *                     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initBLE();
            } else {
                // 認証されなかった場合
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_title)
                        .setMessage(R.string.permission_message)
                        .setPositiveButton(
                                "OK",
                                (dialog, which) -> finish())
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}

