package com.thinkernote.ThinkerNote.utils.actfun;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.utils.TNUtils;


public class TNUtilsSkin {
	private static final String TAG = "TNUtilsSkin";
	public static final String DEFAULT_SKIN = "default";
	
	static Hashtable<Integer, String> idDrawables = new Hashtable<Integer, String>();
	static{
		/* image drawable */
		/* drawable-hdpi image resource */
		idDrawables.put(R.drawable.addatt, "addatt");
		idDrawables.put(R.drawable.attachment, "attachment");
		idDrawables.put(R.drawable.back, "back");
		idDrawables.put(R.drawable.bg, "bg");
		idDrawables.put(R.drawable.blue, "blue");
		idDrawables.put(R.drawable.bottom_more_btn, "bottom_more_btn");
		idDrawables.put(R.drawable.bottom_sync_btn, "bottom_sync_btn");
		idDrawables.put(R.drawable.bottom_toolbg, "bottom_toolbg");
		idDrawables.put(R.drawable.btn_close_normal, "btn_close_normal");
		idDrawables.put(R.drawable.btn_help_close_normal, "btn_help_close_normal");
		idDrawables.put(R.drawable.btn_help_close_pressed, "btn_help_close_pressed");
		idDrawables.put(R.drawable.deletenote, "deletenote");
		idDrawables.put(R.drawable.divide, "divide");
		idDrawables.put(R.drawable.editnote, "editnote");
		idDrawables.put(R.drawable.edittag, "edittag");
		idDrawables.put(R.drawable.failed16, "failed16");
		idDrawables.put(R.drawable.file, "file");
		idDrawables.put(R.drawable.folder, "folder");
		idDrawables.put(R.drawable.foldericon, "foldericon");
		idDrawables.put(R.drawable.foldericonchild, "foldericonchild");
		idDrawables.put(R.drawable.foldericonparent, "foldericonparent");
		idDrawables.put(R.drawable.foldericonparent2, "foldericonparent2");
		idDrawables.put(R.drawable.arrows, "arrows");
		idDrawables.put(R.drawable.folderlistitem_cat, "folderlistitem_cat");
		idDrawables.put(R.drawable.folderlistitem_defaultcat, "folderlistitem_defaultcat");
		idDrawables.put(R.drawable.folderlistitem_recycle, "folderlistitem_recycle");
		idDrawables.put(R.drawable.good16, "good16");
		idDrawables.put(R.drawable.group_icon, "group_icon");
		idDrawables.put(R.drawable.ic_add, "ic_add");
		idDrawables.put(R.drawable.ic_media_pause, "ic_media_pause");
		idDrawables.put(R.drawable.ic_media_play, "ic_media_play");
		idDrawables.put(R.drawable.ic_menu_agenda, "ic_menu_agenda");
		idDrawables.put(R.drawable.ic_menu_blocked_user, "ic_menu_blocked_user");
		idDrawables.put(R.drawable.ic_menu_help, "ic_menu_help");
		idDrawables.put(R.drawable.ic_menu_info_about, "ic_menu_info_about");
		idDrawables.put(R.drawable.ic_menu_invite, "ic_menu_invite");
		idDrawables.put(R.drawable.ic_menu_play_clip, "ic_menu_play_clip");
		idDrawables.put(R.drawable.ic_menu_preferences, "ic_menu_preferences");
		idDrawables.put(R.drawable.ic_menu_refresh, "ic_menu_refresh");
		idDrawables.put(R.drawable.ic_menu_search, "ic_menu_search");
		idDrawables.put(R.drawable.ic_menu_send, "ic_menu_send");
		idDrawables.put(R.drawable.ic_menu_share, "ic_menu_share");
		idDrawables.put(R.drawable.ic_menu_start_conversation, "ic_menu_start_conversation");
		idDrawables.put(R.drawable.icon, "icon");
		idDrawables.put(R.drawable.invitefriends, "invitefriends");
		idDrawables.put(R.drawable.join_project, "join_project");
		idDrawables.put(R.drawable.light, "light");
		idDrawables.put(R.drawable.listitem_pressed, "listitem_pressed");
		idDrawables.put(R.drawable.splash0, "splash0");
//		idDrawables.put(R.drawable.splash1, "splash1");
//		idDrawables.put(R.drawable.splash2, "splash2");
		idDrawables.put(R.drawable.login_bnt_layout_bg, "login_bnt_layout_bg");
		idDrawables.put(R.drawable.logo_360, "logo_360");
		idDrawables.put(R.drawable.logo_baidu, "logo_baidu");
		idDrawables.put(R.drawable.logo, "logo");
		idDrawables.put(R.drawable.logout, "logout");
		idDrawables.put(R.drawable.main_allnote, "main_allnote");
		idDrawables.put(R.drawable.main_bottom_bg, "main_bottom_bg");
		idDrawables.put(R.drawable.main_divide, "main_divide");
		idDrawables.put(R.drawable.main_folder, "main_folder");
		idDrawables.put(R.drawable.main_newnote, "main_newnote");
		idDrawables.put(R.drawable.main_project, "main_project");
		idDrawables.put(R.drawable.main_projectlog_count_bg, "main_projectlog_count_bg");
		idDrawables.put(R.drawable.main_sync_btn, "main_sync_btn");
		idDrawables.put(R.drawable.main_tagcloud, "main_tagcloud");
		idDrawables.put(R.drawable.main_public, "main_public");
		idDrawables.put(R.drawable.more, "more");
		idDrawables.put(R.drawable.newfolder1, "newfolder1");
		idDrawables.put(R.drawable.newnote, "newnote");
		idDrawables.put(R.drawable.newtag, "newtag");
		idDrawables.put(R.drawable.newuser, "newuser");
		idDrawables.put(R.drawable.notcheck16, "notcheck16");
		idDrawables.put(R.drawable.note_edit_att, "note_edit_att");
		idDrawables.put(R.drawable.note_edit_audio, "note_edit_audio");
		idDrawables.put(R.drawable.note_edit_bg, "note_edit_bg");
		idDrawables.put(R.drawable.note_edit_camera, "note_edit_camera");
		idDrawables.put(R.drawable.note_edit_folder, "note_edit_folder");
		idDrawables.put(R.drawable.note_edit_insertcurrenttime, "note_edit_insertcurrenttime");
		idDrawables.put(R.drawable.note_edit_picture, "note_edit_picture");
		idDrawables.put(R.drawable.note_edit_speak, "note_edit_speak");
		idDrawables.put(R.drawable.note_edit_tag, "note_edit_tag");
		idDrawables.put(R.drawable.note_edit_tuya, "note_edit_tuya");
		idDrawables.put(R.drawable.noteedit_att_count_bg, "noteedit_att_count_bg");
		idDrawables.put(R.drawable.noteedit_bottom_toolbar, "noteedit_bottom_toolbar");
		idDrawables.put(R.drawable.noteedit_save, "noteedit_save");
		idDrawables.put(R.drawable.noteedit_title_bg, "noteedit_title_bg");
		idDrawables.put(R.drawable.notelistitem_bg, "notelistitem_bg");
		idDrawables.put(R.drawable.ok, "ok");
		idDrawables.put(R.drawable.preferencemore, "preferencemore");
		idDrawables.put(R.drawable.project_default_pressed, "project_default_pressed");
		idDrawables.put(R.drawable.project_divide, "project_divide");
		idDrawables.put(R.drawable.project_normal_bg, "project_normal_bg");
		idDrawables.put(R.drawable.quickaction_arrow_down, "quickaction_arrow_down");
		idDrawables.put(R.drawable.quickaction_arrow_up, "quickaction_arrow_up");
		idDrawables.put(R.drawable.quickaction_background, "quickaction_background");
		idDrawables.put(R.drawable.quickaction_bottom_frame, "quickaction_bottom_frame");
		idDrawables.put(R.drawable.quickaction_slider_btn_normal, "quickaction_slider_btn_normal");
		idDrawables.put(R.drawable.quickaction_slider_btn_on, "quickaction_slider_btn_on");
		idDrawables.put(R.drawable.quickaction_slider_btn_pressed, "quickaction_slider_btn_pressed");
		idDrawables.put(R.drawable.quickaction_slider_btn_selected, "quickaction_slider_btn_selected");
		idDrawables.put(R.drawable.quickaction_slider_grip_left, "quickaction_slider_grip_left");
		idDrawables.put(R.drawable.quickaction_slider_grip_right, "quickaction_slider_grip_right");
		idDrawables.put(R.drawable.quickaction_top_frame, "quickaction_top_frame");
		idDrawables.put(R.drawable.quickcontact_drop_shadow, "quickcontact_drop_shadow");
		idDrawables.put(R.drawable.red, "red");
//		idDrawables.put(R.drawable.reg_bg, "reg_bg");
		idDrawables.put(R.drawable.report, "report");
		idDrawables.put(R.drawable.restorenote, "restorenote");
		idDrawables.put(R.drawable.save, "save");
		idDrawables.put(R.drawable.savelog, "savelog");
		idDrawables.put(R.drawable.search, "search");
		idDrawables.put(R.drawable.search_page_btn, "search_page_btn");
		idDrawables.put(R.drawable.settings, "settings");
		idDrawables.put(R.drawable.shiftdelete, "shiftdelete");
		idDrawables.put(R.drawable.skin_icon_aoyun, "skin_icon_aoyun");
		idDrawables.put(R.drawable.skin_icon_default, "skin_icon_default");
		idDrawables.put(R.drawable.speakinput, "speakinput");
		idDrawables.put(R.drawable.sync_finished, "sync_finished");
		idDrawables.put(R.drawable.sync_notstarted, "sync_notstarted");
		idDrawables.put(R.drawable.sync_unfinished, "sync_unfinished");
		idDrawables.put(R.drawable.tag, "tag");
		idDrawables.put(R.drawable.tagcloud, "tagcloud");
//		idDrawables.put(R.drawable.helpinfo_bg, "helpinfo_bg");
//		idDrawables.put(R.drawable.helpinfo_page_bg, "helpinfo_page_bg");
		idDrawables.put(R.drawable.text_bg, "text_bg");
		idDrawables.put(R.drawable.toolbg, "toolbg");
		idDrawables.put(R.drawable.top_save, "top_save");
		idDrawables.put(R.drawable.tuya_clear, "tuya_clear");
		idDrawables.put(R.drawable.tuya_eraser_pressed, "tuya_eraser_pressed");
		idDrawables.put(R.drawable.tuya_eraser, "tuya_eraser");
		idDrawables.put(R.drawable.tuya_redu, "tuya_redu");
		idDrawables.put(R.drawable.tuya_select_color, "tuya_select_color");
		idDrawables.put(R.drawable.tuya_select_strokewidth, "tuya_select_strokewidth");
		idDrawables.put(R.drawable.tuya_undo, "tuya_undo");
		idDrawables.put(R.drawable.weibo, "weibo");
		
		/* drawable image resource */
		idDrawables.put(R.drawable.ic_audio, "ic_audio");
		idDrawables.put(R.drawable.btn_default_normal, "btn_default_normal");
		idDrawables.put(R.drawable.btn_default_pressed, "btn_default_pressed");
		idDrawables.put(R.drawable.btn_default_selected, "btn_default_selected");
		idDrawables.put(R.drawable.ic_excel, "ic_excel");
		idDrawables.put(R.drawable.login_btn_normal, "login_btn_normal");
		idDrawables.put(R.drawable.login_btn_pressed, "login_btn_pressed");
		idDrawables.put(R.drawable.login_btn_selected, "login_btn_selected");
		idDrawables.put(R.drawable.menu_normal, "menu_normal");
		idDrawables.put(R.drawable.menu_pressed, "menu_pressed");
		idDrawables.put(R.drawable.menu_selected, "menu_selected");
		idDrawables.put(R.drawable.missing, "missing");
		idDrawables.put(R.drawable.ic_pdf, "ic_pdf");
		idDrawables.put(R.drawable.ic_ppt, "ic_ppt");
		idDrawables.put(R.drawable.rankbg, "rankbg");
		idDrawables.put(R.drawable.ic_txt, "ic_txt");
		idDrawables.put(R.drawable.ic_unknown, "ic_unknown");
		idDrawables.put(R.drawable.ic_word, "ic_word");
		idDrawables.put(R.drawable.page_bg, "page_bg");
		
		/* xml drawable */
		idDrawables.put(R.drawable.layout_bg_gradientcolors, "main_bg_gradientcolors");
		idDrawables.put(R.drawable.group_bg_gradientcolors, "group_bg_gradientcolors");
		}

