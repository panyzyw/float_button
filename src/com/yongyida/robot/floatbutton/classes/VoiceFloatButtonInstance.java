package com.yongyida.robot.floatbutton.classes;

import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.yongyida.robot.floatbutton.R;
import com.yongyida.robot.floatbutton.activity.ButtonsDialogActivity;
import com.yongyida.robot.floatbutton.app.FloatButtonApplication;
import com.yongyida.robot.floatbutton.log.LogTool;

/**
 * 悬浮按钮实例
 * TODO 新增语音监听功能UI显示 - 2016.12
 */
public class VoiceFloatButtonInstance implements IFloatButton, View.OnTouchListener {

    private static String TAG = "FloatButtonInstance";
    private Context context;

    private static final int BUTTON_DISPLAY_MODE_DOWN = 0x0;
    private static final int BUTTON_DISPLAY_MODE_UP = 0x1;
    private static final int BUTTON_DISPLAY_MODE_HIDE = 0x2;

    private static WindowManager windowManager;
    private static WindowManager.LayoutParams mLayoutParams;
    private static Button mFloatButton;
    private DisplayMetrics displayMetrics = new DisplayMetrics();

    private int iconViewX = 0;
    private static float iconDownAlpha = 0.7f;
    private static float iconUpAlpha = 1f;
    private static float iconHideAlpha = 0.5f;
    private int size = 0;
    private static boolean inShowTime = false;

    // 表示状态
    /**
     * 普通状态
     */
    private static final int STATE_NORMAL = 0;
    /**
     * 监听状态
     */
    private static final int STATE_LISTEN = 1;
    /**
     * 当前状态
     */
    private static int mCurrentState = STATE_NORMAL;

    /**
     * 监听音量
     */
    private final String TAG_VOLUME = "TAGVolume";
    private int currentVolume = 10;
    private GradientDrawable drawableVolume;
    private VolumeChangeRunnable volumeChangeRunnable;

    private final int[] lowColors = {0xff0099cc, 0xffffffff};
    private final int[] midColors = {0xff8bc34a, 0xffffffff};
    private final int[] highColors = {0xffff4500, 0xffffffff};

    private GestureDetector mGestureDetector;

    private VoiceFloatButtonInstance() {
        context = FloatButtonApplication.getAppContext();
    }

    /**
     * 获取VoiceFloatButtonInstance单例
     */
    public enum FloatButtonCreator {
        INSTANCE;
        private VoiceFloatButtonInstance mInstance;

        FloatButtonCreator() {
            mInstance = new VoiceFloatButtonInstance();
        }

        public VoiceFloatButtonInstance getFloatButton() {
            return mInstance;
        }
    }

    @Override
    public void onCreate() {
        if (context == null) {
            LogTool.showLog(TAG, "FloatButtonInstance", "[Exception] context == null");
            return;
        }

        try {

            mGestureDetector = new GestureDetector(context, new ButtonGestureDetector());
            initVolumeUI();
            initButton();

        } catch (Exception e) {
            LogTool.showLog(TAG, "FloatButtonInstance", "[Exception] " + e.getMessage());
        }

    }

    private void initButton() {
        if (mFloatButton == null) {
            mFloatButton = new Button(context);
            mFloatButton.setBackgroundResource(R.drawable.selector_btn_launcher);
            mFloatButton.setOnTouchListener(this);
        }

        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.alpha = iconHideAlpha;
        mLayoutParams.x = iconViewX;

        // 适合不同分辨率
        size = Integer.parseInt(context.getResources().getString(R.string.float_button_size));
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        mLayoutParams.y = displayMetrics.heightPixels / 2 - size + 140;
        mLayoutParams.width = size;
        mLayoutParams.height = size;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        windowManager.addView(mFloatButton, mLayoutParams);
    }

    private void initVolumeUI() {
        // 设置监听时的UI
        currentVolume = 10;
        drawableVolume = new GradientDrawable();
        drawableVolume.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        drawableVolume.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        drawableVolume.setCornerRadius(Integer.parseInt(context.getResources().getString(R.string.float_button_size)));
        drawableVolume.setGradientCenter(0.5f, 0.5f);
        volumeChangeRunnable = new VolumeChangeRunnable();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.yongyida.robot.VOLUME_CHANGE");
        context.registerReceiver(new VolumeChangeReceiver(), filter);
    }

    /**
     * 语音监听时，UI变化的动画
     */
    private class VolumeChangeRunnable implements Runnable {

        private boolean isRun;
        private int task = 3;
        private static final int TASK_RISE = 0;
        private static final int TASK_GO_BACK = 1;
        private static final int TASK_DROP = 2;

        private int changeVolume = 10;

        VolumeChangeRunnable() {
            this.isRun = false;
            this.task = 3;
        }

