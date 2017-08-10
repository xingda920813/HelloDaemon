package com.xdandroid.hellodaemon;

import android.annotation.*;
import android.app.job.*;
import android.os.*;

/**
 * Android 5.0+ 使用的 JobScheduler.
 * 运行在 :watch 子进程中.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        if (!DaemonEnv.sInitialized) return false;
        DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
