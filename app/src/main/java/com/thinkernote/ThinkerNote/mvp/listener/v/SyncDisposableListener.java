package com.thinkernote.ThinkerNote.mvp.listener.v;

import io.reactivex.disposables.Disposable;

public interface SyncDisposableListener {
    void add(Disposable d);
}
