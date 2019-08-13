package com.thinkernote.ThinkerNote.mvp.http;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MyRxUtils {
    public static RequestBody toRequestBody(String value) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), value);
        return requestBody;
    }

}
