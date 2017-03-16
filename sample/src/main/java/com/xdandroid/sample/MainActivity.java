package com.xdandroid.sample;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.TextView;

import com.xdandroid.hellodaemon.*;

public class MainActivity extends Activity {
    private TextView txtSocket,txtSocketStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, TraceServiceImpl.class));
            }
        });
        //处理白名单
        findViewById(R.id.btn_white).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {IntentWrapper.whiteListMatters(MainActivity.this, "轨迹跟踪服务的持续运行");}
        });
        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TraceServiceImpl.stopService();
            }
        }
        );
        findViewById(R.id.btn_address).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //txtSocket.setText(App.URL_SOCKET);
            }
        });
        txtSocket = (TextView) findViewById(R.id.txt_socket);

        findViewById(R.id.btn_view).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((TextView) findViewById(R.id.txt_view)).setText(App.STATUS);
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //txtSocket.setText(App.URL_SOCKET);
    }

    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
    @Override public void onBackPressed() { IntentWrapper.onBackPressed(this); }

}
