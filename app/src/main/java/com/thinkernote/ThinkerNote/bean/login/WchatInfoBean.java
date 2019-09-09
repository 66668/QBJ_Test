package com.thinkernote.ThinkerNote.bean.login;

import java.io.Serializable;

/**
 *
 */

public class WchatInfoBean implements Serializable {
    String openid;
    String unionid;
    String nickname;
    String sex;
    String language;
    String city;
    String province;
    String country;
    String headimgurl;

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
