package com.thinkernote.ThinkerNote.Activity;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.FileUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsAtt;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsSkin;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsUi;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * 主页--写笔记界面--添加附件
 * sjy 0625
 */
public class TNFileListAct extends TNActBase
        implements OnClickListener, OnItemClickListener {

    private String mPath = "/";
    private Vector<File> mFiles;
    private TNFileAdapter mFileAdapter;

    // Activity methods
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filelist);
        setViews();

        // initialize
        findViewById(R.id.filelist_back).setOnClickListener(this);

        mFiles = new Vector<File>();

        ListView lv = (ListView) findViewById(R.id.filelist_list);
        lv.setOnItemClickListener(this);
        mFileAdapter = new TNFileAdapter();
        lv.setAdapter(mFileAdapter);

        if (TNUtilsAtt.hasExternalStorage()) {
            mPath = FileUtils.DEFAULT_BASE_PATH;
        }
    }

    protected void setViews() {
        TNUtilsSkin.setViewBackground(this, null, R.id.filelist_toolbar_layout, R.drawable.toolbg);
        TNUtilsSkin.setViewBackground(this, null, R.id.filelist_page, R.drawable.page_bg);
    }

    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        outBundle.putString("PATH", mPath);
        super.onSaveInstanceState(outBundle);
    }

    @Override
    public void onRestoreInstanceState(Bundle outBundle) {
        super.onRestoreInstanceState(outBundle);

        mPath = outBundle.getString("PATH");
    }

    // ConfigView
    //-------------------------------------------------------------------------------
    protected void configView() {
        getFileDir(mPath);
        ((TextView) findViewById(R.id.filelist_path)).setText(mPath);
        mFileAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (!mPath.equals("/")) {
                mPath = new File(mPath).getParent();
                configView();
            } else {
                setResult(Activity.RESULT_CANCELED, null);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // implements OnClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filelist_back:
                setResult(Activity.RESULT_CANCELED, null);
                finish();
                break;
        }
    }

    // implements OnItemClickListener
    //-------------------------------------------------------------------------------
    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        File file = mFiles.get(position);
        if (file.getName().equals("..")) {
            mPath = file.getParent();
            configView();
        } else if (file.isDirectory()) {
            mPath = file.getPath();
            configView();
        } else if (file.isFile()) {
            if (!file.canRead()) {
                TNUtilsUi.alert(this, R.string.alert_file_cantRead_system);
                return;
            }
            Intent it = new Intent();
            it.putExtra("SelectedFile", file.getPath());
            setResult(Activity.RESULT_OK, it);
            finish();
        }
    }

    // Private methods
    //-------------------------------------------------------------------------------
    private void getFileDir(String filePath) {
        File f = new File(filePath);
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                if (file.isHidden())
                    return false;
                return true;
            }
        };

        mFiles.clear();
        if (!filePath.equals("/")) {
            mFiles.add(new File(f.getParent(), ".."));
        }

        File[] subFiles = f.listFiles(filter);// 列出文件
        if (subFiles != null) {

            Arrays.sort(subFiles, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    return o1.getName().toLowerCase().compareTo(
                            o2.getName().toLowerCase());
                }
            });

            for (File file : subFiles) {
                if (file.isDirectory())
                    mFiles.add(file);
            }
            for (File file : subFiles) {
                if (file.isFile())
                    mFiles.add(file);
            }
        }
    }

    // class TNCatAdapter
    //-------------------------------------------------------------------------------
    private class TNFileAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return mFiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.filelistitem, null);
            }

            setView(convertView, position);
            return convertView;
        }

        private void setView(View layout, int position) {
            File file = (File) getItem(position);
            ((TextView) layout.findViewById(R.id.filelistitem_name)).setText(
                    file.getName());
            if (!file.isDirectory()) {
                ImageView v = ((ImageView) layout.findViewById(R.id.filelistitem_icon));
//				v.setImageResource(R.drawable.file);
                TNUtilsSkin.setImageViewDrawable(TNFileListAct.this, v, R.drawable.file);
                ((TextView) layout.findViewById(R.id.filelistitem_size)).setText(
                        (file.length() * 100 / 1024) / 100f + " K");
            } else {
                ImageView v = ((ImageView) layout.findViewById(R.id.filelistitem_icon));
//				v.setImageResource(R.drawable.folder);
                TNUtilsSkin.setImageViewDrawable(TNFileListAct.this, v, R.drawable.folder);
                ((TextView) layout.findViewById(R.id.filelistitem_size)).setText("");
            }
        }
    }

}
