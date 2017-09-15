package com.yongyida.robot.floatbutton.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yongyida.robot.floatbutton.log.LogTool;
import com.yongyida.robot.floatbutton.service.ControlService;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompleteReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
            try {
                Intent intent1 = new Intent(context, ControlService.class);
                context.startService(intent1);
            } catch (Exception e) {
                LogTool.showLog(TAG, "onReceive", "[Exception] " + e.getMessage());
            }
        }
    }
}
