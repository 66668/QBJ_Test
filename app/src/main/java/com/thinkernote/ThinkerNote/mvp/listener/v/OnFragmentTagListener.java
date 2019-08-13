package com.thinkernote.ThinkerNote.mvp.listener.v;

public interface OnFragmentTagListener {
    //获取列表数据（非同步块回调）
    void onGetTagListSuccess();

    void onGetTagListFailed(String msg, Exception e);

}
