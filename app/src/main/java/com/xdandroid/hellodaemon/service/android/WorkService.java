package com.xdandroid.hellodaemon.service.android;

import android.app.*;
import android.app.Notification;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.widget.*;

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
     *
     * @return START_STICKY
     */
    public int onStart(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            //利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
            startService(new Intent(this, WorkNotificationService.class));
        //利用漏洞在 API Level 17 及以下的 Android 系统中，启动前台服务而不显示通知
        startForeground(sHashCode, new Notification());

        //若还没有取消订阅，说明任务仍在运行，为防止重复启动，直接返回START_STICKY
        if (sSubscription != null && !sSubscription.isUnsubscribed()) return START_STICKY;

        //----------业务逻辑----------
        //开始任务前，先检查磁盘中是否有上次销毁时保存的数据
        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sSubscription = Observable
                //每5秒钟执行一次Subscriber的onNext方法
                //interval操作符默认在Schedulers.computation()调度器上运行，因此不会阻塞主线程
                .interval(3, TimeUnit.SECONDS)
                //在onDestroy中取消订阅时，先把数据保存到磁盘，并进行收尾、清理工作
                .doOnUnsubscribe(() -> {
                    System.out.println("保存数据到磁盘。");
                    Toast.makeText(this, "保存数据到磁盘。", Toast.LENGTH_SHORT).show();
                })
                .subscribe(count -> {
                    //void onNext(Long count)
                    System.out.println("每 3 秒采集一次数据... count = " + count);
                });
        //----------业务逻辑----------

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(WakeUpReceiver.ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(this, sHashCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        //设置闹钟 : 每 5 分钟检查一次服务是否在运行，如果不在运行就拉起来
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5 * 60 * 1000, pi);

        //简单守护开机广播
        getPackageManager().setComponentEnabledSetting(
                new ComponentName(getPackageName(), WakeUpReceiver.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

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

    /**
     * 1.处理了销毁时保存的问题;
     * 2.销毁后重新拉起服务.
     */
    public void onEnd(Intent rootIntent) {
        //在onDestroy中取消订阅时，会执行Observable的doOnUnsubscribe(Runnable r)方法，我们在取消订阅时把数据保存到磁盘
        if (sSubscription != null) sSubscription.unsubscribe();
        //重新拉起服务
        startService(new Intent(this, getClass()));
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

    public static class WorkNotificationService extends Service {

        /**
         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
         * 运行在 :work 子进程中
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(WorkService.sHashCode, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
