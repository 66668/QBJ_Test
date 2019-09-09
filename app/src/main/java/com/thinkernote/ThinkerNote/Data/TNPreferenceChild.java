package com.thinkernote.ThinkerNote.Data;

import com.thinkernote.ThinkerNote.General.TNRunner;

public class TNPreferenceChild {
	String childName;
	String info;
	boolean visibleMoreBtn;
	TNRunner targetMethod;
	int logoId;
	String other;

	public TNPreferenceChild(String childName, String info,
			boolean visibleMoreBtn, TNRunner targetMethod) {
		this.childName = childName;
		this.info = info;
		this.visibleMoreBtn = visibleMoreBtn;
		this.targetMethod = targetMethod;
		logoId = -1;
	}

	public String getChildName() {
		return childName;
	}

	public void setChildName(String childName) {
		this.childName = childName;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public boolean isVisibleMoreBtn() {
		return visibleMoreBtn;
	}

	public void setVisibleMoreBtn(boolean visibleMoreBtn) {
		this.visibleMoreBtn = visibleMoreBtn;
	}

	public TNRunner getTargetMethod() {
		return targetMethod;
	}

	public void setTargetMethod(TNRunner targetMethod) {
		this.targetMethod = targetMethod;
	}

	public int getLogoId() {
		return logoId;
	}

	public void setLogoId(int logoId) {
		this.logoId = logoId;
	}

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}

}
