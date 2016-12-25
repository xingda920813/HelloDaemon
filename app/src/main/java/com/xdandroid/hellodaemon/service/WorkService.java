package com.xdandroid.hellodaemon.service;

import android.app.Notification;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;

import java.util.concurrent.*;

import rx.*;

public class WorkService extends Service {

    static final int HASH_CODE = 1;

    public static Subscription sSubscription;

    /**
     * 1.防止重复启动，可以任意调用startService(Intent i);
     * 2.利用漏洞启动前台服务而不显示通知;
     * 3.在子线程中运行定时任务，处理了运行前检查和销毁时保存的问题;
     * 4.启动守护服务.
     * 5.简单守护开机广播.
     */
    int onStart() {
        //启动前台服务而不显示通知的漏洞已在 API Level 25 修复，大快人心！
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            //利用漏洞在 API Level 17 及以下的 Android 系统中，启动前台服务而不显示通知
            startForeground(HASH_CODE, new Notification());
            //利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                startService(new Intent(getApplication(), WorkNotificationService.class));
        }

        //启动守护服务，运行在:watch子进程中
        startService(new Intent(getApplication(), WatchDogService.class));

        //----------业务逻辑----------

        //若还没有取消订阅，说明任务仍在运行，为防止重复启动，直接返回START_STICKY
        if (sSubscription != null && !sSubscription.isUnsubscribed()) return START_STICKY;

        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sSubscription = Observable.interval(3, TimeUnit.SECONDS).subscribe(count -> {
            System.out.println("每 3 秒采集一次数据... count = " + count);
            if (count > 0 && count % 18 == 0) System.out.println("保存数据到磁盘。 saveCount = " + (count / 18 - 1));
        });

        //----------业务逻辑----------

        getPackageManager().setComponentEnabledSetting(new ComponentName(getPackageName(), WatchDogService.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        return START_STICKY;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart();
    }

    @Override
    public IBinder onBind(Intent intent) {
        onStart();
        return null;
    }

    void onEnd() {
        System.out.println("保存数据到磁盘。");
        startService(new Intent(getApplication(), WorkService.class));
        startService(new Intent(getApplication(), WatchDogService.class));
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd();
    }

    /**
     * 设置-正在运行中停止服务时回调
     */
    @Override
    public void onDestroy() {
        onEnd();
    }

    public static class WorkNotificationService extends Service {

        /**
         * 利用漏洞在 API Level 18 及以上的 Android 系统中，启动前台服务而不显示通知
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(WorkService.HASH_CODE, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
