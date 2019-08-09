package com.thinkernote.ThinkerNote.Views;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.R;

/**
 * 音量弹窗
 */

public class VolumeDialog extends Dialog implements View.OnClickListener {

    private Context context;
    private DialogCallBack callBack;

    public VolumeDialog(Context context, DialogCallBack callBack) {
        super(context, R.style.dialogStyle);
        this.context = context;
        this.callBack = callBack;
        init();
    }

    public interface DialogCallBack {

        void sureBack(int str);

        void cancelBack();

    }

    SeekBar seekbar;
    TextView seekbar_dialog_textview;

    private void init() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_volume, null);
        TextView tv_sure = dialogView.findViewById(R.id.tv_sure);
        seekbar_dialog_textview = dialogView.findViewById(R.id.seekbar_dialog_textview);
        seekbar = dialogView.findViewById(R.id.seekbar_dialog_seekbar);
        TextView tv_cancel = dialogView.findViewById(R.id.tv_cancel);
        //
        seekbar.setProgress(TNSettings.getInstance().volume);
        seekbar_dialog_textview.setText(TNSettings.getInstance().volume + "");
        seekbar.setMax(100);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                seekbar_dialog_textview.setText(progress + "");
            }
        });
        tv_sure.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);

        setContentView(dialogView);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();

        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        this.getWindow().setAttributes(lp);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dismiss();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_sure:
                callBack.sureBack(seekbar.getProgress());
                dismiss();
                break;
            case R.id.tv_cancel:
                callBack.cancelBack();
                dismiss();
                break;
            default:
                break;
        }
    }

}
