package com.xdandroid.hellodaemon.service;

import android.app.*;
import android.app.Notification;
import android.app.job.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;

import com.xdandroid.hellodaemon.receiver.*;

import java.util.concurrent.*;

import rx.*;
import rx.android.schedulers.*;
import rx.schedulers.*;

public class WatchDogService extends Service {

    private static final int sHashCode = 2;
    private static final int INTERVAL_WAKE_UP = 6 * 60 * 1000;

    private static Subscription sSubscription;

    public static PowerManager.WakeLock sWakeLock;

    /**
     * 守护服务，运行在:watch子进程中
     */
    private int onStart(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            startForeground(sHashCode, new Notification());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                startService(new Intent(getApplication(), WatchDogNotificationService.class));
        }

        if (sSubscription != null && !sSubscription.isUnsubscribed()) return START_STICKY;

        //定时检查 WorkService 是否在运行，如果不在运行就把它拉起来
        //Android 5.0+ 使用 JobScheduler，效果比 AlarmManager 好
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = new JobInfo.Builder(sHashCode, new ComponentName(getApplication(), JobSchedulerService.class));
            builder.setPeriodic(INTERVAL_WAKE_UP);
            //Android 7.0+ 增加了一项针对 JobScheduler 的新限制，最小间隔只能是下面设定的数字
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis());
            builder.setPersisted(true);
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.schedule(builder.build());
        } else {
            //Android 4.4- 使用 AlarmManager
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent i = new Intent(getApplication(), WorkService.class);
            PendingIntent pi = PendingIntent.getService(getApplication(), sHashCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL_WAKE_UP, INTERVAL_WAKE_UP, pi);
        }

        //使用定时 Observable，避免 Android 定制系统 JobScheduler / AlarmManager 唤醒间隔不稳定的情况
        sSubscription = Observable
                .interval(INTERVAL_WAKE_UP, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> startService(new Intent(getApplication(), WorkService.class)), Throwable::printStackTrace);

        //若需要防止 CPU 休眠，这里给出了 WakeLock 的参考实现
        /*PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WatchDogService.class.getSimpleName());
        sWakeLock.setReferenceCounted(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        try {
            getApplication().registerReceiver(WakeLockReceiver.getInstance(), intentFilter);
        } catch (Exception ignored) {}*/

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
        startService(new Intent(getApplication(), WorkService.class));
        startService(new Intent(getApplication(), WatchDogService.class));
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
