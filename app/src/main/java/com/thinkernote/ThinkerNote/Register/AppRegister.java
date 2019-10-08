package com.thinkernote.ThinkerNote.Register;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.thinkernote.ThinkerNote.base.TNConst;

public class AppRegister extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);

		// 将该app注册到微信
		msgApi.registerApp(TNConst.WX_APP_ID);
	}
}
