package com.thinkernote.ThinkerNote.Activity.unuse;

import java.util.Vector;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Adapter.TNCatViewHolder;
import com.thinkernote.ThinkerNote.Data.TNCat;
import com.thinkernote.ThinkerNote.General.TNUtilsSkin;
import com.thinkernote.ThinkerNote.other.PullListView;
import com.thinkernote.ThinkerNote.base.TNActBase;

/**
 *  未使用
 */
public class TNTestAct extends TNActBase implements OnScrollListener {

	private Vector<TNCat> mCats;
	private PullListView mListView;
	private TNCatAdapter mAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		mListView = (PullListView) findViewById(R.id.test_refresh_lv);
		mCats = new Vector<TNCat>();
		mAdapter = new TNCatAdapter();
		mListView.setAdapter(mAdapter);
	}

	protected void configView() {
//		TNCache.update(this);
//		mCats = TNCache.cats();
		mAdapter.notifyDataSetChanged();
	}
	
	private class TNCatAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mCats.size();
		}

		@Override
		public Object getItem(int position) {
			return mCats.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mCats.get(position).catId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TNCatViewHolder holder = null;
			if(convertView == null){
				holder = new TNCatViewHolder();
				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = (LinearLayout) layoutInflater.inflate(
						R.layout.folder_list_item, null);
				
				holder.catName = (TextView)convertView.findViewById(R.id.folder_listitem_foldername);
				holder.noteCount = (TextView)convertView.findViewById(R.id.folder_listitem_notecount);
				holder.catIcon = (ImageView)convertView.findViewById(R.id.folder_listitem_caticon);
				holder.defaultCat = (ImageView)convertView.findViewById(R.id.folder_listitem_isdefault);
				
				convertView.setTag(holder);
			}else{
				holder = (TNCatViewHolder)convertView.getTag();
			}
			setCatChildView(convertView, holder, position);
			return convertView;
		}
		
		private void setCatChildView(View convertView, TNCatViewHolder holder, int position){
			TNCat cat = mCats.get(position);
			holder.catName.setText(cat.catName);
			if(cat.catId == -1002 || cat.catId == -1001){
				holder.noteCount.setText(Html.fromHtml(
							"<font color=#4485d6>" + cat.noteCounts + "</font>篇笔记"));
			}else{
				holder.noteCount.setText(Html.fromHtml(
										"<font color=#4485d6>" + cat.catCounts + "</font>个文件夹," +
												"<font color=#4485d6>" + cat.noteCounts + "</font>篇笔记"));
			}
			if( cat.catId == -1002 ){
				TNUtilsSkin.setImageViewDrawable(TNTestAct.this, holder.catIcon, R.drawable.folderlistitem_recycle);
			}else{
				TNUtilsSkin.setImageViewDrawable(TNTestAct.this, holder.catIcon, R.drawable.folderlistitem_cat);
			}
//			if(cat.catLocalId == TNCache.user().defaultCatLocalId){
//				holder.defaultCat.setVisibility(View.VISIBLE);
//				TNUtilsSkin.setImageViewDrawable(TNTestAct.this, holder.defaultCat, R.drawable.folderlistitem_defaultcat);
//			}else{
//				holder.defaultCat.setVisibility(View.GONE);
//			}
		}
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}
}
