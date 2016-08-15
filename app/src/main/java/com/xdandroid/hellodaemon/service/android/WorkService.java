package com.xdandroid.hellodaemon.service.android;

import android.app.*;
import android.app.Notification;
import android.content.*;
import android.content.pm.*;
import android.os.*;

import com.xdandroid.hellodaemon.receiver.*;

import java.util.concurrent.*;

import rx.*;

public class WorkService extends Service {

    /**
     * 用作 Notification Id 和 RequestCode
     */
    static final int sHashCode = WorkService.class.getName().hashCode();
    /**
     * 对任务的订阅
     */
    private static Subscription sSubscription;

    /**
     * 1.防止重复启动，可以任意调用startService(Intent i);
     * 2.利用漏洞启动前台服务而不显示通知;
     * 3.在子线程中运行定时任务，处理了运行前检查和销毁时保存的问题;
     * 4.设置闹钟 : 每5分钟检查一次;
     * 5.简单守护开机广播.
     * 运行在 :work 子进程中
     * @return START_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //若还没有取消订阅，说明任务仍在运行，为防止重复启动，直接返回START_STICKY
        if (sSubscription != null && !sSubscription.isUnsubscribed()) return START_STICKY;

        //利用漏洞在 API Level 17 及以下的 Android 系统中，启动前台服务而不显示通知
        startForeground(sHashCode, new Notification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            //利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
            startService(new Intent(this, WorkNotificationService.class));

        //----------业务逻辑----------
        //开始任务前，先检查磁盘中是否有上次销毁时保存的数据
        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sSubscription = Observable
                //每5秒钟执行一次Subscriber的onNext方法
                //interval操作符默认在Schedulers.computation()调度器上运行，因此不会阻塞主线程
                .interval(5, TimeUnit.SECONDS)
                //在onDestroy中取消订阅时，先把数据保存到磁盘，并进行收尾、清理工作
                .doOnUnsubscribe(() -> System.out.println("保存数据到磁盘。"))
                .subscribe(aLong -> {
                    //void onNext(Long aLong)
                    System.out.println("每5秒采集一次数据...");
                });
        //----------业务逻辑----------

        //检查Alarm是否已激活
        if (!WatchDogService.isAlarmAlreadySet(this, sHashCode)) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent i = new Intent(this, WakeUpReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this, sHashCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
            //设置闹钟 : 每 15 分钟检查一次服务是否在运行，如果不在运行就拉起来
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
        }

        //简单守护开机广播
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(getPackageName(), WakeUpReceiver.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        return START_STICKY;
    }

    /**
     * 1.处理了销毁时保存的问题;
     * 2.销毁后重新拉起服务.
     */
    @Override
    public void onDestroy() {
        //在onDestroy中取消订阅时，会执行Observable的doOnUnsubscribe(Runnable r)方法，我们在取消订阅时把数据保存到磁盘
        if (sSubscription != null) sSubscription.unsubscribe();
        //重新拉起服务
        startService(new Intent(this, getClass()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
}
