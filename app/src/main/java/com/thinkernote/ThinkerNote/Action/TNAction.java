package com.thinkernote.ThinkerNote.Action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import junit.framework.Assert;

import android.os.AsyncTask;

import com.thinkernote.ThinkerNote.Utils.MLog;

/**
 * 可不删 sjy 反射封装 sjy 0726
 * <p>
 * 说明：
 * 更新前该类是act各种调用的反射集合类，用于反射调用各种方法（android版本升级后，异步AysncTask类已经不支持使用）
 * 更新后，所有反射调用 后台接口的方法都已删除，只剩下反射调用常用的方法，比如：数据库，监听等
 */
public class TNAction {
    private static final String TAG = "TNAction";

    public Object type;                // 任务标识
    public Vector<Object> inputs;    // 输入
    public Vector<Object> outputs;    // 输出
    public Object progressInfo;
    public TNActionResult result;    // 状态

    public TNAction parentAction;    // 父任务
    public TNAction childAction;    // 当前子任务

    private TNAsyncTask asyncTaskObj;        // 异步对象
    private boolean wantCancel;

    public enum TNActionResult {
        // 任务未开始
        NotStart    // 任务未开始

        //任务进行中
        , Working    // 任务进行中；执行子任务时，自动为该状态
        , Waitting    // 执行异步操作，等待任务完成

        // 任务完成
        , Finished    // 任务完成，结果存在output中
        , Failed        // 任务失败，结果存在output中
        , Cancelled    // 任务取消
    }

    //-------------------------------------------------------------------------------
    // construct
    public TNAction(Object aType, Object... aArray) {
        type = aType;

        inputs = new Vector<Object>();
        outputs = new Vector<Object>();
        result = TNActionResult.NotStart;
        for (Object obj : aArray) {
            inputs.add(obj);
        }

        parentAction = null;
        childAction = null;

        asyncTaskObj = null;
        wantCancel = false;
    }

    // 类方法

    /* 【注册Action执行函数】
     * 任务type是Object类型，即type可以是枚举类型、字符串、数字等，由应用决定。
     * 一个任务type只能注册一个执行函数。如先后注册相同的type，后者将覆盖前者。
     * 执行函数可以是对象函数、也可以是类函数。
     * 函数必须是public void method(TNAction aAction) 模式
     */
    public static void regRunner(Object aType, Object aObject, String aMethod) {
        TNActionCenter center = TNActionCenter.getInstance();
//		Assert.assertTrue("regRunner !center.runners.containsKey(aType)",
//				!center.runners.containsKey(aType));
        if (center.runners.containsKey(aType)) {
            center.runners.remove(aType);
        }
        center.runners.put(aType,
                new TNRunner(aObject, aMethod, TNAction.class));
    }

    /* 【注册Action完成后的响应函数】
     * 一个任务type可以注册多个响应函数。
     * 只有根任务执行完成后，才会调用响应函数。子任务执行，不会调用响应函数。
     * 即只有runRoot，runRootAsync函数执行完成后，才会调用响应函数。
     * 响应函数按注册顺序调用。响应函数总是在主线程执行。
     * 函数必须是public void method(TNAction aAction) 模式
     */
    public static void regResponder(Object aType, Object aObject, String aMethod) {
        TNActionCenter center = TNActionCenter.getInstance();
        if (!center.responders.containsKey(aType)) {
            center.responders.put(aType, new Vector<TNRunner>());
        }
        Vector<TNRunner> v = center.responders.get(aType);
        v.add(new TNRunner(aObject, aMethod, TNAction.class));
    }

    /* 【注销对象对action的执行和响应】
     * 通常在对象析构释放前进行注销。
     * 注销将同时注销执行者和响应者。
     */
    public static void unregister(Object aObject) {
        TNActionCenter center = TNActionCenter.getInstance();

        Enumeration<Object> enRunner = center.runners.keys();
        while (enRunner.hasMoreElements()) {
            Object key = enRunner.nextElement();
            TNRunner runner = center.runners.get(key);
            if (runner.checkTarget(aObject)) {
                center.runners.remove(key);
            }
        }

        Enumeration<Object> enRespond = center.responders.keys();
        while (enRespond.hasMoreElements()) {
            Object key = enRespond.nextElement();
            Vector<TNRunner> v = center.responders.get(key);
            for (Iterator<TNRunner> it = v.iterator(); it.hasNext(); ) {
                TNRunner runner = it.next();
                if (runner.checkTarget(aObject)) {
                    it.remove();
                }
            }
        }
    }

