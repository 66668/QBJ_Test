package com.thinkernote.ThinkerNote._constructer.listener.v;

public interface OnSyncListener {
    //大块同步，最终返回ui处理的回调
    void onSyncSuccess(String obj);

    void onSyncFailed(Exception e, String msg);

    //编辑界面才使用
    void onSyncEditSuccess();
}
