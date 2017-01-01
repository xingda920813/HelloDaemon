package com.xdandroid.hellodaemon;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.xdandroid.hellodaemon.app.*;
import com.xdandroid.hellodaemon.service.*;
import com.xdandroid.hellodaemon.util.*;

import static com.xdandroid.hellodaemon.util.IntentWrapper.*;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(v -> startService(new Intent(App.sApp, WorkService.class)));
        findViewById(R.id.btn_white).setOnClickListener(this::whiteListMatters);
        /*
         * 停止服务并取消定时唤醒
         *
         * 停止服务使用取消订阅的方式实现，而不是调用 stopService。因为：
         * 1.stopService 会调用 Service.onDestroy()，而 WorkService 做了保活处理，会把 Service 再拉起来；
         * 2.我们希望 WorkService 起到一个类似于控制台的角色，即 WorkService 始终运行 (无论任务是否需要运行)，
         * 而是通过 onStart() 里自定义的条件，来决定服务是否应当启动或停止。
         */
        findViewById(R.id.btn_stop).setOnClickListener(v -> {if (WorkService.sSubscription != null) WorkService.sSubscription.unsubscribe();});
    }

    /**
     * 处理白名单
     */
    void whiteListMatters(View v) {
        boolean nothingMatches = true;
        for (IntentWrapper intentWrapper : INTENT_WRAPPER_LIST) {
            //如果本机上没有能处理这个Intent的Activity，说明不是对应的机型，直接忽略进入下一次循环。
            if (!intentWrapper.doesActivityExists()) continue;
            switch (intentWrapper.mType) {
                case DOZE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        if (pm.isIgnoringBatteryOptimizations(getPackageName())) break;
                        nothingMatches = false;
                        new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle("需要忽略 " + getApplicationName() + " 的电池优化")
                                .setMessage("轨迹跟踪服务的持续运行需要 " + getApplicationName() + " 加入到电池优化的忽略名单。\n\n" +
                                        "请点击『确定』，在弹出的『忽略电池优化』对话框中，选择『是』。")
                                .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                                .show();
                    }
                    break;
                case HUAWEI:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 " + getApplicationName() + " 自动启动")
                            .setMessage("轨迹跟踪服务的持续运行需要允许 " + getApplicationName() + " 的后台自动启动。\n\n" +
                                    "请点击『确定』，在弹出的『自动启动管理』中，将 " + getApplicationName() + " 对应的开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case ZTE_GOD:
                case HUAWEI_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("" + getApplicationName() + " 需要加入受保护的应用名单")
                            .setMessage("轨迹跟踪服务的持续运行需要 " + getApplicationName() + " 加入到受保护的应用名单。\n\n" +
                                    "请点击『确定』，在弹出的『受保护应用』列表中，将 " + getApplicationName() + " 对应的开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case XIAOMI_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要关闭 " + getApplicationName() + " 的神隐模式")
                            .setMessage("轨迹跟踪服务的持续运行需要 " + getApplicationName() + " 的神隐模式关闭。\n\n" +
                                    "请点击『确定』，在弹出的神隐模式应用列表中，点击 " + getApplicationName() + " ，然后选择『无限制』和『允许定位』。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case SAMSUNG:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 " + getApplicationName() + " 的自启动")
                            .setMessage("轨迹跟踪服务的持续运行需要 " + getApplicationName() + " 在屏幕关闭时继续运行。\n\n" +
                                    "请点击『确定』，在弹出的『智能管理器』中，点击『内存』，选择『自启动应用程序』选项卡，将 " + getApplicationName() + " 对应的开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case MEIZU:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 " + getApplicationName() + " 的自启动")
                            .setMessage("轨迹跟踪服务的持续运行需要允许 " + getApplicationName() + " 的自启动。\n\n" +
                                    "请点击『确定』，在弹出的应用信息界面中，将『自启动』开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case MEIZU_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("" + getApplicationName() + " 需要在待机时保持运行")
                            .setMessage("轨迹跟踪服务的持续运行需要 " + getApplicationName() + " 在待机时保持运行。\n\n" +
                                    "请点击『确定』，在弹出的『待机耗电管理』中，将 " + getApplicationName() + " 对应的开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case ZTE:
                case LETV:
                case XIAOMI:
                case OPPO:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 " + getApplicationName() + " 的自启动")
                            .setMessage("轨迹跟踪服务的持续运行需要 " + getApplicationName() + " 加入到自启动白名单。\n\n" +
                                    "请点击『确定』，在弹出的『自启动管理』中，将 " + getApplicationName() + " 对应的开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case OPPO_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 " + getApplicationName() + " 在后台运行")
                            .setMessage("轨迹跟踪服务的持续运行需要允许 " + getApplicationName() + " 在后台运行。\n\n" +
                                    "请点击『确定』，在弹出的『纯净后台应用管控』中，将 " + getApplicationName() + " 对应的开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case VIVO:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 " + getApplicationName() + " 的自启动")
                            .setMessage("轨迹跟踪服务的持续运行需要允许 " + getApplicationName() + " 的自启动。\n\n" +
                                    "请点击『确定』，在弹出的 i管家 中，找到『软件管理』->『自启动管理』，将 " + getApplicationName() + " 对应的开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case COOLPAD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 " + getApplicationName() + " 的自启动")
                            .setMessage("轨迹跟踪服务的持续运行需要允许 " + getApplicationName() + " 的自启动。\n\n" +
                                    "请点击『确定』，在弹出的『酷管家』中，找到『软件管理』->『自启动管理』，取消勾选 " + getApplicationName() + "，将 " + getApplicationName() + " 的状态改为『已允许』。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case VIVO_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("" + getApplicationName() + " 需要在后台高耗电时允许运行")
                            .setMessage("轨迹跟踪服务的持续运行需要允许 " + getApplicationName() + " 在后台高耗电时运行。\n\n" +
                                    "请点击『确定』，在弹出的『后台高耗电』中，将 " + getApplicationName() + " 对应的开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case GIONEE:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("" + getApplicationName() + " 需要加入应用自启和绿色后台白名单")
                            .setMessage("轨迹跟踪服务的持续运行需要允许 " + getApplicationName() + " 的自启动和后台运行。\n\n" +
                                    "请点击『确定』，在弹出的『系统管家』中，分别找到『应用管理』->『应用自启』和『绿色后台』->『清理白名单』，将 " + getApplicationName() + " 添加到白名单。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case LETV_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要禁止 " + getApplicationName() + " 被自动清理")
                            .setMessage("轨迹跟踪服务的持续运行需要禁止 " + getApplicationName() + " 被自动清理。\n\n" +
                                    "请点击『确定』，在弹出的『应用保护』中，将 " + getApplicationName() + " 对应的开关关闭。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case LENOVO:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 " + getApplicationName() + " 的后台 GPS 和后台运行")
                            .setMessage("轨迹跟踪服务的持续运行需要允许 " + getApplicationName() + " 的后台自启、后台 GPS 和后台运行。\n\n" +
                                    "请点击『确定』，在弹出的『后台管理』中，分别找到『后台自启』、『后台 GPS』和『后台运行』，将 " + getApplicationName() + " 对应的开关打开。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case LENOVO_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要关闭 " + getApplicationName() + " 的后台耗电优化")
                            .setMessage("轨迹跟踪服务的持续运行需要关闭 " + getApplicationName() + " 的后台耗电优化。\n\n" +
                                    "请点击『确定』，在弹出的『后台耗电优化』中，将 " + getApplicationName() + " 对应的开关关闭。")
                            .setPositiveButton("确定", (d, w) -> intentWrapper.startActivity(this))
                            .show();
                    break;
            }
        }
        if (nothingMatches) Toast.makeText(this, "不是对应的机型", Toast.LENGTH_SHORT).show();
    }

    /**
     * 防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
     */
    @Override
    public void onBackPressed() {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(launcherIntent);
    }
}
