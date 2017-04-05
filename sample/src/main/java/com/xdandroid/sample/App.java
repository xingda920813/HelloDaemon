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

    public static String getSocketUrl(){
        String strUrl = "http://ichess.sinaapp.com/ext/channel.php";
        String txtResult = "";
        try {
            //创建URL对象
            URL url = new URL(strUrl);//Get请求可以在Url中带参数： ①url + "?bookname=" + name;②url="http://www.baidu.com?name=zhang&pwd=123";
            //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //在这里设置一些属性，详细见UrlConnection文档，HttpURLConnection是UrlConnection的子类
            //设置连接超时为5秒
            httpURLConnection.setConnectTimeout(5000);
            //设定请求方式(默认为get)
            httpURLConnection.setRequestMethod("GET");
            //建立到远程对象的实际连接
            httpURLConnection.connect();
            //返回打开连接读取的输入流，输入流转化为StringBuffer类型，这一套流程要记住，常用
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line = null;
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                //转化为UTF-8的编码格式
                line = new String(line.getBytes("UTF-8"));
                stringBuffer.append(line);
            }
            txtResult = stringBuffer.toString();
            bufferedReader.close();
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }

        return txtResult;
    }
}

