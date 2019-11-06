package com.thinkernote.ThinkerNote.views.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;

/**
 * 更新弹窗
 */

public class UpdateDialog extends Dialog implements View.OnClickListener {

    private Context context;
    private DialogCallBack callBack;

    public UpdateDialog(Context context, String versionName, String newVersionName, String description, DialogCallBack callBack) {
        super(context, R.style.dialogStyle);
        this.context = context;
        this.callBack = callBack;
        init(versionName, newVersionName, description);
    }

    public interface DialogCallBack {

        void sureBack();

        void cancelBack();

    }

    TextView update_hint, tv_sure, tv_cancel;
    ProgressBar update_progressbar;
    TextView percent;

    private void init(String versionName, String newVersionName, String description) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);

        update_hint = dialogView.findViewById(R.id.update_hint);
        percent = dialogView.findViewById(R.id.update_percent);
        update_progressbar = dialogView.findViewById(R.id.update_progressbar);
        tv_sure = dialogView.findViewById(R.id.tv_sure);
        tv_cancel = dialogView.findViewById(R.id.tv_cancel);


        tv_sure.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);

        setContentView(dialogView);
        setCanceledOnTouchOutside(false);

        //内容设置
        update_hint.setText(String.format(context.getString(R.string.update_hint),
                versionName, newVersionName, description));
        update_hint.setMovementMethod(ScrollingMovementMethod
                .getInstance());
        update_progressbar.setMax(100);//设置最大100 newSize
        update_progressbar.setProgress(0);
        percent.setText(String.format("%.2fM / %.2fM (%.2f%%)",
                update_progressbar.getProgress() / 1024f / 1024f,
                update_progressbar.getMax() / 1024f / 1024f,
                100f * update_progressbar.getProgress() / update_progressbar.getMax()));
    }

    public void setButtonClick(boolean b) {
        tv_sure.setClickable(b);
        tv_cancel.setClickable(b);
    }

    public void setProgress(int progress) {
        update_progressbar.setProgress(progress);
        percent.setText(progress + "%");//显示
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
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_sure:
                callBack.sureBack();
                break;
            case R.id.tv_cancel:
                callBack.cancelBack();
                break;
            default:
                break;
        }
    }

}
