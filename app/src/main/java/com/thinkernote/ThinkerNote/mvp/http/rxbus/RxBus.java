package com.thinkernote.ThinkerNote.mvp.http.rxbus;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


/**
 * 使用Rxbus跳转
 */
public class RxBus {

    private static volatile RxBus mDefaultInstance;
    private Subject<Object> _bus;


    public static RxBus getInstance() {
        if (mDefaultInstance == null) {
            synchronized (RxBus.class) {
                if (mDefaultInstance == null) {
                    mDefaultInstance = new RxBus();
                }
            }
        }
        return mDefaultInstance;
    }

    private RxBus() {
        _bus = PublishSubject.create().toSerialized();
    }

    /**
     * 根据code进行分发
     *
     * @param code 事件code
     * @param o
     */
    public void post(int code, Object o) {
        _bus.onNext(new RxBusBaseMessage(code, o));

    }

    /**
     * 发送一个新的事件
     *
     * @param o
     */
    public void post(Object o) {
        _bus.onNext(o);
    }


    /**
     * 根据传递的code和 eventType 类型返回特定类型(eventType)的 被观察者
     * 对于注册了code为0，class为voidMessage的观察者，那么就接收不到code为0之外的voidMessage。
     *
     * @param code      事件code
     * @param eventType 事件类型
     * @param <T>
     * @return
     */
    public <T> Observable<T> toObservable(final int code, final Class<T> eventType) {
        return _bus.ofType(RxBusBaseMessage.class).filter(new Predicate<RxBusBaseMessage>() {
            @Override
            public boolean test(RxBusBaseMessage o) throws Exception {

                return o.getCode() == code && eventType.isInstance(o.getObject());

            }
        }).map(new Function<RxBusBaseMessage, Object>() {
            @Override
            public Object apply(RxBusBaseMessage o) throws Exception {
                return o.getObject();
            }
        }).cast(eventType);

    }

    public Observable<Object> toObservable() {
        return _bus;
    }

    /**
     * 判断是否有订阅者
     */
    public boolean hasObservers() {
        return _bus.hasObservers();
    }

    /**
     * 实战项目证明，Rxbus在退出应用的时，并没有释放，需要手动调用释放，否则调用的地方UI会不更新，引起bug（手机直接杀死应用不影响）
     *
     * @return
     */
    public boolean release() {
        if (mDefaultInstance != null) {
            mDefaultInstance = null;
        }
        return true;
    }

}
