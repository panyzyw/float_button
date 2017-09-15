package com.yongyida.robot.floatbutton.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.yongyida.robot.floatbutton.app.FloatButtonApplication;
import com.yongyida.robot.floatbutton.classes.FloatButtonInstance;
import com.yongyida.robot.floatbutton.classes.IFloatButton;

/**
 * 服务：启动悬浮按钮
 * */
public class ControlService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FloatButtonApplication.mFloatButton.onCreate();
    }
}
