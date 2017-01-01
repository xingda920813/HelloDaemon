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
        //我们现在需要服务运行, 将标志位重置为 false
        WorkService.sShouldStopService = false;
        startService(new Intent(this, WorkService.class));
    }
}
