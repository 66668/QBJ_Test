package com.thinkernote.ThinkerNote.mvp.m;

import android.content.Context;

import com.google.gson.Gson;
import com.thinkernote.ThinkerNote.base.TNConst;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.utils.SPUtil;
import com.thinkernote.ThinkerNote.bean.CommonBean;
import com.thinkernote.ThinkerNote.bean.CommonBean2;
import com.thinkernote.ThinkerNote.bean.login.LoginBean;
import com.thinkernote.ThinkerNote.bean.login.ProfileBean;
import com.thinkernote.ThinkerNote.bean.login.QQBean;
import com.thinkernote.ThinkerNote.bean.login.WchatInfoBean;
import com.thinkernote.ThinkerNote.bean.login.WchatTokenBean;
import com.thinkernote.ThinkerNote.mvp.http.url_main.MyHttpService;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnLogListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnUserinfoListener;
import com.thinkernote.ThinkerNote.mvp.listener.v.OnWchatListener;

import java.io.IOException;
import java.net.URLEncoder;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;


/**
 * 登录 m层 具体实现
 */
public class LogModel {

    private Context context;
    private static final String TAG = "SJY";

    public LogModel(Context context) {
        this.context = context;
    }


    public void loginNomal(final OnLogListener listener, String name, String ps) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .loginNormal(name, ps)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<LoginBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onLoginNormalFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

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
    public void mGetQQUnionId(final OnLogListener listener, String url, final String accessToken, final String refreshToken) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getQQUnionID(url)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onQQUnionIdFailed("获取qq unionid异常", null);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            //拿到原始json,需要删除外层 callback();拿到 {"client_id":"101399197","openid":"5B0CB916D4A9BDB5D838A3F66AC0B684","unionid":"UID_CAE5B3A01604A9F7B709D3BF934E7AA4"}
                            String jsonStr = new String(responseBody.bytes());
                            //拿到标准json
                            String jsonData = jsonStr.substring(jsonStr.indexOf('(') + 1, jsonStr.lastIndexOf(')'));
                            MLog.d("qq返回---jsonStr:" + jsonStr + "\njsonData:" + jsonData);
                            if (jsonData.contains("unionid")) {
                                //再使用Retrofit自带的JSON解析（或者别的json解析都可以）
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

    /**
     * 微信登陆（1）先获取token
     *
     * @param code
     * @param listener
     */
    public void getWchatToken(String code, final OnWchatListener listener) {
        /*
         * 将你前面得到的AppID、AppSecret、code，拼接成URL 获取access_token等等的信息(微信)
         */
        String url = getWchatCodeRequest(code);

        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getWchatToken(url)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onWchatFailed("获取微信token失败", null);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String jsonStr = new String(responseBody.bytes());
                            if (jsonStr.contains("access_token")) {
                                WchatTokenBean bean = new Gson().fromJson(jsonStr, WchatTokenBean.class);
                                String access_token = bean.getAccess_token();
                                String openid = bean.getOpenid();
                                String refresh_token = bean.getRefresh_token();
                                String get_user_info_url = getWchatUserInfo(access_token, openid);
                                //
                                getWchatInfo(get_user_info_url, access_token, refresh_token, listener);
                            } else {
                                listener.onWchatFailed("获取微信token失败", null);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            listener.onWchatFailed("获取微信token失败", null);
                        }
                    }
                });

    }

    /**
     * 微信登陆（2）获取info
     *
     * @param url
     * @param access_token
     * @param refresh_token
     * @param listener
     */
    public void getWchatInfo(String url, final String access_token, final String refresh_token, final OnWchatListener listener) {
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .getWchatInfo(url)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onWchatFailed("获取微信Info失败", null);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            String jsonStr = new String(responseBody.bytes());
                            if (jsonStr.contains("unionid")) {
                                WchatInfoBean bean = new Gson().fromJson(jsonStr, WchatInfoBean.class);
                                String unionid = bean.getUnionid();
                                String nickName = bean.getNickname();
                                //
                                //数据返回,登录界面处理,无法使用 intent值跳转
                                SPUtil.putString("unionid", unionid);
                                SPUtil.putString("access_token", access_token);
                                SPUtil.putString("refresh_token", refresh_token);
                                SPUtil.putString("nickName", nickName);
                                listener.onWchatSuccess();
                            } else {
                                listener.onWchatFailed("获取微信Info失败", null);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            listener.onWchatFailed("获取微信Info失败", null);
                        }
                    }
                });

    }

    public void loginThird(final OnLogListener listener, final int btype, final String bid, final long stamp, String sign, final String accessToken, final String refreshToken, final String name) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .loginThird(btype, bid, stamp, sign, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<LoginBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "登录--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "登录--登录失败异常onError:" + e.toString());
                        listener.onLoginThirdFailed("异常", new Exception("接口异常！"), bid, btype, stamp, accessToken, refreshToken, name);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

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

    public void mLogout(final OnUserinfoListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .logout(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d("验证码--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("验证码--异常onError:" + e.toString());
                        listener.onLogoutFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CommonBean bean) {
                        MLog.d("验证码-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            MLog.d("验证码-成功");
                            listener.onLogoutSuccess(bean);
                        } else {
                            listener.onLogoutFailed(bean.getMessage(), null);
                        }
                    }

                });
    }


    public void mProfile(final OnLogListener listener) {
        TNSettings settings = TNSettings.getInstance();
        MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .LogNormalProfile(settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<CommonBean2<ProfileBean>>() {//固定样式，可自定义其他处理
                    @Override
                    public void onComplete() {
                        MLog.d(TAG, "mProfile--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e(TAG, "mProfile--异常onError:" + e.toString());
                        listener.onLogProfileFailed("异常", new Exception("接口异常！"));
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

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

    /**
     * 微信登陆相关处理
     */
    private String GetCodeRequest = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";

    private String GetUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID";

    private String getWchatCodeRequest(String code) {
        String result = null;
        GetCodeRequest = GetCodeRequest.replace("APPID",
                urlEnodeUTF8(TNConst.WX_APP_ID));
        GetCodeRequest = GetCodeRequest.replace("SECRET",
                urlEnodeUTF8(TNConst.WX_APP_SECRET));
        GetCodeRequest = GetCodeRequest.replace("CODE", urlEnodeUTF8(code));
        result = GetCodeRequest;
        return result;
    }

    private String getWchatUserInfo(String access_token, String openid) {
        String result = null;
        GetUserInfo = GetUserInfo.replace("ACCESS_TOKEN",
                urlEnodeUTF8(access_token));
        GetUserInfo = GetUserInfo.replace("OPENID", urlEnodeUTF8(openid));
        result = GetUserInfo;
        return result;
    }

    private String urlEnodeUTF8(String str) {
        String result = str;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
