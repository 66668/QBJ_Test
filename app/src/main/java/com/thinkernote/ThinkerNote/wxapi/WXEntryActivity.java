package com.thinkernote.ThinkerNote.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.mvp.http.rxbus.RxBus;
import com.thinkernote.ThinkerNote.mvp.http.rxbus.RxBusBaseMessage;
import com.thinkernote.ThinkerNote.mvp.http.rxbus.RxCodeConstants;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnWchatListener;
import com.thinkernote.ThinkerNote.mvp.p.LogPresenter;

/**
 * 微信登录 设置
 */
public class WXEntryActivity extends TNActBase implements IWXAPIEventHandler, OnWchatListener {
    private static final int BACK_RESULT_CODE = 11;
    private IWXAPI api;
    private String WX_APP_ID = "wx2c2721939e9d54e3";
    LogPresenter presenter;

    // 获取第一步的code后，请求以下链接获取access_token
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new LogPresenter(this, this);
        api = WXAPIFactory.createWXAPI(this, WX_APP_ID, false);
        api.handleIntent(getIntent(), this);

    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        finish();
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        String result = "";
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (resp.transaction != null) {
                    finish();
                    Toast.makeText(this, "分享成功", Toast.LENGTH_LONG).show();
                } else {
                    String code = ((SendAuth.Resp) resp).code;
                    /*
                     * 将你前面得到的AppID、AppSecret、code，拼接成URL 获取access_token等等的信息(微信)
                     */
                    //
                    presenter.getWchatToken(code);
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "发送取消";
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "发送被拒绝";
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                finish();
                break;
            default:
                result = "发送返回";
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                finish();
                break;
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    //====================================微信登陆回调====================================

    @Override
    public void onWchatSuccess() {
        MLog.d("触发微信登录界面");
        //需要回调登录界面 RxBus
        RxBus.getInstance().post(RxCodeConstants.WEChat_BACK_LOG, new RxBusBaseMessage());
        WXEntryActivity.this.finish();
    }

    @Override
    public void onWchatFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
        WXEntryActivity.this.finish();
    }
}
