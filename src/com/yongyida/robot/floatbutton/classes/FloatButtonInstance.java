package com.yongyida.robot.floatbutton.classes;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
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
 */
public class FloatButtonInstance implements IFloatButton, View.OnTouchListener {

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

    private GestureDetector mGestureDetector;

    private FloatButtonInstance() {
        context = FloatButtonApplication.getAppContext();
    }

    /**
     * 获取FloatButtonInstance单例
     */
    public enum FloatButtonCreator {
        INSTANCE;
        private FloatButtonInstance mInstance;

        FloatButtonCreator() {
            mInstance = new FloatButtonInstance();
        }

        public FloatButtonInstance getFloatButton() {
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
                if (alphaHandler != null) {
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
    }

    @Override
    public void performDoubleClick() {
        // 双击事件
        Context con = FloatButtonApplication.getAppContext();
        Intent intent = new Intent(con, ButtonsDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        con.startActivity(intent);
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
                mFloatButton.setBackgroundResource(R.drawable.selector_btn_launcher);
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
                if (mFloatButton != null) {
                    mLayoutParams.alpha = 0.0f;
                    windowManager.updateViewLayout(mFloatButton, mLayoutParams);
                    mFloatButton.setEnabled(false);
                    mFloatButton.setVisibility(View.GONE);
                }

            } else {
                // TODO 需要显示悬浮球的应用界面
                if (mFloatButton != null) {
                    if (!inShowTime) {
                        mLayoutParams.alpha = iconHideAlpha;
                    } else {
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
            // TODO 移动，并更换背景;
            mFloatButton.setBackgroundResource(R.drawable.ic_button_moving);
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
