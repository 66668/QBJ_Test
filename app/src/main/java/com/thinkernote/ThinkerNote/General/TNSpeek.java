package com.thinkernote.ThinkerNote.General;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.thinkernote.ThinkerNote.Utils.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 讯飞 语音转文字封装
 * <p>
 * 自定义
 */
public class TNSpeek {

    private static final String TAG = "TNSpeek";
    //======================讯飞语音设置======================
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private String mEngineType;  // 引擎类型
    private SharedPreferences mSharedPreferences;
    private SpeechRecognizer mIat; // 语音听写对象
    int ret = 0; //不显示弹窗的参数返回
    private boolean mTranslateEnable = false;//是否要转换
    private Toast mToast;
    private SpeekCallBack callBack;
    private Context context;
    private boolean isStartSpeek;//是否开始、重新开始 说话


    public void setCallBack(SpeekCallBack callBack) {
        this.callBack = callBack;
    }

    public interface SpeekCallBack {
        //处理完，回调
        void onShowError();

        void onShowSpeeking();

        void onShowRestart();

        void onShowImgChanged(int count);

        void onResultBack(StringBuffer builder);

    }


    public TNSpeek(Context context) {
        this.context = context;
        initSpeek();
    }

    /**
     * 初始化 讯飞语音对象
     */
    private void initSpeek() {
        //初始化弹窗
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        // 引擎类型
        mEngineType = SpeechConstant.TYPE_CLOUD;//连接网络类型
        //
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(context, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    showTip("初始化失败，错误码：" + code);
                    if (callBack != null)
                        callBack.onShowError();
                }
            }
        });
        mSharedPreferences = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);

        //初始化完成后， 设置参数
        setParam();
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎类型 --连接网络类型
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        //字音转换
        this.mTranslateEnable = mSharedPreferences.getBoolean("translate", false);
        String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
        if (mTranslateEnable) {
            mIat.setParameter(SpeechConstant.ASR_SCH, "1");
            mIat.setParameter(SpeechConstant.ADD_CAP, "translate");
            mIat.setParameter(SpeechConstant.TRS_SRC, "its");
        }
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mIat.setParameter(SpeechConstant.ACCENT, null);

            if (mTranslateEnable) {
                mIat.setParameter(SpeechConstant.ORI_LANG, "en");
                mIat.setParameter(SpeechConstant.TRANS_LANG, "cn");
            }
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);

            if (mTranslateEnable) {
                mIat.setParameter(SpeechConstant.ORI_LANG, "cn");
                mIat.setParameter(SpeechConstant.TRANS_LANG, "en");
            }
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
//        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "6000"));
        mIat.setParameter(SpeechConstant.VAD_BOS, "10000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
//        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "3500"));
        mIat.setParameter(SpeechConstant.VAD_EOS, "10000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
//        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
//        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
//                Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }


    //========================外部控制============================

    /**
     * 运行 语音
     */
    public void speekStart() {
        //清空
        mIatResults.clear();
        isStartSpeek = true;
        if (callBack != null)
            callBack.onShowSpeeking();


        // 不显示听写对话框
        ret = mIat.startListening(new RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {
                //音量大小(0--13级),可以控制图片变化，做一些动画效果
                if (isStartSpeek) {
                    if (callBack != null)
                        callBack.onShowImgChanged(i);
                } else {
                    if (callBack != null)
                        callBack.onShowImgChanged(2);
                }

            }

            @Override
            public void onBeginOfSpeech() {
                if (callBack != null)
                    callBack.onShowSpeeking();
            }

            @Override
            public void onEndOfSpeech() {
                if (callBack != null)
                    callBack.onShowRestart();
                isStartSpeek = false;
            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                isStartSpeek = false;
                if (callBack != null) {
                    callBack.onResultBack(printResult(recognizerResult));
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                isStartSpeek = false;
                // Tips：
                // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
                // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
                if (mTranslateEnable && speechError.getErrorCode() == 14002) {
                    showTip(speechError.getPlainDescription(true) + "\n请确认是否已开通翻译功能");
                } else {
                    showTip(speechError.getPlainDescription(true));
                }
                if (callBack != null)
                    callBack.onShowRestart();
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {
                if (SpeechEvent.EVENT_RECORD_STOP == i) {//语音结束

                }
            }
        });

        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret);
        }

    }

    public void speekEnd() {

    }

    private StringBuffer printResult(RecognizerResult results) {
        mIatResults.clear();
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        return resultBuffer;
    }


    //========================似有方法============================

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

}