	public static void setImageViewDrawable(Activity activity,View listItemView, int viewId, int resId){
		ImageView iv= null;
		if(listItemView == null){
			 iv = (ImageView)activity.findViewById(viewId);
		}else{
			iv = (ImageView)listItemView.findViewById(viewId);
		}
		
		if(viewId == R.id.splash_splash_imageview){
			iv.setImageDrawable(getDrawableJPG(activity, resId));
		}else
			iv.setImageDrawable(getDrawable(activity, resId));
	}
	
	public static void setImageViewDrawable(Activity activity, ImageView v, int resId){
		v.setImageDrawable(getDrawable(activity, resId));
	}
	
	public static void setViewBackground(Activity activity, View listItemView, int viewId, int rscId){
		View v= null;
		if(listItemView == null){
			 v = (View)activity.findViewById(viewId);
		}else{
			v = (View)listItemView.findViewById(viewId);
		}
		v.setBackgroundDrawable(getDrawable(activity, rscId));
	}
	
	public static void setViewBackground(Activity activity, View v, int resId){
		v.setBackgroundDrawable(getDrawable(activity, resId));
	}
	
	public static void setImageButtomDrawable(Activity activity,View listItemView, int viewId, int resId){
		ImageButton ib= null;
		if(listItemView == null){
			 ib = (ImageButton)activity.findViewById(viewId);
		}else{
			ib = (ImageButton)listItemView.findViewById(viewId);
		}
		ib.setImageDrawable(getDrawable(activity, resId));
	}
	
