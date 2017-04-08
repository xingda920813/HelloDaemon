package com.xdandroid.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.xdandroid.hellodaemon.AbsWorkService;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class TraceServiceImpl extends AbsWorkService {
    private int FOREGROUND_ID = 8001;
    private WebSocketClient cc = null;
    public static String URL_SOCKET = "";
    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public static CompositeDisposable disposables;

    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (disposables != null) disposables.dispose();
        //取消 Job / Alarm / Subscription
        cancelJobAlarmSub();
    }

    /**
     * 是否 任务完成, 不再需要服务运行?
     * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return sShouldStopService;
    }

    private Observable<? extends Long> getObservable() {
        return Observable.interval(0, 10, TimeUnit.SECONDS);
    }

    private DisposableObserver<Long> getObserver() {
        return new DisposableObserver<Long>() {
            @Override
            public void onNext(Long value) {

                DateFormat df = new SimpleDateFormat("HH:mm");
                df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                String now = df.format(new Date());
                Calendar calendar = Calendar.getInstance();
                int days = calendar.get(Calendar.DAY_OF_WEEK);
                if(days <2 || days>6)
                    return;
                if(now.compareTo("09:29") < 0  || (now.compareTo("11:31")>0 && now.compareTo("12:59")<0) || now.compareTo("15:01")>0)
                    return;

                if(!MainActivity.EXIST_FLAG){
                    Intent intent = new Intent(TraceServiceImpl.this, MainActivity.class);
                    startActivity(intent);
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                builder.setSmallIcon(R.mipmap.ic_launcher);

                String title = new Date().toLocaleString();
                String text = MainActivity.STATUS;
                String info = "Content Info";
                builder.setContentTitle(title);
                builder.setContentText(text);
                builder.setContentInfo(info);
                builder.setWhen(System.currentTimeMillis());

                builder.setPriority(NotificationCompat.PRIORITY_MAX);
                builder.setOngoing(true);

                Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);
                Notification notification = builder.build();

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(8000, notification);

                System.out.println("每 10 秒采集一次数据... count = " + value);
                if (value > 0 && value % 18 == 0) System.out.println("保存数据到磁盘。 saveCount = " + (value / 18 - 1));

                if(isNetworkAvailable(TraceServiceImpl.this)){
                    if(cc==null){
                        createSinaSocketClient();
                    }else if(cc.getReadyState() == WebSocket.READYSTATE.OPEN){
                        try {
                            System.out.println("tick on");
                            cc.send("tick on");
                            MainActivity.STATUS = "tick on";
                        } catch (Exception exp) {
                            MainActivity.STATUS = exp.getMessage();
                            exp.printStackTrace();
                        }
                    } else{
                        cc.close();
                        MainActivity.STATUS = "unknown error";
                        System.out.println("unknown error");
                        URL_SOCKET = "";
                        cc = null;
                        createSinaSocketClient();
                    }
                }else{
                    if(cc!=null)
                        cc.close();
                    MainActivity.STATUS = "network is error";
                    System.out.println("network is error");
                    URL_SOCKET = "";
                    cc = null;
                }
            }

            @Override
            public void onError(Throwable e) {
                MainActivity.STATUS = "onError";
            }

            @Override
            public void onComplete() {
                MainActivity.STATUS = "onComplete";
            }
        };
    }

    private Consumer<String> getSocketConsumer() {
        return new Consumer<String>() {
            @Override
            public void accept(String result) throws Exception {
                URL_SOCKET = result;
                MainActivity.STATUS = "create socket";
                cc = createSocket(URL_SOCKET);
            }
        };
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        disposables = new CompositeDisposable();
        disposables.add(getObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()));
    }

    public void createSinaSocketClient(){
        Flowable.fromCallable(new Callable<String>(){

            @Override
            public String call() throws Exception {
                return getSocketUrl();
            }
        }).filter(new Predicate<String>() {
            @Override
            public boolean test(String s) throws Exception {
                if(s==null || s.trim().length()==0)
                    return false;
                else
                    return true;
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread()).subscribe(getSocketConsumer());
    }

    public boolean isNetworkAvailable(Context context) {
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



    public String getSocketUrl(){
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

    @Override
    public void stopWork(Intent intent, int flags, int startId) {
        stopService();
    }

    /**
     * 任务是否正在运行?
     * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        //若还没有取消订阅, 就说明任务仍在运行.
        return disposables != null && !disposables.isDisposed();
    }

    @Override
    public IBinder onBind(Intent intent, Void v) {
        return null;
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        System.out.println("保存数据到磁盘。");
    }

    private WebSocketClient createSocket(String strUrl){

        WebSocketClient cc = null;
        try {
            cc = new MyWebSocketClient(new URI(strUrl));
            cc.connect();
        }catch(Exception exp){
            exp.printStackTrace();
            System.out.println(" is not a valid WebSocket URI\n");
        }
        return cc;
    }

    class MyWebSocketClient extends  WebSocketClient{

        public MyWebSocketClient(URI serverURI){
            super(serverURI);
        }

        @Override
        public void onMessage(String message) {
            System.out.println(message);
            MainActivity.STATUS = "onMessage";

            String title = "";
            String text = "";
            String info = "Content Info";

            try {
                JSONObject json = new JSONObject(message);
                title = json.getString("title");
                text = json.getString("message");

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setContentTitle(title);
                builder.setContentText(text);
                builder.setContentInfo(info);
                builder.setWhen(System.currentTimeMillis());

                builder.setPriority(NotificationCompat.PRIORITY_MAX);
                builder.setAutoCancel(true);

                builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

                Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);
                android.app.Notification notification = builder.build();

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(++FOREGROUND_ID, notification);

                startService(new Intent(getApplicationContext(), TraceServiceImpl.class));

                String[] names = title.split(" ");
                changeStockStatus(names[0],"1");
            } catch (Exception exp) {
                System.out.println(exp.toString());
            }
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            System.out.println("onOpen");
            MainActivity.STATUS = "onOpen";
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("onClose");
            MainActivity.STATUS = "onClose";
        }

        @Override
        public void onError(Exception ex) {
            System.out.println("onError");
            MainActivity.STATUS = "onError";
        }

        private boolean changeStockStatus(String code,String flag){
            String urlFormatter = "http://ichess.sinaapp.com/ext/update.php?code=%s&flag=%s";
            String strUrl = String.format(urlFormatter,code,flag);

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
                String txtResult = stringBuffer.toString();
                bufferedReader.close();
                httpURLConnection.disconnect();
                return true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }

            return false;
        }
    }
}

