package com.xdandroid.sample;

import android.app.*;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xdandroid.hellodaemon.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        startService(new Intent(this, TraceServiceImpl.class));
    }

}

