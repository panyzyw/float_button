package com.yongyida.robot.floatbutton.log;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yongyida.robot.floatbutton.app.FloatButtonApplication;
import com.yongyida.robot.floatbutton.classes.GlobalVars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogTool {
    private static final String TAG = "YYDFloatButton";
    private static final String PATH_DIR = ".YYDFloatButtonLog";
    private static final String USER_FILE_NAME = ".log_user.log";
    private static String PATH_LOGCAT;
    private static FileOutputStream out = null;

    public static synchronized void showLog(String className, String methodName, String msg) {
        String log = className + "." + methodName + ": " + msg;
        int mPId = android.os.Process.myPid();
        String record = "D/" + TAG + " (" + String.valueOf(mPId) + "):" + log;
        String[] logs = { log, record };

        if (GlobalVars.DEBUG) {
            if (logs != null && logs.length >= 2) {
                Log.d(TAG, logs[0]);
                writeLogFile(logs[1]);
            }
        }
    }

    static synchronized void writeLogFile(String string) {
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                    + PATH_DIR;
//        } else {
//            PATH_LOGCAT = FloatButtonApplication.getAppContext().getFilesDir().getAbsolutePath() + File.separator
//                    + PATH_DIR;
//        }

        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(PATH_LOGCAT, USER_FILE_NAME);
        if (file != null) {
            try {
                out = new FileOutputStream(file, true);
                if (isFileOver5M(file.getAbsolutePath())) {
                    if (file.exists()) {
                        if (file.isFile()) {
                            file.delete();
                        }
                    }
                }
                if (string.length() == 0) {
                    return;
                }
                byte[] buffer = (MyDate.getDateEN() + "  " + string + "\n").getBytes();
                if (buffer != null && out != null) {
                    out.write(buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static boolean isFileOver5M(String filePath) {
        File f = new File(filePath);

        if (f.exists() && f.length() > 1024 * 1024 * 5)
            return true;
        return false;
    }
}
