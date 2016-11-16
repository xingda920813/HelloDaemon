package com.xdandroid.hellodaemon.receiver;

import android.content.*;

import com.xdandroid.hellodaemon.service.*;

/**
 * Created by xingda on 16-11-16.
 */

public class WakeLockReceiver extends BroadcastReceiver {

    private static WakeLockReceiver sInstance;

    public static WakeLockReceiver getInstance() {
        if (sInstance == null) sInstance = new WakeLockReceiver();
        return sInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null || WatchDogService.sWakeLock == null)
            return;
        switch (intent.getAction()) {
            case Intent.ACTION_SCREEN_OFF:
                //关闭屏幕时获得 WakeLock
                try {
                    if (!WatchDogService.sWakeLock.isHeld()) WatchDogService.sWakeLock.acquire();
                } catch (Exception ignored) {}
                break;
            case Intent.ACTION_SCREEN_ON:
                //点亮屏幕后释放 WakeLock
                try {
                    if (WatchDogService.sWakeLock.isHeld()) WatchDogService.sWakeLock.release();
                } catch (Exception ignored) {}
                break;
        }
    }
}