        @Override
        public void run() {

            if (!isRun) {
                return;
            }

            switch (task) {
                case TASK_RISE:
                    currentVolume += 6;
                    if (currentVolume >= changeVolume) {
                        currentVolume = changeVolume;
                        setTask(TASK_DROP);
                    }
                    break;

                case TASK_GO_BACK:
                    currentVolume -= 6;
                    if (currentVolume <= changeVolume) {
                        currentVolume = changeVolume;
                        setTask(TASK_DROP);
                    }
                    break;

                case TASK_DROP:
                    currentVolume -= 3;
                    if (currentVolume <= 10) {
                        currentVolume = 10;
                        setRun(false);
                    }
                    break;

                default:
                    setRun(false);
                    break;
            }

            if (currentVolume >= 10 && currentVolume < 41) {
                drawableVolume.setColors(lowColors);
            } else if (currentVolume >= 41 && currentVolume < 71) {
                drawableVolume.setColors(midColors);
            } else if (currentVolume >= 71 && currentVolume <= 100) {
                drawableVolume.setColors(highColors);
            }
            drawableVolume.setGradientRadius(currentVolume);
            mFloatButton.setBackground(drawableVolume);
            mFloatButton.postDelayed(volumeChangeRunnable, 17);
        }

        void setTask(int task) {
            this.task = task;
        }

        void setChangeVolume(int changeVolume) {
            this.changeVolume = changeVolume * 3 + 10;
        }

        public boolean isRun() {
            return isRun;
        }

        void setRun(boolean run) {
            isRun = run;
        }
    }

    /**
     * 监听语音音量变化的广播
     */
    private class VolumeChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String result = intent.getStringExtra("result");

