package com.xdandroid.hellodaemon.service.android;

import android.app.*;
import android.content.*;
import android.os.*;

public class WatchDogNotificationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(WatchDogService.sHashCode, new Notification());
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
