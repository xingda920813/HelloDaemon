package com.xdandroid.sample;

import android.app.*;
import android.content.*;

import com.xdandroid.hellodaemon.*;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        startService(new Intent(this, TraceServiceImpl.class));
    }
}
