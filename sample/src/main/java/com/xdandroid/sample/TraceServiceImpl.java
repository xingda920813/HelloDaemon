package com.xdandroid.sample;

import android.app.*;
import android.app.Notification;
import android.content.*;
import android.os.*;
import android.support.v7.app.NotificationCompat;

import com.xdandroid.hellodaemon.*;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.*;

import rx.*;
import rx.functions.*;

public class TraceServiceImpl extends AbsWorkService {
    private WebSocketClient cc;
    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public static Subscription sSubscription;
    private int FOREGROUND_ID = 8000;
    //private boolean isQuery = false;
    //private boolean isOpen = false;

    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sSubscription != null) sSubscription.unsubscribe();
        //取消 Job / Alarm / Subscription
        cancelJobAlarmSub();

        App.STATUS = "stopService";
    }

    /**
     * 是否 任务完成, 不再需要服务运行?
     * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return sShouldStopService;
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sSubscription = Observable
                .interval(10, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnUnsubscribe(new Action0() {
                    public void call() {
                        System.out.println("保存数据到磁盘。");
                        cancelJobAlarmSub();
                    }
                }).subscribe(new Action1<Long>() {
                    public void call(Long count) {
                        System.out.println("每 10 秒采集一次数据... count = " + count);
                        if (count > 0 && count % 18 == 0) System.out.println("保存数据到磁盘。 saveCount = " + (count / 18 - 1));

                        if(App.isNetworkAvailable(TraceServiceImpl.this)){
                            if(cc==null){
                                if(App.URL_SOCKET.isEmpty())
                                    getUrl();
                                else
                                    cc = createSocket(App.URL_SOCKET);
                            }else{
                                try {
                                    if(cc.getReadyState() == WebSocket.READYSTATE.OPEN) {
                                        if(count%6==0){
                                            System.out.println("tick on");
                                            cc.send("tick on");
                                            App.STATUS = "tick on";
                                        }
                                    }else{
                                        System.out.println("create socket again");
                                        App.URL_SOCKET = "";
                                        cc = null;
                                        App.STATUS = "create socket again";
                                    }
                                } catch (Exception exp) {
                                    App.URL_SOCKET = "";
                                    cc = null;
                                    exp.printStackTrace();
                                }
                            }
                        }else{
                            App.STATUS = "network is error";
                            System.out.println("network is error");
                            App.URL_SOCKET = "";
                            cc = null;
                        }

                    }
                });
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
        return sSubscription != null && !sSubscription.isUnsubscribed();
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
        AsyncTextViewLoader textViewLoader = new AsyncTextViewLoader(TraceServiceImpl.this,null);
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
            String title = "title";
            String content = message;
            try {
                JSONObject json = new JSONObject(message);
                title = json.getString("title");
                content = json.getString("message");
            } catch (Exception exp) {
                System.out.println(exp.toString());
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(TraceServiceImpl.this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(title);
            builder.setContentText(content);
            builder.setContentInfo("message");
            builder.setWhen(System.currentTimeMillis());

            builder.setPriority(NotificationCompat.PRIORITY_MAX);
            builder.setAutoCancel(true);

            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            Intent activityIntent = new Intent(TraceServiceImpl.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(TraceServiceImpl.this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            android.app.Notification notification = builder.build();

            NotificationManager mNotificationManager = (NotificationManager) TraceServiceImpl.this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(++FOREGROUND_ID, notification);
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
