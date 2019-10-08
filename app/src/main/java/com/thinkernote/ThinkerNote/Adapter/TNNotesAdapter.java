package com.thinkernote.ThinkerNote.Adapter;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.base.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsAtt;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;

public class TNNotesAdapter extends BaseAdapter {
	private static final String TAG = "TNNotesAdapter";
	private static final int BIG_THUMBNAIL_WIDTH = 120;
	public static final int BIG_THUMBNAIL_HEIGHT = 90;
	
	private Activity mActivity;
	private LayoutInflater layoutInflater = null;
	private Vector<TNNote> mNotes;
	private float mDensity;

	public TNNotesAdapter(Activity activity, Vector<TNNote> notes, float density) {
		this.mActivity = activity;
		this.layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mNotes = notes;
		this.mDensity = density;
	}
	
	public void updateNotes(Vector<TNNote> notes){
		this.mNotes = notes;
	}

	@Override
	public int getCount() {
		return mNotes.size();
	}

	@Override
	public Object getItem(int position) {
		return mNotes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mNotes.get(position).noteLocalId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TNNoteViewHolder holder = null;
		if (convertView == null) {
			holder = new TNNoteViewHolder();
			convertView = (LinearLayout) layoutInflater.inflate(
					R.layout.notelistitem, null);
			
			holder.noteTitle = (TextView)convertView.findViewById(R.id.notelistitem_title);
			holder.date = (TextView)convertView.findViewById(R.id.notelistitem_date);
			holder.shortContent = (TextView)convertView.findViewById(R.id.notelistitem_shortcontent);
			holder.thumbnail = (ImageView)convertView.findViewById(R.id.notelistitem_thumbnail1);
			holder.thumbnailBg = (ImageView)convertView.findViewById(R.id.notelistitem_thumbnail_bg);
			
			convertView.setTag(holder);
		}else{
			holder = (TNNoteViewHolder) convertView.getTag();
		}
		setNoteView(holder, position);

		return convertView;
	}

	private void setNoteView(TNNoteViewHolder holder, int position) {
		TNNote note = (TNNote) getItem(position);

		holder.noteTitle.setText(note.title);
		holder.shortContent.setText(TNUtilsHtml.decodeHtml(note.shortContent.trim()));
		if (TNSettings.getInstance().sort == TNConst.UPDATETIME) {
			holder.date.setText(TNUtils.formatDateToWeeks(note.lastUpdate));
		} else {
			holder.date.setText(TNUtils.formatDateToWeeks(note.createTime));
		}
	
		ImageView thumbnailView = holder.thumbnail;
//		thumbnailView.setTag(null);
		if (note.attCounts > 0) {
			if (!TextUtils.isEmpty(note.thumbnail) && !("null").equals(note.thumbnail)) {
				holder.thumbnailBg.setVisibility(View.VISIBLE);
				thumbnailView.setImageBitmap(TNUtilsAtt.getImage(note.thumbnail, 90));
				
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
						(int) (46 * mDensity), (int) (46 * mDensity), Gravity.CENTER);
				holder.thumbnailBg.setLayoutParams(layoutParams);
				thumbnailView.setLayoutParams(layoutParams);
			} else {
				holder.thumbnailBg.setVisibility(View.INVISIBLE);
				thumbnailView.setImageResource(R.drawable.notelist_thumbnail_att);
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
						(int) (46 * mDensity), (int) (46 * mDensity), Gravity.CENTER);
				thumbnailView.setLayoutParams(layoutParams);
			}
		} else {
			thumbnailView.setImageResource(R.drawable.notelist_thumbnail_note);
			holder.thumbnailBg.setVisibility(View.INVISIBLE);
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					(int) (46 * mDensity), (int) (46 * mDensity), Gravity.CENTER);
			thumbnailView.setLayoutParams(layoutParams);
			
		}
	}
	
}
