package com.yongyida.robot.floatbutton.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.yongyida.robot.floatbutton.app.FloatButtonApplication;
import com.yongyida.robot.floatbutton.activity.MainActivity;
import com.yongyida.robot.floatbutton.log.LogTool;

public class UnCatchHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "UnCatchHandler";
    FloatButtonApplication application;
    Context mContext;
    Thread.UncaughtExceptionHandler mDefaultHandler;
    PackageManager pm;
    Intent intent;
    PendingIntent restartIntent;
    AlarmManager manager;

    public UnCatchHandler(FloatButtonApplication application) {
        this.application = application;
        mContext = FloatButtonApplication.getAppContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * The thread is being terminated by an uncaught exception. Further
     * exceptions thrown in this method are prevent the remainder of the
     * method from executing, but are otherwise ignored.
     *
     * @param thread the thread that has an uncaught exception
     * @param ex     the exception that was thrown
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (ex == null && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            LogTool.showLog(TAG, "handlerException", "Error!!!! >> \n[ " + ex.getMessage() + " ]");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LogTool.showLog(TAG, "uncaughtException", "InterruptedException [ " + e.getMessage() + " ]");
            }
            intent = new Intent(mContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            restartIntent = PendingIntent.getActivity(application.getApplicationContext(),
                    0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            manager = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
            if (manager != null && restartIntent != null && application != null) {
                manager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
                application.finishActivity();
            }
        }
    }

}
