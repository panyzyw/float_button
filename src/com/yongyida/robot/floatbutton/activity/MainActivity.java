package com.yongyida.robot.floatbutton.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.yongyida.robot.floatbutton.app.FloatButtonApplication;
import com.yongyida.robot.floatbutton.classes.FloatButtonInstance;
import com.yongyida.robot.floatbutton.classes.GlobalVars;
import com.yongyida.robot.floatbutton.log.LogTool;
import com.yongyida.robot.floatbutton.service.ControlService;

public class MainActivity extends Activity {
    String TAG = "MainActivity";
    FloatButtonApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            application = FloatButtonApplication.getApplication();
            application.init();
            application.addActivity(this);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            GlobalVars.width = displayMetrics.widthPixels;
            GlobalVars.height = displayMetrics.heightPixels;

            Intent intent = new Intent(MainActivity.this, ControlService.class);
            startService(intent);

            finish();
        } catch (Exception e) {
            LogTool.showLog(TAG, "onCreate", "[Exception] " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        application.removeActivity(this);
    }

}
