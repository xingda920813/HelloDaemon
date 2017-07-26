package com.xdandroid.sample;

import android.app.*;
import android.content.*;

import com.xdandroid.hellodaemon.*;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //需要在 Application 的 onCreate() 中调用一次 DaemonEnv.initialize()
        DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        DaemonEnv.startServiceSafely(new Intent(this, TraceServiceImpl.class));
    }
}
