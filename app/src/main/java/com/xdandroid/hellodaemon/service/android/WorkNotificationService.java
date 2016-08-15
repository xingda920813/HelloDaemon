package com.xdandroid.hellodaemon.service.android;

import android.app.*;
import android.content.*;
import android.os.*;

public class WorkNotificationService extends Service {

    /**
     * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
     * 运行在 :work 子进程中
     * @return START_NOT_STICKY
     */
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
