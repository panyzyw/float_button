package com.yongyida.robot.floatbutton.activity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.yongyida.robot.floatbutton.R;
import com.yongyida.robot.floatbutton.log.LogTool;

/**
 * @author Bright. Create on 2016/11/17 0017.
 */
public class ButtonsDialogActivity extends Activity {
    private static final String TAG = ButtonsDialogActivity.class.getSimpleName();
    ImageButton btn_home, btn_recent;
    LinearLayout ll_layout;

    Handler finishHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout);
        initUI();
    }

    private void initUI() {
        btn_home = (ImageButton) findViewById(R.id.btn_home);
        btn_recent = (ImageButton) findViewById(R.id.btn_recent);
        ll_layout = (LinearLayout) findViewById(R.id.ll_layout);
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);

                Intent intent1 = new Intent();
                intent1.setAction("com.yydrobot.HOME");
                sendBroadcast(intent1);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        btn_recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Instrumentation ins = new Instrumentation();
                            ins.sendKeyDownUpSync(KeyEvent.KEYCODE_APP_SWITCH);
                        } catch (Exception e) {
                            LogTool.showLog(TAG, "addIconView", "[Exception] " + e.getMessage());
                        }
                    }
                }).start();
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        ll_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        finishHandler.sendEmptyMessageDelayed(0, 8 * 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
