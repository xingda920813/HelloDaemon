package com.xdandroid.hellodaemon.service.android;

import android.app.*;
import android.content.*;
import android.os.*;

public class DismissNotificationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(WorkService.sHashCode, new Notification());
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
