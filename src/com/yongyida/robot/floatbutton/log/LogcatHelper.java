package com.yongyida.robot.floatbutton.log;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
public class LogcatHelper {
	private static final String TAG = "LogcatHelper";
	private static final String PATH_DIR = ".YYDFloatButtonLog";
	private static final String USER_FILE_NAME = ".log_pro.log";
	private static LogcatHelper INSTANCE = null;
	private static String PATH_LOGCAT;
	private LogDumper mLogDumper = null;
	private int mPId;

	public void init(Context context) {
//	    if (Environment.getExternalStorageState().equals(
//	    					Environment.MEDIA_MOUNTED)) {
	    	PATH_LOGCAT = Environment.getExternalStorageDirectory()
	    			.getAbsolutePath() + File.separator + PATH_DIR;
//	    } else {
//			PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + PATH_DIR;
//		}
	    
		File file = new File(PATH_LOGCAT);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static LogcatHelper getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new LogcatHelper(context);
		}
		return INSTANCE;
	}

	private LogcatHelper(Context context) {
		init(context);
		mPId = android.os.Process.myPid();
	}

	public void start() {
		if (mLogDumper == null) {
		    mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
		}
		mLogDumper.start();
	}

	public void stop() {
		if (mLogDumper != null) {
			mLogDumper.stopLogs();
			mLogDumper = null;
		}
		INSTANCE = null;
	}

	private class LogDumper extends Thread {

		private Process logcatProc;
		private BufferedReader mReader = null;
		private boolean mRunning = true;
		private String cmds = null;
		private String mPID;
		private FileOutputStream out = null;
		private File file;

		public LogDumper(String pid, String dir) {
			mPID = pid;
			file = new File(dir, USER_FILE_NAME);
			
			try {
				out = new FileOutputStream(file, true);
			} catch (IOException e) {
			    LogTool.showLog("LogDumper", "LogDumper", "IOException");
			}

			if (isFileOver5M(file.getAbsolutePath())) {
				if (file.exists()) {
					if (file.isFile()) {
						file.delete();
					}
				}
			}

			/**
			 * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
			 * 显示当前mPID程序的 E和W等级的日志.
			 */
			// cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
			// cmds = "logcat | grep \"(" + mPID + ")\"";//打印所有日志信息
			// cmds = "logcat -s way";//打印标签过滤信息
			cmds = "logcat *:e DUBBER:d IRCORE:d | grep \"(" + mPID + ")\"";
		}

		/** 把超过5M的日志文件删除  */
		boolean isFileOver5M(String filePath) {
			File f = new File(filePath);

			if (f.exists() && f.length() > 1024*1024*5)
				return true;
			return false;
		}

		public void stopLogs() {
			mRunning = false;
		}

		@Override
		public void run() {
			try {
				logcatProc = Runtime.getRuntime().exec(cmds);
				mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
				String line = null;
				while (mRunning && (line = mReader.readLine()) != null) {
					if (!mRunning) {
						break;
					}
					if (line.length() == 0) {
						continue;
					}
					if (out != null && line.contains(mPID)) {
						out.write((MyDate.getDateEN() + "  " + line + "\n").getBytes());
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
				LogTool.showLog(TAG, "run", "IOException " + e.getMessage());
			} finally {
				if (logcatProc != null) {
					logcatProc.destroy();
					logcatProc = null;
				}
				if (mReader != null) {
					try {
						mReader.close();
						mReader = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					out = null;
				}
			}
		}
	}
}
