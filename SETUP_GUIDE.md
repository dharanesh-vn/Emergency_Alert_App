# Emergency Alert App - Setup Guide

## Quick Start Guide

### Option 1: Open in Android Studio (Recommended)

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install with default settings

2. **Open Project**
   ```
   - Launch Android Studio
   - Click "Open"
   - Navigate to EmergencyAlertApp folder
   - Click "OK"
   ```

3. **Wait for Gradle Sync**
   - Android Studio will automatically download dependencies
   - This may take 5-10 minutes on first run
   - Check bottom status bar for progress

4. **Add App Icon (Optional)**
   - Copy a PNG icon to: `app/src/main/res/drawable/`
   - Rename it to: `ic_launcher.png`
   - Or use Android Studio's Image Asset tool:
     - Right-click `res` → New → Image Asset
     - Follow wizard to create launcher icons

5. **Run the App**
   ```
   - Click green "Run" button (▶️) at top
   - Select an emulator or connected device
   - Wait for app to install and launch
   ```

### Option 2: Build APK from Command Line

1. **Prerequisites**
   ```bash
   - Install JDK 11 or higher
   - Install Android SDK
   - Set ANDROID_HOME environment variable
   ```

2. **Build APK**
   ```bash
   cd EmergencyAlertApp
   ./gradlew assembleDebug  # Linux/Mac
   gradlew.bat assembleDebug  # Windows
   ```

3. **Find APK**
   ```
   Location: app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Install on Device**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Creating Android Emulator

1. **In Android Studio**
   ```
   Tools → Device Manager → Create Device
   ```

2. **Select Hardware**
   ```
   - Choose: Pixel 5 (recommended)
   - Click "Next"
   ```

3. **Select System Image**
   ```
   - API Level: 34 (Android 14) or higher
   - Click "Download" if needed
   - Click "Next"
   ```

4. **Verify Configuration**
   ```
   - Name: Pixel_5_API_34
   - Click "Finish"
   ```

5. **Launch Emulator**
   ```
   - Click ▶️ icon next to emulator name
   - Wait for emulator to boot
   - Run app from Android Studio
   ```

## Testing on Physical Device

1. **Enable Developer Mode**
   ```
   Settings → About Phone → Tap "Build Number" 7 times
   ```

2. **Enable USB Debugging**
   ```
   Settings → Developer Options → USB Debugging → ON
   ```

3. **Connect Device**
   ```
   - Connect via USB cable
   - Allow USB debugging when prompted
   - Device should appear in Android Studio's device list
   ```

4. **Run App**
   ```
   - Select your device from dropdown
   - Click Run button
   ```

## Required Permissions Setup

When you first run the app, you'll need to grant permissions:

1. **On First Launch**
   - Allow Location (Required)
   - Allow Phone (Required for emergency calls)
   - Allow Camera (Required for image capture)
   - Allow Storage (Required for saving images)
   - Allow Notifications (Required for alerts)

2. **Background Location**
   - Go to: Settings → Apps → Emergency Alert → Permissions
   - Location → Allow all the time
   - This enables background tracking during emergency

## Troubleshooting

### Gradle Sync Failed
```
Solution:
- File → Invalidate Caches → Invalidate and Restart
- Or: Delete .gradle folder and sync again
```

### App Crashes on Launch
```
Check:
1. Minimum SDK is 24 (Android 7.0) or higher
2. All permissions granted in device settings
3. Check Logcat in Android Studio for error messages
```

### Cannot Find R.id.xxx Error
```
Solution:
- Build → Clean Project
- Build → Rebuild Project
```

### Location Not Working
```
Check:
1. Device GPS is ON
2. App has location permission
3. Using emulator? Set location in emulator controls
```

### Camera Not Working
```
Check:
1. Camera permission granted
2. Device has camera hardware
3. Using emulator? Use emulator's virtual camera
```

### Images Not Saving
```
Check:
1. Storage permission granted
2. Check: /storage/emulated/0/Android/data/com.emergency.alert/files/Pictures/
```

## File Structure Explanation

```
EmergencyAlertApp/
├── app/
│   ├── build.gradle          # App dependencies and SDK versions
│   └── src/main/
│       ├── AndroidManifest.xml    # App permissions and components
│       ├── java/               # All Java source code
│       └── res/                # Resources (layouts, images, strings)
├── build.gradle              # Project-level build config
├── settings.gradle           # Module includes
└── gradle.properties         # Gradle settings
```

## Testing Features

### Test Emergency Mode
1. Register/Login
2. Tap "ACTIVATE EMERGENCY MODE"
3. Verify: Red background, large text, siren sound

### Test SOS
1. Add at least one emergency contact
2. Tap "SOS" button
3. Check SMS app for outgoing messages

### Test Location Tracking
1. Tap "Start Location Tracking"
2. Check notification bar for location updates
3. Location format: Lat: xx.xxxxxx, Lon: yy.yyyyyy

### Test Image Editor
1. Tap "Emergency Image Capture & Edit"
2. Capture photo or select from gallery
3. Try all editing tools: Crop, Resize, Brighten, Blur
4. Save image and check file location

### Test Calculators
1. Open "Emergency Calculators"
2. Test each calculator type:
   - Distance to Hospital
   - Travel Time
   - Medical Dosage
   - Battery Life
3. Verify calculations make sense

### Test WebView
1. Open "Emergency Information Online"
2. Tap each button to load websites
3. Verify web pages load correctly
4. Test back button navigation

## Performance Tips

1. **Reduce Battery Drain**
   - Only enable Emergency Mode when needed
   - Stop location tracking when not in emergency

2. **Optimize Storage**
   - Delete old emergency images
   - Clear emergency event logs periodically

3. **Network Usage**
   - WebView requires internet connection
   - Images can be captured offline
   - SOS works without internet (uses SMS)

## Development Tips

1. **Adding New Safety Tips**
   - Edit: DatabaseHelper.java
   - Find: insertDefaultSafetyTips() method
   - Add new insertSafetyTip() calls

2. **Changing Theme Colors**
   - Edit: app/src/main/res/values/colors.xml
   - Modify color values

3. **Modifying Layouts**
   - Edit XML files in: app/src/main/res/layout/
   - Use Android Studio's Layout Editor for visual editing

4. **Adding New Features**
   - Create new Activity class
   - Create corresponding XML layout
   - Register in AndroidManifest.xml
   - Add button in MainActivity to navigate

## Support Resources

- Android Developer Docs: https://developer.android.com/docs
- Stack Overflow: https://stackoverflow.com/questions/tagged/android
- Android Studio Guide: https://developer.android.com/studio/intro

## Known Limitations

1. Geofencing requires active setup (coordinates not pre-configured)
2. Medical dosage calculator is demonstration only
3. Emergency siren uses system notification sound
4. WebView requires internet connection
5. Background location requires "Allow all the time" permission

## Next Steps

After successful installation:
1. ✅ Register an account
2. ✅ Add emergency contacts
3. ✅ Customize your theme
4. ✅ Test Emergency Mode in safe environment
5. ✅ Familiarize yourself with all features
6. ✅ Keep device charged for emergency use

---

**Happy Testing! Stay Safe!**
