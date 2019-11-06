package com.thinkernote.ThinkerNote.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;

import com.baidu.mobstat.StatService;
import com.thinkernote.ThinkerNote.Activity.settings.TNLockAct;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.utils.TNActivityManager;
import com.thinkernote.ThinkerNote.views.dialog.MenuDialog;

import java.lang.ref.WeakReference;
import java.util.Vector;

public class TNActBase extends Activity {
    private static final String kThinkerNotePackage = TNSettings.kThinkerNotePackage;
    private static final String kActivityPackage = TNSettings.kActivityPackage;

    protected final String TAG = getClass().getSimpleName();
    protected boolean isInFront;
    protected int createStatus; // 0 firstCreate, 1 resume, 2 reCreate
    private Vector<Dialog> dialogs;
    public MenuDialog.Builder mMenuBuilder;
    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TNActivityManager.getInstance().addActivity(this);
        handler = new WeakRefHandler(this);
        dialogs = new Vector<Dialog>();

        createStatus = (savedInstanceState == null) ? 0 : 2;
        //进入动画
        overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);

    }

    @Override
    protected void onStart() {
        super.onStart();
        TNSettings settings = TNSettings.getInstance();
        if (TAG.equals("TNMainAct")) {
            if (settings.isLogout) {
                finish();
                return;
            }
        } else if (TAG.equals("TNLoginAct") || TAG.equals("TNSplashAct")) {
            settings.isLogout = false;
        } else {
            if (settings.isLogout) {
                if (TAG.equals("TNNoteEditAct") && TNUtilsUi.isCallFromOutside(this)) {
                    settings.isLogout = false;
                }
                MLog.d("SJY", "TNActBase--onStart--finish");
                MLog.d("SJY", "关闭TNNoteEditAct");
                finish();
                return;
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        createStatus = 2;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    protected void onResume() {

        super.onResume();

        TNSettings settings = TNSettings.getInstance();

        //启动另一个activity时，如果在本activity尚未onStop时新起的activity被finish了,
        //这时重启本activity时不会调用onStart,而是直接调onResume
//		if(settings.isLogout && !TAG.equals("TNLoginAct")){
//			finish();
//			return;
//		}

        //百度
        StatService.onResume(this);

        isInFront = true;
        TNSettings.getInstance().topAct = this;

        // 直接安装包启动，再home退出，再进入，将分别产生2个task。一个task退出将导致另一个task出错。
        if (!TAG.equals("TNSplashAct") &&
                !TAG.equals("TNLoginAct") &&
                !TAG.equals("TNMainAct") &&
                settings.userId <= 0 &&
                !isFinishing() &&
                !TAG.equals("TNRegistAct") &&
                !TAG.equals("TNFindPasswordAct") &&
                !TAG.equals("TNNoteEditAct") && //appWidget适配
                !TAG.equals("TNBindAccountAct")) {
            MLog.d("SJY", "TNActBase--onResume--finish");
            finish();
            return;
        }

        configView();
        createStatus = 1;

        //锁屏判断机制
        if (!TAG.equals("TNLockAct") || !TAG.equals("TNSplashAct") && settings.lockPattern.size() > 0) {
            //登陆成功后，可以开启锁屏配置了
            TNSettings.getInstance().needShowLock_using = true;
        } else {
            //登陆成功后，可以开启锁屏配置了
            TNSettings.getInstance().needShowLock_using = false;
        }
        if (settings.needShowLock_launch && !TNSettings.getInstance().isLogout && !TAG.equals("TNLockAct")) {//重新开启软件的锁屏
            settings.needShowLock_launch = false;
            if (settings.lockPattern.size() > 0) {
                MLog.d("launch--锁");
                Bundle b = new Bundle();
                b.putInt("Type", 2);
                b.putString("OriginalPath", settings.lockPattern.toString());
                //解锁界面不可以使用singleTop模式,使用默认模式
                MLog.e(TAG, "show TNLockAct");
                Intent intent = new Intent(this, TNLockAct.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        } else if (!(TAG.equals("TNLoginAct")) && !TAG.equals("TNSplashAct") && !TAG.equals("TNLockAct")) {//软件不关闭的锁屏判断
            TNUtilsUi.checkLockScreen(this);
            if (settings.needShowLock && settings.needShowLock_using && !isFinishing() && !TNSettings.getInstance().isLogout) {
                if (settings.lockPattern.size() > 0) {//getTitle().equals("lock") &&
                    MLog.d("using--锁");
                    Bundle b = new Bundle();
                    b.putInt("Type", 2);
                    b.putString("OriginalPath", settings.lockPattern.toString());
                    //解锁界面不可以使用singleTop模式,使用默认模式
                    MLog.e(TAG, "show TNLockAct");
                    Intent intent = new Intent(this, TNLockAct.class);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            }
        }

    }

    protected void configView() {
    }

    protected void setViews() {
    }

    protected void onPause() {
        overridePendingTransition(R.anim.hold, R.anim.push_out_to_right);
        super.onPause();

        //百度
        StatService.onPause(this);

        isInFront = false;

        TNSettings settings = TNSettings.getInstance();
        if (settings.topAct == this)
            settings.topAct = null;


    }

    @Override
    public void onDestroy() {
        TNActivityManager.getInstance().deleteActivity(this);
        for (Dialog dialog : dialogs) {
            MLog.e(TAG, "dismiss:" + dialog + " showing:" + dialog.isShowing());
            dialog.dismiss();
        }
//		dialogs.clear();
        super.onDestroy();
    }

    //-------------------------------------------------------------------------------

    /**
     * 添加弹框    resource为布局文件
     *
     * @param resource
     */
    public View addMenu(int resource) {
        mMenuBuilder = new MenuDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        View view = inflater.inflate(resource, null);
        mMenuBuilder.setContentView(view);
        mMenuBuilder.create().show();
        return view;
    }

    public boolean runExtraIntent() {
        Intent it = getIntent();
        if (it.hasExtra(Intent.EXTRA_INTENT)) {
            startActivity(((Intent) it.getExtras().get(Intent.EXTRA_INTENT)));
            return true;
        }
        return false;
    }

    //===================================新版 跳转 --开始==========================================

    public void startToMain(Class clz) {
        Intent i = new Intent(this, clz);//推荐显示调用
        startActivity(i);
    }

    public void startActivity(Class clz) {
        Intent i = new Intent(this, clz);//推荐显示调用
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    /**
     * android5.0要求第一个参数是具体的类
     *
     * @param act
     * @param clz
     */
    public void startActivity(Activity act, Class clz) {
        Intent i = new Intent(act, clz);//推荐显示调用
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void startActivity(Class clz, Bundle aBundle) {
        Intent i = new Intent(this, clz);//推荐显示调用
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (aBundle != null) {
            i.putExtras(aBundle);
        }
        startActivity(i);

    }

    public void startToMain(Class clz, Bundle aBundle) {
        Intent i = new Intent(this, clz);//推荐显示调用
        if (aBundle != null) {
            i.putExtras(aBundle);
        }
        startActivity(i);

    }

    /**
     * android5.0要求第一个参数是具体的类
     *
     * @param act
     * @param clz
     */
    public void startActivity(Activity act, Class clz, Bundle aBundle) {
        Intent i = new Intent(act, clz);//推荐显示调用
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (aBundle != null) {
            i.putExtras(aBundle);
        }
        startActivity(i);

    }

    public void startActForResult(Class aActName, Bundle aBundle, int requestCode) {
        Intent i = new Intent(this, aActName);//推荐显示调用
        if (aBundle != null)
            i.putExtras(aBundle);
        startActivityForResult(i, requestCode);
    }
    //===================================新版 跳转 --结束==========================================


    //===================================handler弱引用 --开始==========================================


    public static class WeakRefHandler extends Handler {

        private final WeakReference<TNActBase> mBase;

        public WeakRefHandler(TNActBase activity) {
            mBase = new WeakReference<TNActBase>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final TNActBase activity = mBase.get();
            if (activity != null) {
                try {
                    activity.handleMessage(msg);
                } catch (Exception e) {
                    MLog.e("SJY", e.toString());
                }
            }
        }
    }

    /**
     * @param msg
     */
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            default:
                break;
        }
    }
    //===================================handler弱引用 --结束==========================================

}
