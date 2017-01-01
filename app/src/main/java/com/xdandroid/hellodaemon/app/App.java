package com.xdandroid.hellodaemon.app;

import android.app.*;
import android.content.*;

import com.xdandroid.hellodaemon.service.*;

public class App extends Application {

    public static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        startService(new Intent(this, WorkService.class));
    }
}
