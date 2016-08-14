package com.xdandroid.hellodaemon;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;

import com.xdandroid.hellodaemon.service.android.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(v -> startService(new Intent(this, WorkService.class)));
    }
}
