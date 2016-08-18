package com.xdandroid.hellodaemon.service.android;

import android.app.*;
import android.content.*;
import android.os.*;

import com.xdandroid.hellodaemon.receiver.*;

public class WatchDogService extends Service {

    static final int sHashCode = WatchDogService.class.getName().hashCode();

    private static boolean sAlive;

    /**
     * 运行在 :watch 子进程中
     */
    public int onStart(Intent intent, int flags, int startId) {
        Context app = getApplicationContext();
        startForeground(sHashCode, new Notification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            app.startService(new Intent(app, WatchDogNotificationService.class));

        if (sAlive) return START_STICKY;

        sAlive = true;

        AlarmManager am = (AlarmManager) app.getSystemService(ALARM_SERVICE);
        Intent i = new Intent(WakeUpReceiver.ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(app, sHashCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5 * 60 * 1000, pi);

        return START_STICKY;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    /**
     * Note : 多进程情况下与Service通信需使用AIDL
     */
    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return null;
    }

    public void onEnd(Intent rootIntent) {
        sAlive = false;
        //重新拉起服务
        Context app = getApplicationContext();
        app.startService(new Intent(app, WorkService.class));
        app.startService(new Intent(app, WatchDogService.class));
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    /**
     * 设置-正在运行中停止服务时回调
     */
    @Override
    public void onDestroy() {
        onEnd(null);
    }

    public static class WatchDogNotificationService extends Service {

        /**
         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
         * 运行在 :watch 子进程中
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(WatchDogService.sHashCode, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
