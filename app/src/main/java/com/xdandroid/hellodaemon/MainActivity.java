package com.xdandroid.hellodaemon;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;

import com.xdandroid.hellodaemon.service.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(v -> startService(new Intent(this, WorkService.class)));
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
