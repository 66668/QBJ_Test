package com.thinkernote.ThinkerNote.bean.login;

import com.thinkernote.ThinkerNote.bean.CommonBean;

import java.io.Serializable;

/**
 * 登录返回数据的bean
 */

public class LoginBean extends CommonBean implements Serializable {
    String username;
    long user_id;
    String token;
    long expire_at;

    public LoginBean(int code, String msg) {
        super(code, msg);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpire_at() {
        return expire_at;
    }

    public void setExpire_at(long expire_at) {
        this.expire_at = expire_at;
    }

    @Override
    public String toString() {
        return "LoginBean{" +
                "username='" + username + '\'' +
                ", user_id=" + user_id +
                ", token='" + token + '\'' +
                ", expire_at='" + expire_at + '\'' +
                '}' + "CommonBean{" +
                "code=" + getCode() +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