	public static void setImageButtomDrawableAndStateBackground(
			Activity activity, View listItemView, int viewId, int srcId){
		ImageButton ib= null;
		if(listItemView == null){
			 ib = (ImageButton)activity.findViewById(viewId);
		}else{
			ib = (ImageButton)listItemView.findViewById(viewId);
		}
		ib.setImageDrawable(getDrawable(activity, srcId));
		
//		ib.setBackgroundDrawable(
//				getStatusDrawable(activity, 
//						R.drawable.btn_default_pressed, 
//						R.drawable.btn_default_selected, 
//						R.drawable.btn_default_normal));
		ib.setBackgroundDrawable(
				getStatusDrawable(activity, 
						R.drawable.btn_default_normal, 
						R.drawable.btn_default_normal, 
						R.drawable.btn_default_normal));
	}
	
	public static void setImageButtomDrawableAndLightColoredStateBackground(
			Activity activity, View listItemView, int viewId, int srcId){
		ImageButton ib= null;
		if(listItemView == null){
			 ib = (ImageButton)activity.findViewById(viewId);
		}else{
			ib = (ImageButton)listItemView.findViewById(viewId);
		}
		ib.setImageDrawable(getDrawable(activity, srcId));
		
		ib.setBackgroundDrawable(
				getStatusDrawable(activity, 
						R.drawable.btn_default_pressed_light_colored, 
						R.drawable.btn_default_selected_light_colored, 
						R.drawable.btn_default_normal));
	}
	
