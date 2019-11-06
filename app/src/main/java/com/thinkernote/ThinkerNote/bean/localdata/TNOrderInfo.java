package com.thinkernote.ThinkerNote.bean.localdata;

import java.io.Serializable;

public class TNOrderInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public String app_id;
	public String format;
	public String timestamp;
	public String out_trade_no;
	public String product_code;
	public double total_amount;
	public String subject;
	public String body;
	public String charset;
	public String method;
	public String sign_type;
	public String version;
	public String sign;
	public String notify_url;
	public String return_url;

}
