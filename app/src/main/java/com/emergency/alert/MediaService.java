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
            if ("PLAY_SIREN".equals(intent.getAction())) {
                playSiren();
            } else if ("STOP_SIREN".equals(intent.getAction())) {
                stopSiren();
            }
        }
        return START_STICKY;
    }

    private void playSiren() {
        if (sirenPlayer == null) {
            sirenPlayer = MediaPlayer.create(this, R.raw.siren);
            sirenPlayer.setLooping(true);
        }
        if (!sirenPlayer.isPlaying()) sirenPlayer.start();
    }

    private void stopSiren() {
        if (sirenPlayer != null) {
            sirenPlayer.stop();
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
