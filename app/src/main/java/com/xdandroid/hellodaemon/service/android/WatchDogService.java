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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            startService(new Intent(this, WatchDogNotificationService.class));
        startForeground(sHashCode, new Notification());

        if (sAlive) return START_STICKY;

        sAlive = true;

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(WakeUpReceiver.ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(this, sHashCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5 * 60 * 1000, pi);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        sAlive = false;
        startService(new Intent(this, getClass()));
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

    public static class WatchDogNotificationService extends Service {

        /**
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
