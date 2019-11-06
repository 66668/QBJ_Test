package com.thinkernote.ThinkerNote.adapter;

import java.util.ArrayList;
import java.util.List;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.BitmapUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class PlusPhotoAdapter extends BaseAdapter implements OnClickListener {
	
	List<String> mFiles;
	Activity mAct;
	OnClickedListener clickedListener;
	
	public PlusPhotoAdapter(Activity context,OnClickedListener clickedListener) {
		mAct = context;
		mFiles = new ArrayList<String>();
		this.clickedListener = clickedListener;
	}
	
	public void update(List<String> files) {
		mFiles = files;
	}

	@Override
	public int getCount() {
		return mFiles.size()+1;
	}

	@Override
	public Object getItem(int position) {
		return mFiles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null){
			convertView = mAct.getLayoutInflater().inflate(R.layout.ak_item_consult_photo, null);
			viewHolder = new ViewHolder();
			viewHolder.pic = (ImageView) convertView.findViewById(R.id.check_image);
			viewHolder.del = (ImageView) convertView.findViewById(R.id.delete_image);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (position == mFiles.size()){
			viewHolder.pic.setImageResource(R.drawable.pic_add);
			viewHolder.del.setVisibility(View.GONE);
		} else {
			String file = mFiles.get(position);
			viewHolder.pic.setImageBitmap(BitmapUtils.compressionPicture(file, 160,160));
			viewHolder.del.setVisibility(View.VISIBLE);
		}
		viewHolder.del.setOnClickListener(this);
		viewHolder.del.setTag(position);
		return convertView;
	}
	
	class ViewHolder{
		ImageView pic,del;
	}

	public interface OnClickedListener {

		public void OnClicked(int id);
	}

	public void setOnClickedListener(OnClickedListener on) {
		this.clickedListener = on;
	}

	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.delete_image) {
			clickedListener.OnClicked((Integer)v.getTag());
		}
	}
}
