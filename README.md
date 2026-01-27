# Emergency Alert & Safety Android App

A comprehensive Android application designed to help users during emergency situations by providing quick alerts, location sharing, safety information, emergency services access, and privacy-aware evidence capture.

## Features

### 🔐 User Profile & Customization
- User login and profile setup with username and password
- Local profile storage using SQLite
- Theme customization options:
  - Background color selection (Hex color codes)
  - Font size adjustment for accessibility

### 🚨 Emergency Mode
- One-tap Emergency Mode activation button
- UI automatically changes:
  - Screen turns RED
  - Font size increases
  - Emergency status displayed prominently
- Emergency Mode overrides normal settings for visibility and urgency
- Automatic emergency siren activation
- Automatic location tracking

### ⚡ Quick Emergency Actions
- **SOS Button**: Sends emergency SMS to all contacts
- **Call Emergency Contact**: Quick dial to emergency contacts or 911
- **View Safety Tips**: Instant access to life-saving information

### 📍 Location & Safety Services
- Real-time GPS location tracking
- Share live location with emergency contacts
- Background location service for continuous tracking during emergency
- Location displayed in notifications

### 🔔 Alerts & Notifications
- Push notifications for:
  - Natural disasters (flood, cyclone, earthquake)
  - Accident alerts
  - Panic confirmation messages
  - Safety reminders
- Notifications work even when app is in background

### 🔊 Audio & Media Support
- Emergency siren sound playback
- Voice-based emergency instructions
- Safety audio guides included for:
  - CPR steps
  - Self-defense tips
  - Medical emergencies
- Media playback continues in background

### 📍 Geofencing & Risk Detection
- Detect when user enters:
  - Accident-prone zones
  - Isolated or unsafe areas
  - Disaster-affected locations
- Automatic alerts when:
  - User stays too long in unsafe areas
  - User enters monitored zones
- Automatic notification to emergency contacts

### 🗄️ Local Data Management (SQLite)
- User profile data
- Emergency contact list with full CRUD operations
- Emergency event logs (date, time, location, notes)
- Safety tips database with pre-loaded content
- All data stored locally and securely

### 🧮 Emergency Utility Calculators
- **Distance Calculator**: Calculate distance to nearest hospital
- **Travel Time Calculator**: Estimate time to reach destination
- **Medical Dosage Calculator**: Demo calculation based on age & weight (educational only)
- **Battery Life Calculator**: Estimate remaining battery time

### 🌐 Online Emergency Information
- WebView integration to display:
  - Live disaster alerts (weather.gov)
  - Government emergency guidelines (ready.gov)
  - Police and emergency services (911.gov)
  - Red Cross emergency preparedness information
- Real-time emergency news and instructions

### 🖼️ Emergency Image Processing
- Capture images during emergencies using camera
- Image editing features:
  - **Crop**: Focus on injury or damage
  - **Resize**: Fast sharing on low networks (50% reduction)
  - **Brightness**: Improve clarity in low light
  - **Blur**: Mask or blur faces for privacy protection
- Save edited images to device
- All processing done locally

## Technical Stack

- **Language**: Java
- **IDE**: Android Studio
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Database**: SQLite
- **Location Services**: Google Play Services Location API
- **UI**: XML Layouts with Material Design components
- **Architecture**: Activity-based with Services

## Project Structure

```
EmergencyAlertApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/emergency/alert/
│   │   │   ├── MainActivity.java              # Main dashboard
│   │   │   ├── LoginActivity.java             # Login/Register
│   │   │   ├── ProfileActivity.java           # Theme customization
│   │   │   ├── EmergencyContactsActivity.java # Manage contacts
│   │   │   ├── SafetyTipsActivity.java        # Safety guidelines
│   │   │   ├── CalculatorActivity.java        # Emergency calculators
│   │   │   ├── WebViewActivity.java           # Emergency info web
│   │   │   ├── ImageEditorActivity.java       # Image capture & edit
│   │   │   ├── DatabaseHelper.java            # SQLite operations
│   │   │   ├── LocationTrackingService.java   # Background GPS
│   │   │   ├── MediaService.java              # Emergency sounds
│   │   │   ├── GeofenceBroadcastReceiver.java # Geofencing
│   │   │   └── NotificationHelper.java        # Push notifications
│   │   ├── res/
│   │   │   ├── layout/                        # All XML layouts
│   │   │   ├── values/                        # Strings, colors, styles
│   │   │   └── xml/                           # File paths config
│   │   └── AndroidManifest.xml                # App configuration
│   └── build.gradle                           # App dependencies
├── build.gradle                               # Project build config
├── settings.gradle                            # Project settings
└── gradle.properties                          # Gradle properties
```

