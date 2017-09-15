package com.yongyida.robot.floatbutton.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yongyida.robot.floatbutton.R;
import com.yongyida.robot.floatbutton.app.FloatButtonApplication;
import com.yongyida.robot.floatbutton.log.LogTool;

import java.util.Arrays;
import java.util.List;

/**
 * 接收HomeLauncher来的广播，保持运行状态
 * 判断是否应该显示或隐藏, （Launcher需每个2秒一次广播）
 *
 * @author Bright. Create on 2017/3/10.
 */
public class ShowOrHideReceiver extends BroadcastReceiver {
    private static String TAG = ShowOrHideReceiver.class.getSimpleName();

    static int count = 0;
    String topActivity;
    String className;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !"floatbutton.ACTION_SHOW_HIDE".equals(intent.getAction())) {
            return;
        }
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null && activityManager.getRunningTasks(1) != null) {

            topActivity = activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
            className = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
            if (count++ > 5) {
                count = 0;
                LogTool.showLog(TAG, "onReceive", "topActivity: " + topActivity + ", " +
                        "class: " + className);
            }

            List<String> mInvisiblePackages =
                    Arrays.asList(context.getResources().getStringArray(R.array.invisible_package));

            boolean isHide =
                    mInvisiblePackages.contains(topActivity) || mInvisiblePackages.contains(className);

            FloatButtonApplication.mFloatButton.setButtonHide(isHide);
        }
    }
}