    /* 【执行根任务（非异步）】
     * 推荐仅在UI模块调用
     */
    public static TNAction runAction(Object aType, Object... aArray) {
        TNAction action = new TNAction(aType, aArray);

        // 执行任务
        try {
            runActionImp(action);
        } catch (TNActionException e) {
            // remove child action
            removeChildAction(action);

            action.result = e.result;
            action.outputs.clear();
            action.outputs.addAll(e.outputs);
        }

        // 从列表移除任务
        TNActionCenter center = TNActionCenter.getInstance();
        synchronized (center.actions) {
            center.actions.remove(action);
        }

        // 执行响应函数
        respondActionImp(action, null);

        return action;
    }

    /* 【执行跟任务（异步）】
     * 推荐仅在UI模块调用
     * 使用AsyncTask来实现子线程的异步调用
     */
    public static TNAction runActionAsync(Object aType, Object... aArray) {
        android.util.Log.d("SJY", "异步--runActionAsync");
        TNAction action = new TNAction(aType, aArray);
        action.asyncTaskObj = new TNAsyncTask();
        action.asyncTaskObj.execute(action);
        return action;
    }

    /* 【返回正在运行的Action列表】
     * 该列表返回的是复制列表
     * 应用可获取该列表，从而得知哪些任务正在运行。
     * 任务respond时，Action应已从该列表中移除。（跟原来不一样）
     */
    public static Vector<TNAction> runningList() {
        TNActionCenter center = TNActionCenter.getInstance();
        Vector<TNAction> temp = null;
        synchronized (center.actions) {
            temp = new Vector<TNAction>(center.actions);
        }
        return temp;
    }

    // 对象方法
    /* 【执行子任务（非异步）】
     * 执行子任务时，上次的子任务将从列表移除。
     */
    public TNAction runChildAction(Object aType, Object... aArray) {
        removeChildAction(this);

        TNAction action = new TNAction(aType, aArray);
        childAction = action;
        action.parentAction = this;

        runActionImp(action);

        return action;
    }

    // 为简单化，暂不支持异步子任务！！！！
    /* 【执行子任务（异步）】
     * 异步子任务不推荐使用。
     * 只有异步任务才可以执行异步子任务。（否则waitting将导致主线程阻塞）
     * 异步子任务是使用AysncTask实现的。
     * 执行子任务时，上次的子任务将从列表移除。
     * 调用该函数后，应马上调用aAction.waitting()，等待子任务完成。
     */
//	public TNAction runChildAsysnc(Object aType, Object ... aArray){
//		removeChildAction(this);
//		
//		TNAction action = new TNAction(aType, aArray);
//		childAction = action;
//		action.parentAction = this;
//		
//		action.asyncTaskObj = new TNAsyncTask();
//		status = TNActionResult.Waitting;
//		action.asyncTaskObj.execute(action);
//		
//		return action;
//	}

    /* 【任务完成】
     * 任务执行完毕必须调用finished或failed
     */
    public void finished(Object... aArray) {
//		Assert.assertTrue("finished status == TNActionResult.Working", 
//				result == TNActionResult.Working);
        result = TNActionResult.Finished;

        outputs.clear();
        for (Object obj : aArray) {
            outputs.add(obj);
        }
    }

    /* 【任务失败】
     * 任务执行完毕必须调用finished或failed
     */
    public void failed(Object... aArray) {
//		Assert.assertTrue("failed status == TNActionResult.Working", 
//				result == TNActionResult.Working);
//		result = TNActionResult.Failed;
//		
//		outputs.clear();
//		for(Object obj : aArray){
//			outputs.add(obj);
//		}
        throw new TNActionException(TNActionResult.Failed, aArray);
    }

    /* 【任务取消】
     * 只有异步任务，才可以取消。
     * 某任务取消将使根任务下所有任务都取消。
     * 任务取消不会立即进行，而是当下一次执行任务runAction时，让任务立即结束。
     */
    public void cancel() {
        TNAction root = rootAction();
        Assert.assertTrue("cancel root.asyncTaskObj != null",
                root.asyncTaskObj != null);
        if (root.asyncTaskObj != null) {
            root.wantCancel = true;
        }
    }

