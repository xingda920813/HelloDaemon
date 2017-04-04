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

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

import rx.*;
import rx.functions.*;

public class TraceServiceImpl extends AbsWorkService {
    private WebSocketClient cc = null;
    public static String URL_SOCKET = "";
    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public static Subscription sSubscription;

    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sSubscription != null) sSubscription.unsubscribe();
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

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sSubscription = Observable
                .interval(3, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnUnsubscribe(new Action0() {
                    public void call() {
                        System.out.println("保存数据到磁盘。");
                        cancelJobAlarmSub();
                    }
                }).subscribe(new Action1<Long>() {
                    public void call(Long count) {
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

                        System.out.println("每 10 秒采集一次数据... count = " + count);
                        if (count > 0 && count % 18 == 0) System.out.println("保存数据到磁盘。 saveCount = " + (count / 18 - 1));

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
                                    if(count%6==0) {
                                        System.out.println("tick on");
                                        cc.send("tick on");
                                        App.STATUS = "tick on";
                                    }
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

            Intent intent = new Intent(App.BROADCAST_MESSAGE);
            intent.putExtra("message",message);
            sendBroadcast(intent);
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

