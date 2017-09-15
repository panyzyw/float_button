package com.yongyida.robot.floatbutton.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.yongyida.robot.floatbutton.activity.MainActivity;
import com.yongyida.robot.floatbutton.log.LogTool;

public class RestartAppService extends Service {
    private static final String TAG = "RestartAppService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(main);
        } catch (Exception e) {
            LogTool.showLog(TAG, "onStartCommand", "[Exception] " + e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
