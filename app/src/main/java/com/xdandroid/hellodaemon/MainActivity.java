package com.xdandroid.hellodaemon;

import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.support.v7.app.*;

import com.xdandroid.hellodaemon.receiver.*;

import rx.*;
import rx.schedulers.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //为防止优化软件禁用BroadcastReceiver组件，可以每次启动App都守护一下
        Observable.just(new Object())
                  //防止阻塞主线程
                .subscribeOn(Schedulers.computation())
                .subscribe(o -> {
                    getPackageManager().setComponentEnabledSetting(
                            new ComponentName(getPackageName(), WakeUpReceiver.class.getName()),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                }, Throwable::printStackTrace);
        findViewById(R.id.btn_start).setOnClickListener(v -> sendBroadcast(new Intent(WakeUpReceiver.ACTION)));
    }

    /**
     * 防止华为机型按返回键回到桌面再锁屏后几秒钟进程被杀
     */
    @Override
    public void onBackPressed() {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(launcherIntent);
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent(WakeUpReceiver.ACTION));
    }
}
