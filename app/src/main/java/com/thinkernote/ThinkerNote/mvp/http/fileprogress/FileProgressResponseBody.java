package com.thinkernote.ThinkerNote.mvp.http.fileprogress;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.thinkernote.ThinkerNote.utils.MLog;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 文件上传下载 查看进度 --自定义请求体
 */
public class FileProgressResponseBody extends ResponseBody {
    public static final int UPDATE = 0x01;

    private ResponseBody responseBody;//请求体

    FileProgressListener listener;//下载进度回调

    // BufferedSource 是okio库中的输入流，这里就当作inputStream来使用。
    private BufferedSource bufferedSource;

    private Handler myHandler;

    //
    public FileProgressResponseBody(ResponseBody responseBody, FileProgressListener listener) {
        this.responseBody = responseBody;
        this.listener = listener;
        if (myHandler == null) {
            myHandler = new MyHandler();
        }
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    //Okio.Source 核心方法
    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                MLog.e("FileProgressResponseBody", "read: " + (int) (totalBytesRead * 100 / responseBody.contentLength()));
                if (null != listener) {
                    if (bytesRead != -1) {
                        int progress = (int) (totalBytesRead * 100 / responseBody.contentLength());
                        //发送消息到主线程
                        Message msg = Message.obtain();
                        msg.what = UPDATE;
                        msg.obj = progress;
                        myHandler.sendMessage(msg);
                    }

                }
                return bytesRead;
            }
        };

    }

    /**
     * 将进度放到主线程中显示
     */
    class MyHandler extends Handler {

        public MyHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE:
                    listener.onFileProgressing((Integer) msg.obj);
                    break;

            }
        }
    }
}
