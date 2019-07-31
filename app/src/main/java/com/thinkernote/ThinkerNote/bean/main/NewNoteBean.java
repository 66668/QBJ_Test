package com.thinkernote.ThinkerNote.bean.main;

import com.thinkernote.ThinkerNote.bean.CommonBean;

import java.io.Serializable;

/**
 *
 * 格式{code msg xx}
 */

public class NewNoteBean extends CommonBean implements Serializable {
    String md5;
    long id;

    public NewNoteBean(int code, String msg) {
        super(code, msg);
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "OldNotePicBean{" +
                "md5='" + md5 + '\'' +
                ", id=" + id +
                '}';
    }
}
