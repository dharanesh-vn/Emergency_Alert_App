package com.emergency.alert;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MediaService extends Service {

    private MediaPlayer sirenPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "PLAY_SIREN":
                    playSiren();
                    break;
                case "STOP_SIREN":
                    stopSiren();
                    stopSelf();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playSiren() {
        if (sirenPlayer == null) {
            sirenPlayer = MediaPlayer.create(this, R.raw.siren);
            if (sirenPlayer != null) {
                sirenPlayer.setLooping(true);
                sirenPlayer.start();
            }
        } else if (!sirenPlayer.isPlaying()) {
            sirenPlayer.start();
        }
    }

    private void stopSiren() {
        if (sirenPlayer != null) {
            try {
                sirenPlayer.stop();
            } catch (Exception ignored) {}
            sirenPlayer.release();
            sirenPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        stopSiren();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