## Database Schema

### Users Table
- id (PRIMARY KEY)
- username (UNIQUE)
- password
- bg_color (default: #FFFFFF)
- font_size (default: 16)

### Emergency Contacts Table
- id (PRIMARY KEY)
- name
- phone
- relation

### Emergency Events Table
- id (PRIMARY KEY)
- event_type
- event_date
- location
- notes

### Safety Tips Table
- id (PRIMARY KEY)
- category
- title
- content

## Required Permissions

The app requires the following permissions:
- `INTERNET` - Load emergency information from web
- `ACCESS_FINE_LOCATION` - GPS location tracking
- `ACCESS_COARSE_LOCATION` - Network-based location
- `ACCESS_BACKGROUND_LOCATION` - Background tracking during emergency
- `CALL_PHONE` - Quick dial emergency contacts
- `SEND_SMS` - Send SOS messages
- `CAMERA` - Capture emergency evidence
- `READ_EXTERNAL_STORAGE` - Access gallery images
- `WRITE_EXTERNAL_STORAGE` - Save edited images
- `VIBRATE` - Alert notifications
- `WAKE_LOCK` - Keep device awake during emergency
- `FOREGROUND_SERVICE` - Background location tracking
- `POST_NOTIFICATIONS` - Show emergency alerts

## Build Instructions

### Prerequisites
1. Install Android Studio (Latest version recommended)
2. Install Java JDK 8 or higher
3. Android SDK with API Level 34

### Steps to Build

1. **Open Project in Android Studio**
   ```
   File → Open → Select EmergencyAlertApp folder
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

3. **Build the Project**
   ```
   Build → Make Project
   ```

4. **Run on Emulator or Device**
   ```
   Run → Run 'app'
   ```
   - Select an emulator (API 24+) or connect a physical device
   - Enable USB Debugging on physical device

5. **Generate APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   - APK will be generated in: `app/build/outputs/apk/debug/`

## Installation on Device

1. Enable "Install from Unknown Sources" in device settings
2. Transfer APK to device
3. Open APK file and install
4. Grant all required permissions when prompted

## Usage Guide

### First Time Setup
1. Launch the app
2. Register with a username and password
3. Login with your credentials
4. Add emergency contacts (at least one recommended)

### Emergency Mode
1. Tap "ACTIVATE EMERGENCY MODE" on main screen
2. Screen turns RED with large text
3. Location tracking starts automatically
4. Emergency siren begins playing
5. Event is logged in database

### Send SOS
1. Tap "SOS" button
2. SMS sent to all emergency contacts
3. Notification shown
4. Event logged

### Customize Theme
1. Go to Profile & Theme Settings
2. Enter hex color (e.g., #FF5733)
3. Adjust font size with slider
4. Tap "Save Theme"

### Use Calculators
1. Open Emergency Calculators
2. Select calculator type (distance, travel time, dosage, battery)
3. Enter required inputs
4. Tap "Calculate" to see results

### Capture Emergency Evidence
1. Open Image Editor
2. Tap "Capture" or "Gallery"
3. Use editing tools:
   - Crop to focus on important area
   - Resize for faster sharing
   - Brighten for clarity
   - Blur for privacy protection
4. Tap "Save" to store image

## Safety Tips Included

- **Medical**: CPR Steps, Choking Aid
- **Safety**: Self Defense Tips
- **Disaster**: Earthquake Safety, Flood Safety

## Important Notes

⚠️ **Medical Disclaimer**: The medical dosage calculator is for demonstration purposes only. Always consult a medical professional for actual medical advice.

⚠️ **Emergency Services**: This app complements but does not replace official emergency services. Always call 911 or local emergency number in critical situations.

⚠️ **Location Accuracy**: GPS accuracy depends on device hardware and environmental conditions.

⚠️ **Battery Usage**: Background location tracking and emergency mode consume battery. Keep device charged during emergencies.

## Future Enhancements

- Cloud backup of emergency contacts
- Multi-language support
- Integration with wearable devices
- Voice-activated emergency mode
- Offline maps for disaster areas
- Group emergency coordination
- Medical information storage (blood type, allergies)
- Live location sharing via web link

## Academic Purpose

This application is designed as an academic project demonstrating:
- Android UI/UX design principles
- SQLite database management
- Background services and foreground services
- Location-based services and geofencing
- Multimedia processing (audio, images)
- WebView integration
- Permission handling
- Material Design implementation
- Clean code architecture

## License

This project is created for educational purposes.

## Support

For issues or questions, please refer to the code comments and documentation within each Java class.

---

**Developed with Java and Android Studio**
