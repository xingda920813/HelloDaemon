package com.xdandroid.hellodaemon.service;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;

import com.xdandroid.hellodaemon.receiver.*;

public class WatchDogService extends Service {

    private static final int sHashCode = WatchDogService.class.getName().hashCode();

    private static boolean sAlive;

    /**
     * 守护服务，运行在:watch子进程中
     */
    private int onStart(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            startForeground(sHashCode, new Notification());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                startService(new Intent(this, WatchDogNotificationService.class));
            }
        }

        if (sAlive) return START_STICKY;

        sAlive = true;

        //每 9 分钟检查一次WorkService是否在运行，如果不在运行就把它拉起来
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, WorkService.class);
        PendingIntent pi = PendingIntent.getService(this, sHashCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 9 * 60 * 1000,
                9 * 60 * 1000,
                pi);

        //简单守护开机广播
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(getPackageName(), WakeUpReceiver.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(getPackageName(), WakeUpReceiver.WakeUpAutoStartReceiver.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        return START_STICKY;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return null;
    }

    private void onEnd(Intent rootIntent) {
        startService(new Intent(this, WorkService.class));
        startService(new Intent(this, WatchDogService.class));
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
         * 运行在:watch子进程中
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
