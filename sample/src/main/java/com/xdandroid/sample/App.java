package com.xdandroid.sample;

import android.app.*;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xdandroid.hellodaemon.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class App extends Application {
    public static String BROADCAST_MESSAGE="com.xdandroid.sample.message";
    public static String STATUS="";
    @Override
    public void onCreate() {
        super.onCreate();
        DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        startService(new Intent(this, TraceServiceImpl.class));
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    public static String httpsConnection(String requestUrl, String method, String outputStr, Map<String, String> headers)
            throws Exception {
        try {
            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();

            if (null != headers) {
                for (String key : headers.keySet()) {
                    httpUrlConn.setRequestProperty(key, headers.get(key));
                }
            }

            if (!method.equals("GET")) {
                httpUrlConn.setDoOutput(true);
                httpUrlConn.setDoInput(true);
            }

            httpUrlConn.setRequestMethod(method);
            httpUrlConn.connect();

            if (outputStr != null) {
                DataOutputStream out = new DataOutputStream(httpUrlConn.getOutputStream());
                out.writeBytes(outputStr);
                out.flush();
                out.close();
            }

            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpUrlConn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();

            httpUrlConn.disconnect();
            return buffer.toString();
        } catch (Exception ex) {
            throw ex;
        }
    }
}

