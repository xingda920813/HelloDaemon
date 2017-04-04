package com.xdandroid.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import org.json.JSONObject;

/**
 * Created by spring on 2017/4/4.
 */

public class MessageReceiver extends BroadcastReceiver {
    private int FOREGROUND_ID = 8001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        String title = "";
        String text = "";
        String info = "Content Info";

        try {
            JSONObject json = new JSONObject(message);
            title = json.getString("title");
            text = json.getString("message");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle(title);
            builder.setContentText(text);
            builder.setContentInfo(info);
            builder.setWhen(System.currentTimeMillis());

            builder.setPriority(NotificationCompat.PRIORITY_MAX);
            builder.setAutoCancel(true);

            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

            Intent activityIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            android.app.Notification notification = builder.build();

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(++FOREGROUND_ID, notification);

            context.startService(new Intent(context, TraceServiceImpl.class));

            String[] names = title.split(" ");
            AsyncSocketMessageLoader socketMessageLoader = new AsyncSocketMessageLoader(null);
            socketMessageLoader.execute(names[0],"1");
        } catch (Exception exp) {
            System.out.println(exp.toString());
        }


    }
}
