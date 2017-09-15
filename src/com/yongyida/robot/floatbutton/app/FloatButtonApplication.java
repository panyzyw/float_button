package com.yongyida.robot.floatbutton.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;


import com.yongyida.robot.floatbutton.classes.FloatButtonInstance;
import com.yongyida.robot.floatbutton.classes.IFloatButton;
import com.yongyida.robot.floatbutton.classes.VoiceFloatButtonInstance;
import com.yongyida.robot.floatbutton.log.LogTool;
import com.yongyida.robot.floatbutton.receiver.BootCompleteReceiver;
import com.yongyida.robot.floatbutton.util.UnCatchHandler;

import java.util.ArrayList;

public class FloatButtonApplication extends Application {
    private static final String TAG = "FloatButtonApp";
    private static Context appContext;
    ArrayList<Activity> activities = new ArrayList<Activity>();
    static FloatButtonApplication application = new FloatButtonApplication();
    private BootCompleteReceiver bootCompleteReceiver;
    public static IFloatButton mFloatButton;

    public static FloatButtonApplication getApplication() {
        return application;
    }

    public void init() {
        UnCatchHandler unCatch = new UnCatchHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(unCatch);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        activities.clear();
//        mFloatButton = FloatButtonInstance.FloatButtonCreator.INSTANCE.getFloatButton();
        mFloatButton = VoiceFloatButtonInstance.FloatButtonCreator.INSTANCE.getFloatButton();

        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        bootCompleteReceiver = new BootCompleteReceiver();
        registerReceiver(bootCompleteReceiver, filter);
    }

    @Override
    public void onTerminate() {
        unregisterReceiver(bootCompleteReceiver);
        super.onTerminate();
    }

    public static Context getAppContext() {
        return appContext;
    }

    public void addActivity(Activity act) {
        activities.add(act);
    }

    public void removeActivity(Activity act) {
        activities.remove(act);
    }

    /**
     * 终止每一个Activity
     * */
    public void finishActivity() {
        if (activities != null && activities.size() > 0) {
            for (Activity activity : activities) {
                if (activity != null) {
                    activity.finish();
                }
            }
            LogTool.showLog(TAG, "finishActivity", "count = " + activities.size());
        }
        Process.killProcess(Process.myPid());
    }

}
