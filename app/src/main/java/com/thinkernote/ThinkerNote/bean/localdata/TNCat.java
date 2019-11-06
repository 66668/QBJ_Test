package com.thinkernote.ThinkerNote.bean.localdata;

import java.io.Serializable;

public class TNCat implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public long catLocalId;
	public String catName;
	public long catId;
	public int trash;
	public int noteCounts;
	public int catCounts;
	public int deep;
	public long pCatId;
	public long createTime;
	public long lastUpdateTime;
	public int isNew;
	public long userId;
	
	public TNCat copy(){
		TNCat cat = new TNCat();
		cat.catId = catId;
		cat.catName = catName;
		cat.trash = trash;
		cat.noteCounts = noteCounts;
		cat.catCounts = catCounts;
		cat.deep = deep;
		cat.pCatId = pCatId;
		cat.createTime = createTime;
		cat.lastUpdateTime = lastUpdateTime;
		return cat;
	}
}
