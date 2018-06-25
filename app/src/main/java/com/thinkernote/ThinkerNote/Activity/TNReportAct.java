package com.thinkernote.ThinkerNote.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.thinkernote.ThinkerNote.Adapter.PlusPhotoAdapter;
import com.thinkernote.ThinkerNote.Adapter.PlusPhotoAdapter.OnClickedListener;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.KeyBoardManager;
import com.thinkernote.ThinkerNote.Utils.UiUtils;
import com.thinkernote.ThinkerNote.Views.MyGridView;
import com.thinkernote.ThinkerNote._constructer.presenter.ReportPresenterImpl;
import com.thinkernote.ThinkerNote._interface.p.IReportPresener;
import com.thinkernote.ThinkerNote._interface.v.OnReportListener;
import com.thinkernote.ThinkerNote.base.TNActBase;
import com.thinkernote.ThinkerNote.bean.main.OldNotePicBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 设置--关于我们--反馈
 * 只支持上传一张图片
 */
public class TNReportAct extends TNActBase
        implements OnClickListener, OnItemClickListener, OnReportListener {
    //	private static final int CAMERA_CODE = 1001;
    private static final int PHOTO_CODE = 1002;


    private EditText mEmailView;
    private MyGridView mGridView;
    private PlusPhotoAdapter mPhotoAdapter;
    private ArrayList<String> mFiles = new ArrayList<String>();
    private ProgressDialog mProgressDialog = null;
//	private String mPhotoPath;
//	private Uri mOutUri;

    //p
    IReportPresener presener;


    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);
        mProgressDialog = TNUtilsUi.progressDialog(this, R.string.in_progress);
        setViews();
        //
        presener = new ReportPresenterImpl(this, this);

        mEmailView = (EditText) findViewById(R.id.report_email);
        if (!TextUtils.isEmpty(TNSettings.getInstance().email)) {
            mEmailView.setText(TNSettings.getInstance().email);
        }

        findViewById(R.id.report_home).setOnClickListener(this);
        findViewById(R.id.report_save).setOnClickListener(this);

        mGridView = (MyGridView) findViewById(R.id.photo_grid);
        mGridView.setOnItemClickListener(this);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mPhotoAdapter = new PlusPhotoAdapter(this, new DeletePhotoClickListener());
        mGridView.setAdapter(mPhotoAdapter);

    }

    @Override
    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.report_toolbar_layout, R.drawable.toolbg);
    }

    protected void configView() {
    }

    // Implement OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report_home:
                View view = getWindow().peekDecorView();
                if (view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                finish();
                break;

            case R.id.report_save:
                String content = ((EditText) findViewById(R.id.report_content)).getText().toString();
                if (content.length() <= 5) {
                    TNUtilsUi.showToast(R.string.feedback_content_short);
                    break;
                }
                String email = mEmailView.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    TNUtilsUi.showToast("请输入邮箱");
                    break;
                }
                if (!TNUtils.checkRegex(TNUtils.FULL_EMAIL_REGEX, email)) {
                    TNUtilsUi.showToast("邮箱格式不正确");
                    break;
                }
                if (!TNUtils.isNetWork()) {
                    TNUtilsUi.showToast("请确定网络连接是否正常");
                    break;
                }
                mProgressDialog.show();
                pfeedBack(content, mFiles, email);

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (position == mFiles.size()) {
            if (mFiles.size() >= 1) {
                Toast.makeText(getApplicationContext(), "最多只能上传1张图片", Toast.LENGTH_LONG).show();
                return;
            } else {
                KeyBoardManager.hideKeyboard(this, R.id.report_home);
//				mPhotoPath = AppUtils.getTmpPath("TN" + System.currentTimeMillis() + ".jpg");
//				if (!TextUtils.isEmpty(mPhotoPath)) {
//					File f = new File(mPhotoPath);
//					if (f.exists()) {
//						f.delete();
//					} else if (!f.getParentFile().exists()) {
//						f.getParentFile().mkdirs();
//					}
//				} else {
//					Toast.makeText(getApplicationContext(), "SD卡没有准备好,照片本地保存会失败", Toast.LENGTH_LONG).show();
//				}
//				mOutUri = Uri.parse("file://" + mPhotoPath);
//				UiUtils.openCamera(this, mOutUri, CAMERA_CODE);
                UiUtils.openPhoto(this, PHOTO_CODE);
            }
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("uri", mFiles.get(position));
            Intent intent = new Intent(this, ViewImageActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == PHOTO_CODE) {
            mFiles.add(compress(getPath(data.getData())));
        }
        mPhotoAdapter.update(mFiles);
        mPhotoAdapter.notifyDataSetChanged();
    }

    private String compress(String path) {
        try {
            File file = new File(path);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            bitmap.compress(CompressFormat.JPEG, 80, new FileOutputStream(path));
            return path;
        } catch (Exception e) {
            e.printStackTrace();
            return path;
        }

    }

    private String getPath(Uri uri) {
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = this.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else {
                return uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    class DeletePhotoClickListener implements OnClickedListener {

        @Override
        public void OnClicked(final int id) {
            mFiles.remove(id);
            mPhotoAdapter.update(mFiles);
            mPhotoAdapter.notifyDataSetChanged();
        }

    }
    //--------------------------------------p层调用------------------------------------------

    /**
     * 说明 反馈的接口调用方式，是先上传图片，返回一个ID，然后用id再绑定反馈内容
     * <p>
     * 先调图片接口，再顺序反馈接口
     */


    private void pfeedBack(String content, List<String> mFiles, String email) {
        if (mFiles.size() > 0) {
            presener.pFeedBackPic(mFiles, content, email);
        } else {
            presener.pFeedBack(content, -1L, email);
        }
    }

    //--------------------------------------接口结果回调------------------------------------------

    @Override
    public void onPicSuccess(Object obj, String content, String email) {
        OldNotePicBean picBean = (OldNotePicBean) obj;
        //拿到pid,上传内容
        presener.pFeedBack(content, -picBean.getId(), email);
    }

    @Override
    public void onPicFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }

    @Override
    public void onSubmitSuccess(Object obj) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(R.string.alert_Report_SaveMsg);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    @Override
    public void onSubmitFailed(String msg, Exception e) {
        mProgressDialog.hide();
        TNUtilsUi.showToast(msg);
    }

}
