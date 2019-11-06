package com.thinkernote.ThinkerNote.bean.localdata;

import java.io.Serializable;

/**
 * 标签所有信息
 */
public class TNTag implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long tagLocalId;
	public long tagId;
	public String tagName;
	public String strIndex;
	public int trash;
	public long userId;
	
	public int noteCounts;
}
