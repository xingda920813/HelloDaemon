package com.xdandroid.hellodaemon.receiver;

import android.content.*;

import com.xdandroid.hellodaemon.service.android.*;

public class WakeUpReceiver extends BroadcastReceiver {

    /**
     * 监听 3 种系统广播 : BOOT_COMPLETED, CONNECTIVITY_CHANGE, USER_PRESENT
     * 在系统启动完成、网络连接改变、用户屏幕解锁时拉起 Service
     * Service 内部做了判断，若 Service 已在运行，不会重复启动
     * 运行在 :work 子进程中
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, WorkService.class));
    }
}
