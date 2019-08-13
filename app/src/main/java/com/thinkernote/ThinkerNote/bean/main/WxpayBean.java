package com.thinkernote.ThinkerNote.bean.main;

import com.google.gson.annotations.SerializedName;
import com.thinkernote.ThinkerNote.bean.CommonBean;

import java.io.Serializable;

/**
 * 支付结果返回
 * 格式{code msg data {}}
 */
public class WxpayBean extends CommonBean implements Serializable {
    String appid;
    String partnerid;
    String prepayid;
    String noncestr;
    String timestamp;

    @SerializedName("package")
    String Package;
    String   sign;

    public WxpayBean(int code, String msg) {
        super(code, msg);
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPartnerid() {
        return partnerid;
    }

    public void setPartnerid(String partnerid) {
        this.partnerid = partnerid;
    }

    public String getPrepayid() {
        return prepayid;
    }

    public void setPrepayid(String prepayid) {
        this.prepayid = prepayid;
    }

    public String getNoncestr() {
        return noncestr;
    }

    public void setNoncestr(String noncestr) {
        this.noncestr = noncestr;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPackage() {
        return Package;
    }

    public void setPackage(String aPackage) {
        Package = aPackage;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "WxpayBean{" +
                "appid='" + appid + '\'' +
                ", partnerid='" + partnerid + '\'' +
                ", prepayid='" + prepayid + '\'' +
                ", noncestr='" + noncestr + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", Package='" + Package + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
