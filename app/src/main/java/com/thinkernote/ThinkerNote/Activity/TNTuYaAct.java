package com.thinkernote.ThinkerNote.Activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsView;
import com.thinkernote.ThinkerNote.other.TuyaView;
import com.thinkernote.ThinkerNote.dialog.CommonDialog;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * 写笔记--涂鸦主界面 sjy 0614
 */
public class TNTuYaAct extends TNActBase implements OnClickListener,
        OnCheckedChangeListener, OnSeekBarChangeListener {
    private Paint mPaint;
    private TuyaView mTuYa;
    private boolean isEraser = false;
    private int oColor;
    private float oPaintWith;
    //	private FrameLayout mColorLayout;
    RadioGroup mColorLayout;
    private LinearLayout mStrokelayout;

    private SeekBar mStrokeWidth;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                mColorLayout.setVisibility(View.GONE);
                mStrokelayout.setVisibility(View.GONE);
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tuya);
        setViews();

        findViewById(R.id.tuya_back_btn).setOnClickListener(this);
        findViewById(R.id.tuya_color_btn).setOnClickListener(this);
        findViewById(R.id.tuya_eraser_btn).setOnClickListener(this);
        findViewById(R.id.tuya_save_btn).setOnClickListener(this);
        findViewById(R.id.tuya_strokewidth_btn).setOnClickListener(this);
        findViewById(R.id.tuya_clear_btn).setOnClickListener(this);
        findViewById(R.id.tuya_redo_btn).setOnClickListener(this);
        findViewById(R.id.tuya_undo_btn).setOnClickListener(this);

        initView();
    }

    private void initView() {
        FrameLayout doodleLayout = (FrameLayout) findViewById(R.id.tuya_view_layout);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        WindowManager manager = getWindow().getWindowManager();
        int height = (int) (manager.getDefaultDisplay().getHeight());
        int width = (int) (manager.getDefaultDisplay().getWidth());

        mTuYa = new TuyaView(this, mPaint, width, height);
        doodleLayout.addView(mTuYa);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float scale = dm.scaledDensity;

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        params.setMargins((int) (4 * scale), 0, (int) (4 * scale),
                (int) (6 * scale));
        View colorLayout = layoutInflater.inflate(R.layout.doodle_colorlayout,
                null);
        View strokeLayout = layoutInflater.inflate(
                R.layout.doodle_strokewidth_layout, null);
        doodleLayout.addView(colorLayout, params);
        doodleLayout.addView(strokeLayout, params);

        colorLayout.setVisibility(View.GONE);
        strokeLayout.setVisibility(View.GONE);

        mColorLayout = (RadioGroup) colorLayout
                .findViewById(R.id.doodle_color_rg);
        mStrokelayout = (LinearLayout) strokeLayout
                .findViewById(R.id.doodle_strokewidth_layout);
        mColorLayout.setOnCheckedChangeListener(this);
        mStrokeWidth = (SeekBar) mStrokelayout.findViewById(R.id.doodle_strokewidth_seekbar);
        mStrokeWidth.setOnSeekBarChangeListener(this);
        mStrokeWidth.setProgress(12);
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.tuya_toolbar,
                R.drawable.toolbg);
        TNUtilsSkin.setViewBackground(this, null, R.id.tuya_bottom_toolbar,
                R.drawable.bottom_toolbg);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.tuya_color_btn, R.drawable.tuya_select_color_selector);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.tuya_strokewidth_btn, R.drawable.tuya_strokewidth_selector);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.tuya_redo_btn, R.drawable.tuya_redu_selector);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.tuya_undo_btn, R.drawable.tuya_undo_selector);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.tuya_clear_btn, R.drawable.tuya_clear_selector);
        TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null,
                R.id.tuya_eraser_btn, R.drawable.tuya_eraser);
        TNUtilsSkin.setViewBackground(this, null, R.id.tuya_page,
                R.drawable.page_bg);
    }

    @Override
    protected void configView() {
        super.configView();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        startViewVisibleTimeing();
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);
        switch (checkedId) {
            case R.id.doodle_color_read: {
                mPaint.setColor(0xFFD80000);
                break;
            }
            case R.id.doodle_color_pink: {
                mPaint.setColor(0xFFDF006E);
                break;
            }
            case R.id.doodle_color_yellow: {
                mPaint.setColor(0xFFFFFC00);
                break;
            }
            case R.id.doodle_color_green: {
                mPaint.setColor(0xFF4CD800);
                break;
            }
            case R.id.doodle_color_blue: {
                mPaint.setColor(0xFF00ADD8);
                break;
            }
            case R.id.doodle_color_gray: {
                mPaint.setColor(0xFFA3A3A3);
                break;
            }
            case R.id.doodle_color_black: {
                mPaint.setColor(0xFF000000);
                break;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (progress < 3) {
            mPaint.setStrokeWidth(3);
            return;
        }
        mPaint.setStrokeWidth(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        startViewVisibleTimeing();
    }

    @Override
    public void onClick(View v) {
        if (mColorLayout.getVisibility() == View.VISIBLE) {
            mColorLayout.setVisibility(View.GONE);
            if (v.getId() == R.id.tuya_color_btn) {
                return;
            }
        }
        if (mStrokelayout.getVisibility() == View.VISIBLE) {
            mStrokelayout.setVisibility(View.GONE);
            if (v.getId() == R.id.tuya_strokewidth_btn) {
                return;
            }
        }

        switch (v.getId()) {
            case R.id.tuya_back_btn:
                back();
                break;
            case R.id.tuya_color_btn:
                if (!isEraser) {
                    // new ColorPickerDialog(this, mPaint.getColor(), "",
                    // this).show();
                    mColorLayout.setVisibility(View.VISIBLE);
                    startViewVisibleTimeing();
                }
                break;
            case R.id.tuya_eraser_btn: {
                if (!isEraser) {
                    oColor = mPaint.getColor();
                    oPaintWith = mPaint.getStrokeWidth();
                    mPaint.setColor(Color.WHITE);
                    mPaint.setStrokeWidth(48f);
                    isEraser = true;
                    TNUtilsSkin.setImageButtomDrawableAndStateBackground(this,
                            null, R.id.tuya_eraser_btn,
                            R.drawable.tuya_eraser_pressed);

                    findViewById(R.id.tuya_color_btn).setEnabled(false);
                    findViewById(R.id.tuya_redo_btn).setEnabled(false);
                    findViewById(R.id.tuya_undo_btn).setEnabled(false);
                    findViewById(R.id.tuya_clear_btn).setEnabled(false);
                } else {
                    mPaint.setColor(oColor);
                    mPaint.setStrokeWidth(oPaintWith);
                    isEraser = false;
                    TNUtilsSkin.setImageButtomDrawableAndStateBackground(this,
                            null, R.id.tuya_eraser_btn, R.drawable.tuya_eraser);
                    findViewById(R.id.tuya_color_btn).setEnabled(true);
                    findViewById(R.id.tuya_redo_btn).setEnabled(true);
                    findViewById(R.id.tuya_undo_btn).setEnabled(true);
                    findViewById(R.id.tuya_clear_btn).setEnabled(true);
                }
                break;
            }
            case R.id.tuya_save_btn: {
                String path = TNUtilsView.saveViewToImage(mTuYa);
                if (path == null) {
                    return;
                }
                Intent it = new Intent();
                it.putExtra("TuYa", path);
                setResult(Activity.RESULT_OK, it);
                finish();
                break;
            }
            case R.id.tuya_strokewidth_btn: {
                mStrokelayout.setVisibility(View.VISIBLE);
                startViewVisibleTimeing();
                break;
            }
            case R.id.tuya_redo_btn: {
                if (!isEraser)
                    mTuYa.redo();
                break;
            }
            case R.id.tuya_undo_btn: {
                if (!isEraser)
                    mTuYa.undo();
                break;
            }
            case R.id.tuya_clear_btn: {
                if (!isEraser)
                    mTuYa.clear();
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void back() {
        CommonDialog dialog = new CommonDialog(this, R.string.alert_TuYa_SaveMsg,
                "保存",
                "不保存",
                new CommonDialog.DialogCallBack() {
                    @Override
                    public void sureBack() {
                        String path = TNUtilsView.saveViewToImage(mTuYa);
                        Intent it = new Intent();
                        it.putExtra("TuYa", path);
                        setResult(Activity.RESULT_OK, it);
                        finish();
                    }

                    @Override
                    public void cancelBack() {
                        setResult(Activity.RESULT_CANCELED, null);
                        finish();
                    }

                });
        if (!isFinishing()) {
            dialog.show();
        }
    }

    private void startViewVisibleTimeing() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        mTimer = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                mHandler.sendEmptyMessage(1);
            }
        };

        mTimer.schedule(mTimerTask, 3 * 1000);
    }
}
