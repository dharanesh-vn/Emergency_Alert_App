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
            Log.e(TAG, "Invalid geofence event");
            return;
        }

        // Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Notification permission missing");
            return;
        }

        int transition = event.getGeofenceTransition();
        List<Geofence> geofenceList = event.getTriggeringGeofences();

        if (geofenceList == null || geofenceList.isEmpty()) return;

        String geofenceId = geofenceList.get(0).getRequestId();
        DatabaseHelper db = new DatabaseHelper(context);

        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            handleEnter(context, geofenceId, db);

        } else if (transition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            NotificationHelper.showEmergencyNotification(
                    context,
                    "⚠️ Prolonged Stay Alert",
                    "You are staying too long in a risky area."
            );

            db.addEmergencyEvent(
                    "GEOFENCE_DWELL",
                    geofenceId,
                    "User stayed too long"
            );

        } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            NotificationHelper.showEmergencyNotification(
                    context,
                    "✅ Safe Zone",
                    "You exited the monitored area."
            );

            db.addEmergencyEvent(
                    "GEOFENCE_EXIT",
                    geofenceId,
                    "User exited zone"
            );
        }
    }

    private void handleEnter(Context context, String geofenceId, DatabaseHelper db) {

        String title = "⚠️ Area Alert";
        String message = "You entered a monitored zone.";

        if (geofenceId.contains("UNSAFE")) {
            title = "⚠️ Unsafe Area";
            message = "High risk area. Stay alert.";

        } else if (geofenceId.contains("ACCIDENT")) {
            title = "⚠️ Accident Zone";
            message = "Accident-prone area. Drive carefully.";

        } else if (geofenceId.contains("DISASTER")) {
            title = "⚠️ Disaster Zone";
            message = "Disaster-affected area. Follow safety rules.";
        }

        NotificationHelper.showEmergencyNotification(context, title, message);

        db.addEmergencyEvent(
                "GEOFENCE_ENTER",
                geofenceId,
                "User entered zone"
        );
    }
}
