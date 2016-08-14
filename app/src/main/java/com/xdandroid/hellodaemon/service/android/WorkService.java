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

    static final int sHashCode = WorkService.class.getName().hashCode();

    private static Subscription sSubscription;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (sSubscription != null && !sSubscription.isUnsubscribed()) return START_STICKY;

        startForeground(sHashCode, new Notification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            startService(new Intent(this, DismissNotificationService.class));

        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sSubscription = Observable
                .interval(5, TimeUnit.SECONDS)
                .doOnUnsubscribe(() -> System.out.println("保存数据到磁盘。"))
                .subscribe(aLong -> {
                    System.out.println("每5秒采集1次数据...");
                });

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, WakeUpReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, sHashCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 60 * 1000, 5 * 60 * 1000, pi);

        getPackageManager().setComponentEnabledSetting(
                new ComponentName(getPackageName(), WakeUpReceiver.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (sSubscription != null) sSubscription.unsubscribe();
        startService(new Intent(this, getClass()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
