# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep database models
-keep class com.emergency.alert.DatabaseHelper$** { *; }

# Keep service classes
-keep class com.emergency.alert.*Service { *; }

# Keep notification helper
-keep class com.emergency.alert.NotificationHelper { *; }