	public static void setViewStateBackground(Activity activity, View listItemView, int viewId, 
			int pressedId, int focusId, int normalId){        
        View v= null;
		if(listItemView == null){
			 v = (View)activity.findViewById(viewId);
		}else{
			v = (View)listItemView.findViewById(viewId);
		}
		v.setBackgroundDrawable(getStatusDrawable(activity, pressedId, focusId, normalId));
	}
	
	public static Drawable getListItemStatusDrawable(Activity activity){
		return getStatusDrawable(activity, 
				R.drawable.listitem_pressed, R.drawable.listitem_pressed, R.drawable.notelistitem_bg);
	}
	
	public static Drawable getListItemBigStatusDrawable(Activity activity){
		return getStatusDrawable(activity, 
				R.drawable.listitem_big_pressed, R.drawable.listitem_big_pressed, R.drawable.listitem_big_bg);
	}
	
	public static Drawable getPreferenceItemStatusDrawable(Activity activity){
		return getStatusDrawable(activity, 
				R.drawable.preference_child_bg_pressed, R.drawable.preference_child_bg_pressed, R.drawable.preference_child_bg_normal);
	}
	
	public static Drawable getStatusDrawable(Activity activity, int pressedId, int focusId, int normalId){
		StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {android.R.attr.state_pressed}, getDrawable(activity, pressedId));
        states.addState(new int[] {android.R.attr.state_focused}, getDrawable(activity, focusId));
        states.addState(new int[] {}, getDrawable(activity, normalId));
        return states;
	}
	
	public static Drawable getDrawable(Context context, int id){
		return getDrawable(context, id, -1001);
	}
	
	public static Drawable getDrawable(Context context, int id, int gravity){
		TNSettings setting = TNSettings.getInstance();
		if( setting.skinName.equals("test") && idDrawables.containsKey(id)){
			String filename = idDrawables.get(id);
			String themePath = TNUtilsAtt.getSkinsPath() + setting.skinName + "/";
			
			String fullpath = null;
			
			if( new File(themePath + filename + ".png").exists() ){
				fullpath = themePath + filename + ".png";
			}else if(new File(themePath + filename + ".jpg").exists()){
				fullpath = themePath + filename + ".jpg";
			}else if(new File(themePath + filename + ".gif").exists()){
				fullpath = themePath + filename + ".gif";
			}
			
			if( fullpath != null){
				MLog.i("getDrawable", fullpath);
				
				Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(fullpath, opts);
				
				opts.inSampleSize = TNUtilsAtt.computeSampleSize(opts, -1, 480*800);
				opts.inJustDecodeBounds = false;
				opts.inDensity = DisplayMetrics.DENSITY_HIGH;
				opts.inTargetDensity = DisplayMetrics.DENSITY_HIGH;
				
				Bitmap bm = BitmapFactory.decodeFile(fullpath, opts);
				if (bm != null) {
					BitmapDrawable bd = new BitmapDrawable(context.getResources(), bm);
					if(gravity != -1001)
						bd.setGravity(gravity);
					return bd;
				}
				
			}else if( new File(themePath + filename + ".9.png").exists() ){
				fullpath = themePath + filename + ".9.png";
				MLog.i("getDrawable", fullpath);
	    		Bitmap bitmap = BitmapFactory.decodeFile(fullpath);
	    		if( bitmap != null){
	    			byte[] chunk = bitmap.getNinePatchChunk();
	    			if(NinePatch.isNinePatchChunk(chunk)){
	    				return new NinePatchDrawable(context.getResources(), 
	    						bitmap, chunk, new Rect(), null);
	    			}
	    		}
			}
			
		}
//		Log.i("getDrawable", "id=" + id);
		
		return context.getResources().getDrawable(id);
	}
	
	public static Drawable getDrawableJPG(Context context, int id){
		TNSettings setting = TNSettings.getInstance();
		if( setting.skinName.equals("test") && idDrawables.containsKey(id)){
			String filename = idDrawables.get(id);
			String themePath = TNUtilsAtt.getSkinsPath() + setting.skinName + "/";
			
			String fullpath = null;
			
			if( new File(themePath + filename + ".png").exists() ){
				fullpath = themePath + filename + ".png";
			}else if(new File(themePath + filename + ".jpg").exists()){
				fullpath = themePath + filename + ".jpg";
			}else if(new File(themePath + filename + ".gif").exists()){
				fullpath = themePath + filename + ".gif";
			}
			
			if( fullpath != null){
				MLog.i(TAG, fullpath);
				
				Options opts = new BitmapFactory.Options();opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(fullpath, opts);
				
				opts.inSampleSize = TNUtilsAtt.computeSampleSize(opts, -1, 480*800);
				opts.inJustDecodeBounds = false;
				opts.inDensity = DisplayMetrics.DENSITY_HIGH;
				opts.inTargetDensity = DisplayMetrics.DENSITY_HIGH;
				opts.inPreferredConfig = Bitmap.Config.RGB_565;
				opts.inPurgeable = true;
				opts.inInputShareable = true;
				
				Bitmap bm = BitmapFactory.decodeFile(fullpath, opts);
				if (bm != null) {
				  return new BitmapDrawable(context.getResources(), bm);
				}
				
			}
			
		}
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		//獲取資源圖片
		InputStream is = context.getResources().openRawResource(id);
		Bitmap bm = BitmapFactory.decodeStream(is,null,opt);
		
		return new BitmapDrawable(context.getResources(), bm);
	}
	
	//从assets中获取定制皮肤
	public static void setAssetsSkins(String skinName){
		MLog.i("TNUtilsSkin", "setAssetsSkins" + skinName);
		if(skinName.equals("default"))
			return;
		
		try {
			String skinPath = TNUtilsAtt.getSkinsPath() + skinName;
			File f = new File(skinPath);
			if(!f.exists()){
				InputStream inputStream = TNUtils.getAppContext().getAssets().open(skinName + ".zip");
				if(inputStream == null)
					return ;
				TNUtils.unZipStream(inputStream, skinPath);
			}
			TNSettings.getInstance().skinName = skinName;
			TNSettings.getInstance().savePref(false);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
