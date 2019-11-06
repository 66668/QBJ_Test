package com.thinkernote.ThinkerNote.bean.localdata;

import java.io.File;
import java.io.Serializable;


import android.app.Activity;
import android.graphics.BitmapFactory;

import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.TNUtils;
import com.thinkernote.ThinkerNote.utils.actfun.TNUtilsAtt;

/**
 * 笔记附件的所有信息
 */
public class TNNoteAtt implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public long attLocalId;
	public long noteLocalId;
	public long attId;
	public String attName;
	public int type;
	public String path;
	public int syncState;//1表示未完全同步，2表示完全同步，3表示新增
	
	public int size;
	
	public String digest;
	
	public String thumbnail;
	
	public int width;
	public int height;

	
	public TNNoteAtt copyAtt(){
		TNNoteAtt att = new TNNoteAtt();
		att.attLocalId = -1;
		att.attId = -1;
		att.attName = attName;
		att.type = type;
		att.path = TNUtilsAtt.getTempPath(att.attName);

		File file = new File(path);
		if(!file.exists())
			return null;
		
		try {
			TNUtilsAtt.copyFile(file.getPath(), att.path);	
			att.makeThumbnail();
		} catch (Exception e) {
			e.printStackTrace();
		}
		att.size = (int) file.length();
		
		BitmapFactory.Options bfo = TNUtilsAtt.getImageSize(att.path);
		att.width = bfo.outWidth;
		att.height = bfo.outHeight;
		att.syncState = 0;
		
		att.digest = TNUtilsAtt.fileToMd5(att.path);
		
		return att;
	}
	
	public static TNNoteAtt newAtt(Object aFile, Activity act){
		TNNoteAtt att = new TNNoteAtt();
		att.attLocalId = -1;
		att.attId = -1;
		if(File.class.isInstance(aFile)){
			File file = (File)aFile;
			att.attName = file.getName();
			if(att.attName.length() > 100){
				String suffix=att.attName.substring(att.attName.lastIndexOf("."));
				att.attName=att.attName.substring(0, 100-suffix.length() - 1 ) + "~" + suffix;
			}
			att.type = TNUtilsAtt.getAttType(att.attName);
			att.path = TNUtilsAtt.getTempPath(att.attName);
			if(att.type > 10000 && att.type < 20000 && file.length() >300*1024 
					&& TNSettings.getInstance().pictureCompressionMode == 1){
				boolean b = TNUtilsAtt.compressionPicture(file, att.path);
				if(!b){
					try {
						TNUtilsAtt.copyFile(file.getPath(), att.path);	
						att.makeThumbnail();
					} catch (Exception e) {
						e.printStackTrace();
					}
					att.size = (int) file.length();
				}else{
					try {
						att.makeThumbnail();
					} catch (Exception e) {
						e.printStackTrace();
					}
					File outfile = new File(att.path);
					att.size = (int) outfile.length();
				}
			}else{
				try {
					TNUtilsAtt.copyFile(file.getPath(), att.path);	
					att.makeThumbnail();
				} catch (Exception e) {
					e.printStackTrace();
				}
				att.size = (int) file.length();
			}
		}else if(String.class.isInstance(aFile)){ // from asset
			String assetName = (String)aFile;
			att.attName = assetName;
			if(att.attName.length() > 100){
				String suffix=att.attName.substring(att.attName.lastIndexOf("."));
				att.attName=att.attName.substring(0, 100-suffix.length() - 1 ) + "~" + suffix;
			}
			att.type = TNUtilsAtt.getAttType(att.attName);
			att.path = TNUtilsAtt.getTempPath(att.attName);
			try {
				TNUtilsAtt.copyFile(
						TNUtils.getAppContext().getAssets().open(assetName), att.path);
				att.makeThumbnail();
			} catch (Exception e) {
				e.printStackTrace();
			}
			File file = new File(att.path);
			att.size = (int) file.length();
		}
		BitmapFactory.Options bfo = TNUtilsAtt.getImageSize(att.path);
		att.width = bfo.outWidth;
		att.height = bfo.outHeight;
		att.syncState = 0;
		
		att.digest = TNUtilsAtt.fileToMd5(att.path);
		
		return att;
	}
	
	public void makeThumbnail() throws Exception{
		if(type > 10000 && type < 20000){
			thumbnail = TNUtilsAtt.makeThumbnailForImage(path);
		}
	}

	@Override
	public String toString() {
		return "TNNoteAtt{" +
				"attLocalId=" + attLocalId +
				", noteLocalId=" + noteLocalId +
				", attId=" + attId +
				", attName='" + attName + '\'' +
				", type=" + type +
				", path='" + path + '\'' +
				", syncState=" + syncState +
				", size=" + size +
				", digest='" + digest + '\'' +
				", thumbnail='" + thumbnail + '\'' +
				", width=" + width +
				", height=" + height +
				'}';
	}
}
