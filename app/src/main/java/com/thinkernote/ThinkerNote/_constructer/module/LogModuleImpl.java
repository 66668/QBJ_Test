package com.thinkernote.ThinkerNote._constructer.module;

import android.content.Context;

import com.google.gson.Gson;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote._interface.m.ILogModule;
import com.thinkernote.ThinkerNote._interface.v.OnLogListener;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.login.QQBean;
import com.thinkernote.ThinkerNote.http.MyHttpService;

import java.io.IOException;

import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 登录 m层 具体实现
 */
public class LogModuleImpl implements ILogModule {

    private Context context;
    private static final String TAG = "SJY";

    public LogModuleImpl(Context context) {
        this.context = context;
    }


    @Override
    public void loginNomal(final OnLogListener listener, String name, String ps) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .loginNormal(name, ps)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<LoginBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onLoginNormalFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(LoginBean bean) {
                        MLog.d(TAG, "登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "登录-成功");
                            listener.onLoginNormalSuccess(bean);
                        } else {
                            listener.onLoginNormalFailed(bean.getMessage(), null);
                        }
                    }

                });
    }

    /**
     * qq的返回不是标准的json串:所以获取ResponseBody的原始json，自定义解析
     * <p>
     * 正确返回：callback( {"client_id":"101399197","openid":"5B0CB916D4A9BDB5D838A3F66AC0B684","unionid":"UID_CAE5B3A01604A9F7B709D3BF934E7AA4"} );
     * 错误返回：callback({"error":100016,"error_description":"access token check failed"});
     *
     * @param listener
     * @param url
     * @param accessToken
     * @param refreshToken
     */
    @Override
    public void mGetQQUnionId(final OnLogListener listener, String url, final String accessToken, final String refreshToken) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getQQUnionID(url)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onQQUnionIdFailed("获取qq unionid异常", null);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            //拿到原始json,需要删除外层 callback();拿到 {"client_id":"101399197","openid":"5B0CB916D4A9BDB5D838A3F66AC0B684","unionid":"UID_CAE5B3A01604A9F7B709D3BF934E7AA4"}
                            String jsonStr = new String(responseBody.bytes());
                            //拿到标准json
                            String jsonData = jsonStr.substring(jsonStr.indexOf('(')+1, jsonStr.lastIndexOf(')'));
                            MLog.d("qq返回---jsonStr:" + jsonStr + "\njsonData:" + jsonData);
                            if (jsonData.contains("unionid")) {
                                //再使用Retrofit自带的JSON解析（或者别的什么）
                                QQBean bean = new Gson().fromJson(jsonData, QQBean.class);
                                listener.onQQUnionIdSuccess(bean, accessToken, refreshToken);
                            } else {
                                listener.onQQUnionIdFailed("获取qq unionid失败", null);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    @Override
    public void loginThird(final OnLogListener listener, final int btype, final String bid, final long stamp, String sign, final String accessToken, final String refreshToken, final String name) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .loginThird(btype, bid, stamp, sign, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<LoginBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onLoginThirdFailed("异常", new Exception("接口异常！"), bid, btype, stamp, accessToken, refreshToken, name);
                    }

                    @Override
                    public void onNext(LoginBean bean) {
                        MLog.d(TAG, "登录-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "登录-成功");
                            listener.onLoginThirdSuccess(bean);
                        } else {
                            listener.onLoginThirdFailed(bean.getMessage(), null, bid, btype, stamp, accessToken, refreshToken, name);
                        }
                    }

                });
    }

    @Override
    public void mProfile(final OnLogListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .LogNormalProfile(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean2<ProfileBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mProfile--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "mProfile--异常onError:" + e.toString());
                        listener.onLogProfileFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onNext(CommonBean2<ProfileBean> bean) {
                        MLog.d(TAG, "mProfile-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d(TAG, "mProfile-成功");
                            listener.onLogProfileSuccess(bean.getProfile());
                        } else {
                            listener.onLogProfileFailed(bean.getMsg(), null);
                        }
                    }

                });
    }

}
