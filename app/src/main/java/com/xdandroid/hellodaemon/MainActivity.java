package com.xdandroid.hellodaemon;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.app.AlertDialog;
import android.view.*;

import com.xdandroid.hellodaemon.service.*;
import com.xdandroid.hellodaemon.util.*;

public class MainActivity extends AppCompatActivity {
    
    //正式发布的App中不要这样做. Context对象存储在static变量中会造成内存泄露.
    public static Application sApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sApp = getApplication();
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(v -> startService(new Intent(this, WorkService.class)));
        findViewById(R.id.btn_jump).setOnClickListener(this::whiteListMatters);
    }

    /**
     * 处理白名单
     */
    private void whiteListMatters(View v) {
        for (IntentWrapper intentWrapper : IntentWrapper.getIntentWrapperList()) {
            //如果本机上没有能处理这个Intent的Activity，说明不是对应的机型，直接忽略进入下一次循环。
            if (!intentWrapper.doesActivityExists(this)) continue;
            switch (intentWrapper.type) {
                case IntentWrapper.HUAWEI:
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(false)
                            .setTitle("需要加入受保护的应用名单")
                            .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 加入到受保护的应用名单。\n\n" +
                                    "请点击『确定』，在弹出的受保护应用列表中，将 HelloDaemon 对应的开关打开。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case IntentWrapper.XIAOMI:
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(false)
                            .setTitle("需要加入自启动白名单")
                            .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 加入到自启动白名单。\n\n" +
                                    "请点击『确定』，在弹出的自启动管理界面中，将 HelloDaemon 对应的开关打开。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case IntentWrapper.XIAOMI_GOD:
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(false)
                            .setTitle("需要关闭 HelloDaemon 的神隐模式")
                            .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 的神隐模式关闭。\n\n" +
                                    "请点击『确定』，在弹出的神隐模式应用列表中，点击 HelloDaemon ，然后选择『无限制』和『允许定位』。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case IntentWrapper.SAMSUNG:
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(false)
                            .setTitle("需要允许 HelloDaemon 的自启动")
                            .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 在屏幕关闭时继续运行。\n\n" +
                                    "请点击『确定』，在弹出的 智能管理器 中，点击『内存』，选择『自启动应用程序』选项卡，将 HelloDaemon 对应的开关打开。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
            }
        }
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
}
