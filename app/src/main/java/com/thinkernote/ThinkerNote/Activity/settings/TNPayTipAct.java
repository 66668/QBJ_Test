package com.thinkernote.ThinkerNote.Activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.thinkernote.ThinkerNote.base.TNConst;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsSkin;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.utils.PayResult;
import com.thinkernote.ThinkerNote.mvp.p.PayPresenter;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnPayListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.AlipayBean;
import com.thinkernote.ThinkerNote.bean.main.WxpayBean;

import org.json.JSONObject;

import java.util.Map;

/**
 * 设置--打赏（支付宝） sjy 0615
 */
public class TNPayTipAct extends TNActBase implements OnClickListener, android.widget.CompoundButton.OnCheckedChangeListener
        , OnPayListener {
    private TextView mBack;
    private EditText mEditText;
    private Button mRButton1, mRButton2, mRButton3, mRButton4;
    private CheckBox mCheckZ, mCheckW;
    private Button mConfirmBtn;

    private String mAmount = "1";
    private String mType = "alipay";

    private IWXAPI api;

    private static final int SDK_PAY_FLAG = 1;
    /**
     * 支付宝支付业务：入参app_id
     */
    public static final String APPID = "2016080401702122";

    /**
     * 支付宝账户登录授权业务：入参pid值
     */
    public static final String PID = "";
    /**
     * 支付宝账户登录授权业务：入参target_id值
     */
    public static final String TARGET_ID = "";

    private String info = "";

    //p
    private PayPresenter presener;
    private AlipayBean alipayBean;
    private WxpayBean wxpayBean;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        Toast.makeText(getApplicationContext(), "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        Toast.makeText(getApplicationContext(), "支付失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_tip);
        api = WXAPIFactory.createWXAPI(this, TNConst.WX_APP_ID);

        presener = new PayPresenter( this);
        setViews();
        initView();
        initListener();
    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.regist_toolbg_framelayout, R.drawable.toolbg);
    }

    private void initView() {
        mBack = (TextView) findViewById(R.id.pay_back);
        mEditText = (EditText) findViewById(R.id.pay_num);
        mRButton1 = (Button) findViewById(R.id.pay_rb1);
        mRButton2 = (Button) findViewById(R.id.pay_rb2);
        mRButton3 = (Button) findViewById(R.id.pay_rb3);
        mRButton4 = (Button) findViewById(R.id.pay_rb4);
        mCheckZ = (CheckBox) findViewById(R.id.check_zfb);
        mCheckW = (CheckBox) findViewById(R.id.check_wx);
        mConfirmBtn = (Button) findViewById(R.id.pay_confirm);
        mCheckZ.setChecked(true);
    }

    private void initListener() {
        mBack.setOnClickListener(this);
        mCheckZ.setOnCheckedChangeListener(this);
        mCheckW.setOnCheckedChangeListener(this);
        mConfirmBtn.setOnClickListener(this);
        mRButton1.setOnClickListener(this);
        mRButton2.setOnClickListener(this);
        mRButton3.setOnClickListener(this);
        mRButton4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pay_back:
                finish();
                break;
            case R.id.pay_confirm:
                preparePay();
                break;
            case R.id.pay_rb1:
                mEditText.setText("1");
                break;
            case R.id.pay_rb2:
                mEditText.setText("2");
                break;
            case R.id.pay_rb3:
                mEditText.setText("5");
                break;
            case R.id.pay_rb4:
                mEditText.setText("10");
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        if (v == mCheckZ && isChecked) {
            mType = "alipay";
            mCheckZ.setChecked(true);
            mCheckW.setChecked(false);
        } else if (v == mCheckW && isChecked) {
            mType = "weixin";
            mCheckW.setChecked(true);
            mCheckZ.setChecked(false);
        } else {
            mType = "";
        }
    }

    @Override
    protected void configView() {
        super.configView();
    }

    private void preparePay() {
        mAmount = mEditText.getText().toString().trim();

        if ("alipay".equals(mType)) {
            payTip(mAmount, mType);
        } else {
            Toast.makeText(this, "请选择支付方式", Toast.LENGTH_SHORT).show();
        }


    }

    private void payAli(AlipayBean bean) {

        info = bean.getSigned_str();

        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(TNPayTipAct.this);
                Map<String, String> result = alipay.payV2(info, true);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    private void payWx(JSONObject json) {

        if (null != json && !json.has("retcode")) {

            PayReq req = new PayReq();
            req.appId = (String) TNUtils.getFromJSON(json, "appid");
            req.partnerId = (String) TNUtils.getFromJSON(json, "partnerid");
            req.prepayId = (String) TNUtils.getFromJSON(json, "prepayid");
            req.nonceStr = (String) TNUtils.getFromJSON(json, "noncestr");
            req.timeStamp = (String) TNUtils.getFromJSON(json, "timestamp");
            req.packageValue = (String) TNUtils.getFromJSON(json, "package");
            req.sign = (String) TNUtils.getFromJSON(json, "sign");
            req.extData = "app data"; // optional
            Toast.makeText(TNPayTipAct.this, "正常调起支付", Toast.LENGTH_SHORT).show();
            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
            api.sendReq(req);
        } else {
            MLog.d("PAY_GET", "返回错误" + TNUtils.getFromJSON(json, "msg"));
            Toast.makeText(TNPayTipAct.this, "返回错误" + TNUtils.getFromJSON(json, "msg"), Toast.LENGTH_SHORT).show();
        }
    }

    private void payWx(WxpayBean bean) {

        PayReq req = new PayReq();
        req.appId = bean.getAppid();
        req.partnerId = bean.getPartnerid();
        req.prepayId = bean.getPrepayid();
        req.nonceStr = bean.getNoncestr();
        req.timeStamp = bean.getTimestamp();
        req.packageValue = bean.getPackage();
        req.sign = bean.getSign();
        req.extData = "app data"; // optional
        Toast.makeText(TNPayTipAct.this, "正常调起支付", Toast.LENGTH_SHORT).show();
        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
        api.sendReq(req);
    }


    //---------------------------------p层调用-------------------------------------------

    private void payTip(String mAmount, String mType) {
        if (mType.equals("alipay")) {
            presener.pAlipay(mAmount, mType);
        } else {
            presener.pWxpay(mAmount, mType);
        }
    }

    //---------------------------------接口结果回调-------------------------------------------

    @Override
    public void onAlipaySuccess(Object obj) {
        alipayBean = (AlipayBean) obj;
        payAli(alipayBean);
    }

    @Override
    public void onAlipayFailed(String msg, Exception e) {
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onWxpaySuccess(Object obj) {
        wxpayBean = (WxpayBean) obj;
        payWx(wxpayBean);
    }

    @Override
    public void onWxpayFailed(String msg, Exception e) {
        Toast.makeText(TNPayTipAct.this, "返回错误" + msg, Toast.LENGTH_SHORT).show();

    }
}
