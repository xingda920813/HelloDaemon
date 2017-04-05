package com.xdandroid.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.os.*;
import android.support.v7.app.NotificationCompat;

import com.xdandroid.hellodaemon.*;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class TraceServiceImpl extends AbsWorkService {
    private int FOREGROUND_ID = 8001;
    private WebSocketClient cc = null;
    public static String URL_SOCKET = "";
    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public final static CompositeDisposable disposables = new CompositeDisposable();

    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (disposables != null) disposables.clear();
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
        return Observable.interval(0, 3, TimeUnit.SECONDS);
    }

    private DisposableObserver<Long> getObserver() {
        return new DisposableObserver<Long>() {

            @Override
            public void onNext(Long value) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                builder.setSmallIcon(R.mipmap.ic_launcher);
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                String title = new Date().toLocaleString();
                String text = App.STATUS;
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

                if(App.isNetworkAvailable(TraceServiceImpl.this)){
                    if(cc==null){
                        if(URL_SOCKET.isEmpty()) {
                            App.STATUS = "get url";
                            getUrl();
                        }
                        else {
                            App.STATUS = "create socket";
                            cc = createSocket(URL_SOCKET);
                        }
                    }else{
                        try {
                            System.out.println("tick on");
                            cc.send("tick on");
                            App.STATUS = "tick on";
                        } catch (Exception exp) {
                            cc.close();
                            URL_SOCKET = "";
                            cc = null;
                            App.STATUS = exp.getMessage();
                            exp.printStackTrace();
                        }
                    }
                }else{
                    if(cc!=null)
                        cc.close();
                    App.STATUS = "network is error";
                    System.out.println("network is error");
                    URL_SOCKET = "";
                    cc = null;
                }
            }

            @Override
            public void onError(Throwable e) {
                App.STATUS = "onError";
            }

            @Override
            public void onComplete() {
                App.STATUS = "onComplete";
            }
        };
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        System.out.println("检查磁盘中是否有上次销毁时保存的数据");

        disposables.add(getObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()));

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
    public void getUrl(){
        AsyncTextViewLoader textViewLoader = new AsyncTextViewLoader(TraceServiceImpl.this,new Callback(){

            @Override
            public void execute(String result) {
                URL_SOCKET = result;

                App.STATUS = "create socket";
                cc = createSocket(URL_SOCKET);
            }
        });
        textViewLoader.execute();
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
            App.STATUS = "onMessage";

//            Intent intent = new Intent(App.BROADCAST_MESSAGE);
//            intent.putExtra("message",message);
//            sendBroadcast(intent);

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
                AsyncSocketMessageLoader socketMessageLoader = new AsyncSocketMessageLoader(null);
                socketMessageLoader.execute(names[0],"1");
            } catch (Exception exp) {
                System.out.println(exp.toString());
            }
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            System.out.println("onOpen");
            App.STATUS = "onOpen";
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("onClose");
            App.STATUS = "onClose";
        }

        @Override
        public void onError(Exception ex) {
            System.out.println("onError");
            App.STATUS = "onError";
        }
    }
}