            switch (result) {
                case "out":
                    mCurrentState = STATE_NORMAL;
                    currentVolume = 10;
                    volumeChangeRunnable.setRun(false);
                    mFloatButton.removeCallbacks(volumeChangeRunnable);
                    mFloatButton.setBackgroundResource(R.drawable.selector_btn_launcher);

                    // 恢复悬浮按钮UI，并且在倒计时 showTime 后半透明
                    alphaHandler.removeMessages(0);
                    inShowTime = true;
                    alphaHandler.sendEmptyMessageDelayed(0, 4000);
                    mLayoutParams.alpha = iconUpAlpha;
                    windowManager.updateViewLayout(mFloatButton, mLayoutParams);
                    System.gc();
                    break;

                case "enter":
                    mCurrentState = STATE_LISTEN;
                    mFloatButton.removeCallbacks(volumeChangeRunnable);
                    volumeChangeRunnable.setRun(true);
                    volumeChangeRunnable.setTask(3);
                    currentVolume = 10;
                    mFloatButton.post(volumeChangeRunnable);
                    break;

                default:
                    mCurrentState = STATE_LISTEN;
                    volumeChangeRunnable.setRun(false);
                    mFloatButton.removeCallbacks(volumeChangeRunnable);

                    int volume = Integer.decode(result);
                    if (volume >= currentVolume) {
                        volumeChangeRunnable.setTask(0);
                    } else {
                        volumeChangeRunnable.setTask(1);
                    }
                    volumeChangeRunnable.setRun(true);
                    volumeChangeRunnable.setChangeVolume(volume);
                    mFloatButton.post(volumeChangeRunnable);
                    break;
            }
        }
    }
	
    private Handler alphaHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            setVisible(BUTTON_DISPLAY_MODE_HIDE);
            return true;
        }
    });

    @Override
    public void setVisible(int mode) {
        if (mLayoutParams == null || windowManager == null) {
            return;
        }
        if (mFloatButton == null) {
            return;
        }
        switch (mode) {
            case BUTTON_DISPLAY_MODE_DOWN:
                mLayoutParams.alpha = iconDownAlpha;
                windowManager.updateViewLayout(mFloatButton, mLayoutParams);
                break;

            case BUTTON_DISPLAY_MODE_UP:
                mLayoutParams.alpha = iconUpAlpha;
                windowManager.updateViewLayout(mFloatButton, mLayoutParams);
                if (alphaHandler != null && mCurrentState == STATE_NORMAL) {
                    inShowTime = true;
                    alphaHandler.removeMessages(0);
                    alphaHandler.sendEmptyMessageDelayed(0, 4000);
                }
                break;

            case BUTTON_DISPLAY_MODE_HIDE:
                mLayoutParams.alpha = iconHideAlpha;
                windowManager.updateViewLayout(mFloatButton, mLayoutParams);
                inShowTime = false;
                break;
        }
    }

    @Override
    public void performSingleClick() {
        if (mCurrentState == STATE_NORMAL) {
            // TODO 普通状态可以单双击功能
            // 单击事件，返回上级
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Instrumentation ins = new Instrumentation();
                        ins.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                    } catch (Exception e) {
                        LogTool.showLog(TAG, "addIconView", "[Exception] " + e.getMessage());
                    }
                }
            }).start();

        } else {
            // TODO 监听语音时，单击变为取消监听，无双击
            // 通知主服务停止监听
            Intent intent = new Intent("com.yongyida.robot.APPLE_STOP_MONITOR");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(intent);

            mCurrentState = STATE_NORMAL;
            currentVolume = 10;
            volumeChangeRunnable.setRun(false);
            mFloatButton.removeCallbacks(volumeChangeRunnable);
            mFloatButton.setBackgroundResource(R.drawable.selector_btn_launcher);
        }
    }

    @Override
    public void performDoubleClick() {
        if (mCurrentState == STATE_NORMAL) {
            // TODO 普通状态可以单双击功能
            // 双击事件, 打开菜单
            Context con = FloatButtonApplication.getAppContext();
            Intent intent = new Intent(con, ButtonsDialogActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            con.startActivity(intent);

        } else {
            // TODO 监听语音时，单击变为取消监听，无双击
            // 通知主服务停止监听
            Intent intent = new Intent("com.yongyida.robot.APPLE_STOP_MONITOR");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(intent);

            mCurrentState = STATE_NORMAL;
            currentVolume = 10;
            volumeChangeRunnable.setRun(false);
            mFloatButton.removeCallbacks(volumeChangeRunnable);
            mFloatButton.setBackgroundResource(R.drawable.selector_btn_launcher);
        }
    }

    @Override
    public void performLongClick() {
        // 长按事件
        Context con = FloatButtonApplication.getAppContext();
        Intent intent = new Intent(con, ButtonsDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        con.startActivity(intent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean result = mGestureDetector != null && mGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mLayoutParams != null) {
                int widthPixels = displayMetrics.widthPixels;
                if (event.getRawX() <= widthPixels / 2) {
                    mLayoutParams.x = 0;
                } else {
                    mLayoutParams.x = widthPixels;
                }
                if (mCurrentState == STATE_NORMAL) {
                    mFloatButton.setBackgroundResource(R.drawable.selector_btn_launcher);
                }
                setVisible(BUTTON_DISPLAY_MODE_UP);
            }
        }
        return result;
    }

    private void reLayout(float newPosX, float newPosY, float alpha) {
        if (mLayoutParams == null || windowManager == null || mFloatButton == null) {
            return;
        }
        mLayoutParams.alpha = alpha;
        iconViewX = (int) newPosX;
        mLayoutParams.x = (int) newPosX;
        mLayoutParams.y = (int) newPosY;
        windowManager.updateViewLayout(mFloatButton, mLayoutParams);
    }

    public void setButtonHide(boolean isHide) {
        try {
            if (isHide) {
                // TODO 需要隐藏悬浮球
                if (mCurrentState == STATE_NORMAL) {
                    mLayoutParams.alpha = 0.0f;
                    windowManager.updateViewLayout(mFloatButton, mLayoutParams);
                    mFloatButton.setEnabled(false);
                    mFloatButton.setVisibility(View.GONE);

                } else {
                    // 显示语音监听动画时，不透明
                    mLayoutParams.alpha = iconUpAlpha;
                    windowManager.updateViewLayout(mFloatButton, mLayoutParams);
                    mFloatButton.setEnabled(true);
                    if (mFloatButton.getVisibility() != View.VISIBLE) {
                        mFloatButton.setVisibility(View.VISIBLE);
                    }
                }

            } else {
                // TODO 需要显示悬浮球的应用界面
                if (mFloatButton != null) {
                    if (!inShowTime) {
                        mLayoutParams.alpha = iconHideAlpha;
                    }
                    if (mCurrentState == STATE_LISTEN) {
                        // TODO 显示语音监听动画时，不透明
                        mLayoutParams.alpha = iconUpAlpha;
                    }
                    windowManager.updateViewLayout(mFloatButton, mLayoutParams);
                    mFloatButton.setEnabled(true);
                    if (mFloatButton.getVisibility() != View.VISIBLE) {
                        mFloatButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        } catch (Exception e) {
            LogTool.showLog(TAG, "onReceive", "[Error] >> " + e.getMessage());
        }
    }

    private class ButtonGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            setVisible(BUTTON_DISPLAY_MODE_DOWN);
            return super.onDown(event);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float dx = e2.getRawX() - size / 2;
            float dy = e2.getRawY() - size;
            reLayout(dx, dy, iconDownAlpha);
            if (mCurrentState == STATE_NORMAL) {
                // TODO 普通状态时，长按移动，并更换背景; 监听语音时，长按移动，不更换背景
                mFloatButton.setBackgroundResource(R.drawable.ic_button_moving);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            performSingleClick();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            performDoubleClick();
            return super.onDoubleTapEvent(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            performLongClick();
            super.onLongPress(e);
        }
    }

}
