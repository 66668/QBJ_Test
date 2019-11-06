package com.thinkernote.ThinkerNote.views.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;

/**
 * 通用弹窗
 */

public class CommonDialog extends Dialog implements View.OnClickListener {

    private Context context;
    private DialogCallBack callBack;

    public CommonDialog(Context context, String content,DialogCallBack callBack) {
        super(context, R.style.dialogStyle);
        this.context = context;
        this.callBack = callBack;
        init(content, "确定", "取消");
    }

    public CommonDialog(Context context, String content, String sure, String cancel, DialogCallBack callBack) {
        super(context, R.style.dialogStyle);
        this.context = context;
        this.callBack = callBack;
        init(content, sure, cancel);
    }

    public CommonDialog(Context context, int content, DialogCallBack callBack) {
        super(context, R.style.dialogStyle);
        this.context = context;
        this.callBack = callBack;
        init(content, "确定", "取消");
    }

    public CommonDialog(Context context, int content, String sure, String cancel, DialogCallBack callBack) {
        super(context, R.style.dialogStyle);
        this.context = context;
        this.callBack = callBack;
        init(content,sure, cancel);
    }

    public interface DialogCallBack {

        void sureBack();

        void cancelBack();

    }

    private void init(int content,String sure, String cancel) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_common, null);

        TextView tv_sure = dialogView.findViewById(R.id.tv_sure);
        TextView tv_cancel = dialogView.findViewById(R.id.tv_cancel);
        TextView tv_content = dialogView.findViewById(R.id.content);

        tv_sure.setText(sure);
        tv_cancel.setText(cancel);
        tv_content.setText(content);


        tv_sure.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);

        setContentView(dialogView);
        setCanceledOnTouchOutside(false);
    }

    private void init(String content,String sure, String cancel) {

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_common, null);

        TextView tv_sure = dialogView.findViewById(R.id.tv_sure);
        TextView tv_cancel = dialogView.findViewById(R.id.tv_cancel);
        TextView tv_content = dialogView.findViewById(R.id.content);

        tv_sure.setText(sure);
        tv_cancel.setText(cancel);
        tv_content.setText(content);


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
                callBack.sureBack();
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