    /* 【任务等待】
     * 只有异步任务，才有任务等待。
     * 当调用异步函数后，需调用该函数，锁定线程。
     * 异步函数执行完毕后，需调用resume，改变status，从而线程解锁，继续执行。
     */
    public void waitting() {
        TNAction root = rootAction();
        Assert.assertTrue("waitting root.asyncTaskObj != null",
                root.asyncTaskObj != null);
        if (root.asyncTaskObj != null) {
            result = TNActionResult.Waitting;
            MLog.d(TAG, "(waitting....)" + toString());
            while (result == TNActionResult.Waitting) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Assert.assertTrue("failed status == TNActionResult.Working",
                    result == TNActionResult.Working);
            MLog.d(TAG, "(resume....)" + toString());
        }
    }

    /* 【任务继续执行】
     * 当异步任务完成后，在回调函数中，调用该函数，使得线程解锁，继续执行。
     */
    public void resume() {
        result = TNActionResult.Working;
    }

    /* 【任务进度更新】
     * 只有异步任务，才有进度更新
     * 调用该函数后，将调用根任务的响应函数
     */
    public void progressUpdate(Object aInfo) {
        Assert.assertTrue("progressUpdate status == TNActionResult.Working",
                result == TNActionResult.Working);
        TNAction root = rootAction();
        Assert.assertTrue("progressUpdate root.asyncTaskObj != null",
                root.asyncTaskObj != null);
        if (root.asyncTaskObj != null) {
//			Log.d(TAG,"(progress....)" + toString());
            root.asyncTaskObj.doProgress(aInfo);
        }
    }

    /* 【检查子任务是否完成】
     * 完成则返回true，如失败或取消，则设置status并复制outputs
     * 这样，调用该函数，如返回false，则可以立即return，任务结束。
     */
//	public boolean isChildFinished(){
//		Assert.assertTrue("isChildFinished childAction != null", childAction != null);
//		Assert.assertTrue("isChildFinished childAction.status != TNActionResult.NotStart", 
//				childAction.result != TNActionResult.NotStart);
//		Assert.assertTrue("isChildFinished childAction.status != TNActionResult.Working", 
//				childAction.result != TNActionResult.Working);
//		Assert.assertTrue("isChildFinished childAction.status != TNActionResult.Waitting", 
//				childAction.result != TNActionResult.Waitting);
//		
//		if(childAction != null){
//			if(childAction.result == TNActionResult.Finished){
//				return true;
//			}else if(childAction.result == TNActionResult.Failed
//					|| childAction.result == TNActionResult.Cancelled){
//				result = childAction.result;
//				outputs.clear();
//				outputs.addAll(childAction.outputs);				
//			}
//		}
//
//		return false;
//	}

    /* 【检查是否异步任务】
     *
     */
    public boolean isAsync() {
        TNAction root = rootAction();
        return root.asyncTaskObj != null;
    }

