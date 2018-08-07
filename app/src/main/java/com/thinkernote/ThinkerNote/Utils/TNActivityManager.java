package com.thinkernote.ThinkerNote.Utils;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * act管理类 sjy 0620
 * 容易内存泄漏，本人不支持使用
 */

public class TNActivityManager {

    private List<Activity> activityList = new LinkedList<Activity>();
    private static TNActivityManager instance;

    private TNActivityManager() {
    }

    // 单例模式中获取唯一的MyApplication实例
    public static TNActivityManager getInstance() {
        if (null == instance) {
            instance = new TNActivityManager();
        }
        return instance;
    }

    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void finishOtherActivity(Activity activity) {
        for (int i = 0; i < activityList.size(); i++) {
            Activity act = activityList.get(i);
            if (act == activity && i == (activityList.size() - 1)) {
                continue;
            }
            act.finish();
        }
    }

    // 移除一个activity
    public void deleteActivity(Activity activity) {
        if (activityList != null && activityList.size() > 0) {
            if (activity != null) {
                activity.finish();
                activityList.remove(activity);
                activity = null;
            }

        }
    }

    // 遍历所有Activity并finish
    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        System.exit(0);
    }
}
