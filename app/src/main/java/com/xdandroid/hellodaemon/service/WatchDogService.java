package com.xdandroid.hellodaemon.service;

import android.app.*;
import android.app.Notification;
import android.app.job.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;

import com.xdandroid.hellodaemon.app.*;

import java.util.concurrent.*;

import rx.*;

public class WatchDogService extends Service {

    static final int HASH_CODE = 2;
    static final int INTERVAL_WAKE_UP = 6 * 60 * 1000;

    static Subscription sSubscription;
    static PendingIntent sPendingIntent;

    /**
     * 守护服务，运行在:watch子进程中
     */
    int onStart(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            startForeground(HASH_CODE, new Notification());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) startService(new Intent(App.sApp, WatchDogNotificationService.class));
        }

        if (sSubscription != null && !sSubscription.isUnsubscribed()) return START_STICKY;

        //定时检查 WorkService 是否在运行，如果不在运行就把它拉起来
        //Android 5.0+ 使用 JobScheduler，效果比 AlarmManager 好
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = new JobInfo.Builder(HASH_CODE, new ComponentName(App.sApp, JobSchedulerService.class));
            builder.setPeriodic(INTERVAL_WAKE_UP);
            //Android 7.0+ 增加了一项针对 JobScheduler 的新限制，最小间隔只能是下面设定的数字
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis());
            builder.setPersisted(true);
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.schedule(builder.build());
        } else {
            //Android 4.4- 使用 AlarmManager
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent i = new Intent(App.sApp, WorkService.class);
            sPendingIntent = PendingIntent.getService(App.sApp, HASH_CODE, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL_WAKE_UP, INTERVAL_WAKE_UP, sPendingIntent);
        }

        //使用定时 Observable，避免 Android 定制系统 JobScheduler / AlarmManager 唤醒间隔不稳定的情况
        sSubscription = Observable.interval(INTERVAL_WAKE_UP, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> startService(new Intent(App.sApp, WorkService.class)), Throwable::printStackTrace);

        //守护 Service 组件的启用状态, 使其不被 MAT 等工具禁用
        getPackageManager().setComponentEnabledSetting(new ComponentName(getPackageName(), WorkService.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

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

    void onEnd(Intent rootIntent) {
        startService(new Intent(App.sApp, WorkService.class));
        startService(new Intent(App.sApp, WatchDogService.class));
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

    /**
     * 用于在不需要服务运行的时候取消 Job / Alarm / Subscription.
     *
     * 因 WatchDogService 运行在 :watch 子进程, 请勿在主进程中直接调用此方法.
     * 而是向 WakeUpReceiver 发送一个 Action 为 WakeUpReceiver.ACTION_CANCEL_JOB_ALARM_SUB 的广播.
     */
    public static void cancelJobAlarmSub() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler scheduler = (JobScheduler) App.sApp.getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancel(HASH_CODE);
        } else {
            AlarmManager am = (AlarmManager) App.sApp.getSystemService(ALARM_SERVICE);
            if (sPendingIntent != null) am.cancel(sPendingIntent);
        }
        if (sSubscription != null) sSubscription.unsubscribe();
    }

    public static class WatchDogNotificationService extends Service {

        /**
         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
         * 运行在:watch子进程中
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(WatchDogService.HASH_CODE, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
