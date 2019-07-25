package com.thinkernote.ThinkerNote.Activity.unuse;

import java.io.File;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.thinkernote.ThinkerNote.Activity.TNFileListAct;
import com.thinkernote.ThinkerNote.BuildConfig;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Data.TNNoteAtt;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsDialog;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.General.TNUtilsUi;
import com.thinkernote.ThinkerNote.Utils.MLog;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 * TODO 未使用
 */
public class TNAttListAct extends TNActBase implements OnClickListener,
				OnItemClickListener, OnItemLongClickListener {

	private Vector<TNNoteAtt> mAttList;
	private AttListAdapter adapter ;
	private ListView mAttListView;
	private TNNoteAtt mCurAtt;
	private TNNote mNote;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.attlist);
		setViews();
		
		registerForContextMenu(findViewById(R.id.attlistitem_longclick_menu));
		
		mNote = (TNNote)getIntent().getSerializableExtra("AttList");
		mAttList = new Vector<TNNoteAtt>();
		mAttList.addAll(mNote.atts);
		adapter = new AttListAdapter();
		mAttListView = (ListView)findViewById(R.id.attlist_listview);
//		TNUtilsUi.addListHelpInfoFootView(this, mAttListView, R.string.listfootview_title_attlist, R.string.listfootview_info_attlist);
		
		mAttListView.setAdapter(adapter);
		mAttListView.setOnItemClickListener(this);
		mAttListView.setOnItemLongClickListener(this);
		
		findViewById(R.id.attlist_home_btn).setOnClickListener(this);
		findViewById(R.id.attlist_add_att).setOnClickListener(this);
	}

	@Override
	protected void configView() {
		adapter.notifyDataSetChanged();
	}
	
	public void setViews(){
		TNUtilsSkin.setViewBackground(this, null, R.id.attlist_toolbar_LinearLayout, R.drawable.toolbg);
		TNUtilsSkin.setViewBackground(this, null, R.id.attlist_layout, R.drawable.layout_bg_gradientcolors);
		TNUtilsSkin.setImageButtomDrawableAndStateBackground(this, null, R.id.attlist_add_att, R.drawable.addatt);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(mNote.atts.equals(mAttList)){
				setResult(Activity.RESULT_CANCELED);
			}else{
				mNote.atts.clear();
				mNote.atts.addAll(mAttList);
				mAttList.clear();
				Intent it = new Intent();
				it.putExtra("NewNote", mNote);
				setResult(Activity.RESULT_OK, it);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		switch (v.getId()) {
		case R.id.attlistitem_longclick_menu:
			getMenuInflater().inflate(R.menu.attedit_menu, menu);
//			menu.setHeaderTitle(R.string.alert_Title);
			break;
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.attedit_menu_delete:
			mAttList.remove(mCurAtt);
			mCurAtt = null;
			break;
		}
		configView();
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {
		mCurAtt = mAttList.get(position);
		openContextMenu(findViewById(R.id.attlistitem_longclick_menu));
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mCurAtt = mAttList.get(position);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		Uri contentUri = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//7.0+版本安全设置
			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".FileProvider", new File(mCurAtt.path));
		} else {//7.0-正常调用
			contentUri = Uri.fromFile(new File(mCurAtt.path));
		}

//                  intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
		intent.setDataAndType(contentUri, TNUtilsAtt.getMimeType(mCurAtt.type, mCurAtt.attName));
		TNUtilsDialog.startIntent(this, intent, R.string.alert_NoteView_CantOpenAttMsg);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.attlist_home_btn:{
			if(mNote.atts.equals(mAttList)){
				setResult(Activity.RESULT_CANCELED);
			}else{
				mNote.atts.clear();
				mNote.atts.addAll(mAttList);
				mAttList.clear();
				Intent it = new Intent();
				it.putExtra("NewNote", mNote);
				setResult(Activity.RESULT_OK, it);
				}
			finish();
			break;
			}
		case R.id.attlist_add_att:
			startActForResult(TNFileListAct.class, null, R.id.noteedit_addatt);//TODO
			break;
			
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		addAtt(data.getStringExtra("SelectedFile"));
		configView();
	}

	private void addAtt(String path){
		if(path == null){
			MLog.e(TAG, "addAtt path is NULL");
			return;
		}
		File file = new File(path);
		if( file.length() <= 0 || path == null){
			TNUtilsUi.alert(this, R.string.alert_NoteEdit_AttSizeWrong);
		}else if( file.length() > TNConst.ATT_MAX_LENTH){
			TNUtilsUi.alert(this, R.string.alert_NoteEdit_AttTooLong);
		}else{
			mAttList.add(TNNoteAtt.newAtt(file, this));
		}
	}

	class AttListAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			return mAttList.size();
		}

		@Override
		public Object getItem(int position) {
			return mAttList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mAttList.get(position).attId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				LayoutInflater inflater = LayoutInflater.from(TNAttListAct.this);
				convertView = inflater.inflate(R.layout.attlist_item, null);
				holder = new ViewHolder();
				holder.thumbnail = (ImageView)convertView.findViewById(R.id.attlistitem_thumbnail);
				holder.attName = (TextView)convertView.findViewById(R.id.attlistitem_attname);
				holder.attSize = (TextView)convertView.findViewById(R.id.attlistitem_attsize);
				convertView .setTag(holder);
			}else {
				holder = (ViewHolder)convertView.getTag();
			}
			TNUtilsSkin.setViewStateBackground(TNAttListAct.this, convertView, 
					R.id.attlistitem_layout_linlerlayout, R.drawable.listitem_pressed, 
					R.drawable.listitem_pressed, R.drawable.notelistitem_bg);
			setAttView(holder, mAttList.get(position));
			return convertView;
		}
		
	}
	
	private class ViewHolder{
		ImageView thumbnail;
		TextView attName;
		TextView attSize;
	}
	
	private void setAttView(ViewHolder holder, TNNoteAtt att){
		holder.attName.setText(att.attName);
		holder.attSize.setText((att.size*100/1024)/100f + " K");
		
//		if( att.uploadFlag == 1 && (att.serverUploadFlag < 0 || !att.isNetDiskAtt())){
////			holder.thumbnail.setImageResource(R.drawable.missing);
//			TNUtilsSkin.setImageViewDrawable(this, holder.thumbnail, R.drawable.missing);
//		}else 
			if( att.type > 10000 && att.type < 20000){
			if(att.thumbnail != null)
				holder.thumbnail.setImageURI(Uri.parse(att.thumbnail));
			else
				holder.thumbnail.setImageURI(Uri.parse(att.path));
		}
		else if( att.type > 20000 && att.type < 30000)
//			holder.thumbnail.setImageResource(R.drawable.audio);
			TNUtilsSkin.setImageViewDrawable(this, holder.thumbnail, R.drawable.ic_audio);
		else if( att.type == 40001)
//			holder.thumbnail.setImageResource(R.drawable.pdf);
			TNUtilsSkin.setImageViewDrawable(this, holder.thumbnail, R.drawable.ic_pdf);
		else if( att.type == 40002)
//			holder.thumbnail.setImageResource(R.drawable.txt);
			TNUtilsSkin.setImageViewDrawable(this, holder.thumbnail, R.drawable.ic_txt);
		else if( att.type == 40003 || att.type == 40010)
//			holder.thumbnail.setImageResource(R.drawable.word);
			TNUtilsSkin.setImageViewDrawable(this, holder.thumbnail, R.drawable.ic_word);
		else if( att.type == 40005 || att.type == 40011)
//			holder.thumbnail.setImageResource(R.drawable.ppt);
			TNUtilsSkin.setImageViewDrawable(this, holder.thumbnail, R.drawable.ic_ppt);
		else if( att.type == 40009 || att.type == 40012)
//			holder.thumbnail.setImageResource(R.drawable.excel);
			TNUtilsSkin.setImageViewDrawable(this, holder.thumbnail, R.drawable.ic_excel);
		else
//			holder.thumbnail.setImageResource(R.drawable.unknown);
			TNUtilsSkin.setImageViewDrawable(this, holder.thumbnail, R.drawable.ic_unknown);
	}
}
