package com.thinkernote.ThinkerNote.General;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射执行
 */
public class TNRunner {
    private Object mTarget;
    private Method mMethod;

    /*
     * 定义对象函数
     */
    public TNRunner(Object aTarget, String aName, Class<?>... params) {
        mTarget = aTarget;

        try {
            if (aTarget instanceof Class<?>) {
                mMethod = ((Class<?>) aTarget).getMethod(aName, params);
            } else {
                mMethod = aTarget.getClass().getMethod(aName, params);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /*
     * 运行函数（需确保传入的对象与定义类型一致）
     */
    public Object run(Object... objects) {
        Object ret = null;
        try {
            ret = mMethod.invoke(mTarget, objects);
        } catch (InvocationTargetException e) {
            Throwable ee = e;
            while (InvocationTargetException.class.isInstance(ee)) {
                ee = ((InvocationTargetException) ee).getTargetException();
            }
            ee.printStackTrace();
            if (RuntimeException.class.isInstance(ee)) {
                throw (RuntimeException) ee;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /*
     * 检查运行目标体
     */
    public boolean checkTarget(Object obj) {
        return mTarget == obj;
    }
}

