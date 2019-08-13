package com.thinkernote.ThinkerNote.mvp.http;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 设置文件格式（后台蛋疼）
 *
 * application/json
 *  text
 */
public class RequestBodyUtil {
    public static RequestBody getRequest(String filePath, File file) {
        RequestBody requestFile = null;
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith("png")) {
            requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        } else if (filePath.endsWith(".mp3") || filePath.endsWith(".wmv") || filePath.endsWith(".avi")) {//需要后台支持
            requestFile = RequestBody.create(MediaType.parse("music/*"), file);
        } else {
            requestFile = RequestBody.create(MediaType.parse("file/*"), file);
        }
        return requestFile;
    }
}
