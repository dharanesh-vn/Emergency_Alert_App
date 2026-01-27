package com.emergency.alert;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event == null || event.hasError()) {
            Log.e(TAG, "Geofence error");
            return;
        }

        // Android 13+ notification permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }

        int transition = event.getGeofenceTransition();
        List<Geofence> geofences = event.getTriggeringGeofences();

        if (geofences == null || geofences.isEmpty()) {
            return;
        }

        String geofenceId = geofences.get(0).getRequestId();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        switch (transition) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                handleEnter(context, geofenceId, dbHelper);
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                NotificationHelper.showEmergencyNotification(
                        context,
                        "⚠️ Prolonged Stay Warning",
                        "You have stayed in a risky area for too long."
                );

                dbHelper.addEmergencyEvent(
                        "Geofence DWELL",
                        geofenceId,
                        "User stayed too long in monitored area"
                );
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                NotificationHelper.showEmergencyNotification(
                        context,
                        "✅ Safe Zone",
                        "You have exited the monitored area."
                );

                dbHelper.addEmergencyEvent(
                        "Geofence EXIT",
                        geofenceId,
                        "User exited monitored area"
                );
                break;
        }
    }

    private void handleEnter(Context context, String geofenceId, DatabaseHelper dbHelper) {

        String title;
        String message;

        if (geofenceId.contains("UNSAFE")) {
            title = "⚠️ Unsafe Area Alert";
            message = "You entered an unsafe zone. Stay alert.";

        } else if (geofenceId.contains("ACCIDENT")) {
            title = "⚠️ Accident-Prone Area";
            message = "High accident risk area. Proceed carefully.";

        } else if (geofenceId.contains("DISASTER")) {
            title = "⚠️ Disaster Zone Alert";
            message = "Disaster-affected area. Follow safety instructions.";

        } else {
            title = "⚠️ Area Alert";
            message = "You entered a monitored zone.";
        }

        NotificationHelper.showEmergencyNotification(context, title, message);

        dbHelper.addEmergencyEvent(
                "Geofence ENTER",
                geofenceId,
                "User entered monitored zone"
        );
    }
}
