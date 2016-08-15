package com.xdandroid.hellodaemon.service.android;

import android.app.*;
import android.content.*;
import android.os.*;

import com.xdandroid.hellodaemon.receiver.*;

public class WatchDogService extends Service {

    static final int sHashCode = WatchDogService.class.getName().hashCode();

    private static boolean sAlive;

    /**
     * 由于在取消Alarm的同时也取消了pi，并且一个PendingIntent只能登记给一个Alarm，
     * 所以可通过检查pi是否存在，来确认Alarm是否激活。
     */
    static boolean isAlarmAlreadySet(Context context, int hashCode) {
        Intent i = new Intent(context, WakeUpReceiver.class);
        // FLAG_NO_CREATE表示如果描述的pi不存在，则返回null，而不是创建它。
        PendingIntent pi = PendingIntent.getBroadcast(context, hashCode, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    /**
     * 运行在 :watch 子进程中
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (sAlive) return START_STICKY;

        startForeground(sHashCode, new Notification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            startService(new Intent(this, WatchDogNotificationService.class));

        sAlive = true;

        if (!isAlarmAlreadySet(this, sHashCode)) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent i = new Intent(this, WakeUpReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this, sHashCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        sAlive = false;
        startService(new Intent(this, getClass()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class WatchDogNotificationService extends Service {

        /**
         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
         * 运行在 :watch 子进程中
         * @return START_NOT_STICKY
         */
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
}
