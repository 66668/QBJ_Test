package com.thinkernote.ThinkerNote.appwidget43;

/**
 * app widget相关配置
 */
public class TNAppWidegtConst {

    public static final String SCHEME = "qbj_appwidget";//该值在manifest中设置，值保持一致
    //冒号之前的参数必须为SCHEME,保持一致,冒号后的参数随便设置，本人设置为包名
    public static final String SCHEME_HOST = SCHEME + "://" + "com.thinkernote.ThinkerNote";
    //item跳转的 key值设置
    public static final String SCHEME_ITEMKEY = "NoteLocalId";

}
