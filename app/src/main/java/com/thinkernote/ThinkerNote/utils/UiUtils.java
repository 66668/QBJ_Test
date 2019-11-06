package com.thinkernote.ThinkerNote.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.thinkernote.ThinkerNote.R;

import java.util.Vector;

public class UiUtils {

	/**
	 * 璋冪敤鍙戠煭淇″簲鐢?
	 * 
	 * @param act
	 * @param mobile
	 * @param smsBody
	 */
	public static void sendSMSToMobile(Activity act, String mobile,
			String smsBody) {
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(Uri.parse("smsto:" + mobile));// 姝や负鍙风爜
		// intent.setType("vnd.android-dir/mms-sms");
		// intent.putExtra("address", mobile);
		intent.putExtra("sms_body", smsBody);
		act.startActivity(intent);
	}

	/**
	 * 璋冪敤缇ゅ彂鐭俊
	 * 
	 * @param act
	 * @param mobiles
	 * @param smsBody
	 */
	public static void massSMS(Activity act, Vector<String> mobiles,
			String smsBody) {
		String mobileStr = "";
		for (int i = 0; i < mobiles.size(); i++) {
			if (i == 0)
				mobileStr = mobiles.get(i);
			mobileStr = mobileStr + "," + mobiles.get(i);
		}
		sendSMSToMobile(act, mobileStr, smsBody);
	}

	/**
	 * 璋冪敤鎷ㄦ墦鐢佃瘽
	 * 
	 * @param act
	 * @param mobile
	 */
	public static void callToMobile(Activity act, String mobile) {
		Uri uri = Uri.parse("tel:" + mobile);
		Intent it = new Intent(Intent.ACTION_DIAL, uri);
		act.startActivity(it);
	}

	/**
	 * 璋冪敤鐩告満
	 * 
	 * @param act
	 * @param outUri
	 * @param requestCode
	 */
	public static void openCamera(Activity act, Uri outUri, int requestCode) {
		if (outUri == null) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startIntentForResult(act, intent, R.string.ak_alert_NoAppCanOpen,
					requestCode);
		} else {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
			startIntentForResult(act, intent, R.string.ak_alert_NoAppCanOpen,
					requestCode);
		}
		return;
	}

	/**
	 * 璋冪敤鐩稿唽
	 * 
	 * @param act
	 * @param requestCode
	 */
	public static void openPhoto(Activity act, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startIntentForResult(act, intent, R.string.ak_alert_NoAppCanOpen,
				requestCode);
	}
	
	/**
	 * 璋冪敤鍥剧墖瑁佸壀搴旂敤
	 * 
	 * @param uri
	 */
	public static void startPhotoZoom(Activity act, Uri uri) {
		/*
		 * 鑷充簬涓嬮潰杩欎釜Intent鐨凙CTION鏄?庝箞鐭ラ亾鐨勶紝澶у鍙互鐪嬩笅鑷繁璺緞涓嬬殑濡備笅缃戦〉
		 * yourself_sdk_path/docs/reference/android/content/Intent.html
		 * 鐩存帴鍦ㄩ噷闈trl+F鎼滐細CROP 锛屼箣鍓嶅皬椹病浠旂粏鐪嬭繃锛屽叾瀹炲畨鍗撶郴缁熸棭宸茬粡鏈夎嚜甯﹀浘鐗囪鍓姛鑳?, 鏄洿鎺ヨ皟鏈湴搴撶殑锛屽皬椹笉鎳侰 C++
		 * 杩欎釜涓嶅仛璇︾粏浜嗚В鍘讳簡锛屾湁杞瓙灏辩敤杞瓙锛屼笉鍐嶇爺绌惰疆瀛愭槸鎬庝箞 鍒跺仛鐨勪簡...鍚煎惣
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 涓嬮潰杩欎釜crop=true鏄缃湪寮?鍚殑Intent涓缃樉绀虹殑VIEW鍙鍓?
		intent.putExtra("crop", "true");
		// aspectX aspectY 鏄楂樼殑姣斾緥
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 鏄鍓浘鐗囧楂?
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);

		startIntentForResult(act, intent, R.string.ak_alert_NoAppCanOpen, 3);
	}

	/**
	 * 璋冪敤绯荤粺鍒嗕韩鏂囨湰
	 * 
	 * @param act
	 * @param msg
	 */
	public static void sherText(Activity act, String msg) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_TEXT, msg);
		i.setType("text/plain");
		startIntent(act, i, R.string.ak_alert_NoAppCanOpen);
	}


	/**
	 * 璋冪敤绯荤粺鍙戦?侀偖浠?
	 * 
	 * @param act
	 * @param email
	 * @param subject
	 * @param body
	 */
	public static void sendToEmail(Activity act, String email, String subject,
			String body) {
		Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
		intent.setType("text/plain");

		// 璁剧疆閭欢榛樿鍦板潃
		// intent.putExtra(android.content.Intent.EXTRA_EMAIL, reciver);
		intent.setData(Uri.parse("mailto:" + email));
		// 璁剧疆閭欢榛樿鏍囬
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		// 璁剧疆瑕侀粯璁ゅ彂閫佺殑鍐呭
		intent.putExtra(android.content.Intent.EXTRA_TEXT, body);

		startIntent(act, intent, R.string.ak_alert_NoAppCanOpen);
	}

	public static void startIntentForResult(final Activity act,
			final Intent intent, int msgId, int requestCode) {
		PackageManager packageManager = act.getPackageManager();
		if (packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
			act.startActivityForResult(intent, requestCode);
		} else {
			Toast.makeText(act, act.getResources().getString(msgId), Toast.LENGTH_LONG).show();
		}
	}

	public static void startIntent(final Activity act, final Intent intent,
			int msgId) {
		PackageManager packageManager = act.getPackageManager();
		if (packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
			act.startActivity(intent);
		} else {
			Toast.makeText(act, act.getResources().getString(msgId), Toast.LENGTH_LONG).show();
		}
	}
}
