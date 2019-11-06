package com.thinkernote.ThinkerNote.utils.actfun;

import com.thinkernote.ThinkerNote.utils.TNUtils;

import java.util.Vector;

public class TNUtilsTag {
	//private static final String TAG = "TNUtilsTag";
	
	private static final String SPLIT = "[,;，；]";
	private static final String EMAIL_SPLIT = "[<>,;，；]";

	public static String clearTagStr(String aTagStr){
		return makeTagStr(splitTagStr(aTagStr));
	}
	
	public static Vector<String> splitTagStr(String aTagStr){
		Vector<String> goodTag = new Vector<String>();
		String[] tags = aTagStr.split(SPLIT);
		for(String tag : tags){
			tag = tag.trim();
			//Log.i(TAG, "tag:" + tag);
			if( tag.length() > 0 && tag.length() <= 50 && !goodTag.contains(tag)){
				goodTag.add(tag);
			}
		}
		return goodTag;
	}
	
	public static Vector<String> splitEmailStr(String aEmailStr){
		Vector<String> goodEmail = new Vector<String>();
		String[] emails = aEmailStr.split(EMAIL_SPLIT);
		for(String email : emails){
			email = email.trim();
			//Log.i(TAG, "tag:" + tag);
			if( email.length() <= 100 && TNUtils.checkRegex(TNUtils.FULL_EMAIL_REGEX, email)){
				goodEmail.add(email);
			}
		}
		return goodEmail;
	}
		
	public static String makeTagStr(Vector<String> aTags){
		String result = aTags.toString();
		return result.substring(1, result.length()-1);
	}
	
	public static boolean isTagNameOk(String aTag){
		String[] tags = aTag.split(SPLIT);
		// lq #627
		return (tags.length==1 && aTag.length()==tags[0].length());
	}

}