    /* 【返回Log字符串】
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append((parentAction == null) ? "" : parentAction.type.toString());
        sb.append("【" + type.toString() + "】");
        sb.append((childAction == null) ? "" : childAction.type.toString());
        sb.append("\t");
        sb.append(String.format("Type:%s Status:%s Input:%s Output:%s Progress:%s",
                type.toString(), result.toString(),
                inputs.toString(), outputs.toString(), progressInfo));
        return sb.toString();
    }

    //-------------------------------------------------------------------------------
    // 私有方法
    // 【执行任务】
    private static TNAction runActionImp(TNAction aAction) {
        // 加入actions列表
        TNActionCenter center = TNActionCenter.getInstance();
        Assert.assertTrue("runAction !center.actions.contains(aAction)",
                !center.actions.contains(aAction));
        if (!center.actions.contains(aAction)) {
            synchronized (center.actions) {
                center.actions.add(aAction);
            }
        }

        // cancel任务
        TNAction root = aAction.rootAction();
        if (root.wantCancel) {
//			aAction.result = TNActionResult.Cancelled;
//			Log.d(TAG,"($cancel....)" + aAction.toString());
            throw new TNActionException(TNActionResult.Cancelled);
        } else {
            // Log
            if (root.asyncTaskObj != null)
                MLog.d(TAG, "($run....)" + aAction.toString());
            else
                MLog.d(TAG, "(run....)" + aAction.toString());

            // 执行任务
            aAction.result = TNActionResult.Working;
            TNRunner runner = center.runners.get(aAction.type);
            Assert.assertTrue("runActionImp runner != null",
                    runner != null);
            if (runner != null) {
                runner.run(aAction);
            }
        }

        // remove child action
        removeChildAction(aAction);

        return aAction;
    }

    // Action完成后响应
    private static void respondActionImp(TNAction aAction, Object aProgress) {
        // Log
        MLog.d(TAG, "(respond)" + aAction.toString() + " 【Progress: " + aProgress + "】");

        // call respond method
        TNActionCenter center = TNActionCenter.getInstance();
        Vector<TNRunner> v = center.responders.get(aAction.type);
        if (v != null) {
            // 复制列表，防止循环中ConcurrentModificationException
            Vector<TNRunner> responders = new Vector<TNRunner>(v);
            for (TNRunner runner : responders) {
                aAction.progressInfo = aProgress;
                runner.run(aAction);
            }
        }
    }

    // 子任务从列表移除
    private static void removeChildAction(TNAction aAction) {
        TNActionCenter center = TNActionCenter.getInstance();
        if (aAction.childAction != null) {
            // 递归调用，清除子任务
            removeChildAction(aAction.childAction);

            // 从Actions列表清除任务
            synchronized (center.actions) {
                center.actions.remove(aAction.childAction);
            }
            aAction.childAction.parentAction = null;
            aAction.childAction = null;
        }
    }

    // 获取根任务
    private TNAction rootAction() {
        TNAction root = this;
        while (root.parentAction != null)
            root = root.parentAction;
        return root;
    }

    //-------------------------------------------------------------------------------
    // 内部类
    private static class TNActionCenter {
        private static TNActionCenter singleton = null;

        // 正在运行的Action列表
        public Vector<TNAction> actions;
        // 任务与运行者的对应关系
        public Hashtable<Object, TNRunner> runners;
        // 任务与响应者的对应关系
        public Hashtable<Object, Vector<TNRunner>> responders;

        //-------------------------------------------------------------------------------
        // Singleton
        private TNActionCenter() {
            actions = new Vector<TNAction>();
            runners = new Hashtable<Object, TNRunner>();
            responders = new Hashtable<Object, Vector<TNRunner>>();
        }

        public static TNActionCenter getInstance() {
            if (singleton == null) {
                synchronized (TNActionCenter.class) {
                    if (singleton == null) {
                        singleton = new TNActionCenter();
                    }
                }
            }
            return singleton;
        }
    }

    private static class TNAsyncTask extends AsyncTask<TNAction, Object, TNAction> {
        private static final String TAG = "TNAsyncTask";
        private TNAction mAction = null;

        @Override
        protected TNAction doInBackground(TNAction... params) {
            mAction = params[0];
            MLog.d("SJY", "doInBackground:" + mAction.type);
            try {
                TNAction.runActionImp(mAction);
            } catch (TNActionException e) {
                // remove child action
                removeChildAction(mAction);

                mAction.result = e.result;
                mAction.outputs.clear();
                mAction.outputs.addAll(e.outputs);
            }

            return mAction;
        }

        public void doProgress(Object info) {
            publishProgress(info);
        }

        @Override
        protected void onProgressUpdate(Object... progress) {
//			Log.i(TAG, "onProgressUpdate " + progress[0]);
            TNAction.respondActionImp(mAction, progress[0]);
            mAction.progressInfo = null;
        }

        @Override
        protected void onPostExecute(TNAction result) {
            MLog.d(TAG, "onPostExecute:" + mAction.type);

            TNActionCenter center = TNActionCenter.getInstance();
            synchronized (center.actions) {
                center.actions.remove(mAction);
            }

            mAction.asyncTaskObj = null;
            TNAction.respondActionImp(mAction, null);

            mAction = null;
        }
    }

    public static class TNActionException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public TNActionResult result;
        public Vector<Object> outputs;

        public TNActionException(TNActionResult aResult, Object... aArray) {
            super(aResult.toString());
            Assert.assertTrue("TNActionException aResult == TNActionResult.Failed || aResult == TNActionResult.Cancelled",
                    aResult == TNActionResult.Failed || aResult == TNActionResult.Cancelled);
            result = aResult;
            outputs = new Vector<Object>();
            for (Object obj : aArray) {
                outputs.add(obj);
            }
        }
    }

    public static class TNRunner {
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
         * 定义类函数
         */
//		public TNRunner(Class<?> aClass, String aName, Class<?> ... params){
//			mTarget = null;
//			
//			try {
//				mMethod = aClass.getMethod(aName, params);
//			}catch (NoSuchMethodException e) {
//				e.printStackTrace();
//			}
//		}

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
}
