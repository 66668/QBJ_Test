package com.thinkernote.ThinkerNote.mvp.http.third;


/**
 * 微信登陆
 */

public interface WeichatHttpService {

    /**
     * 微信第三方登陆 分享的builder
     */
    class WeiChatBuilder {
        /**
         * @return
         */
        public static WeichatHttpService getWeiChatServer() {
            return WeichatHttpUtils.getInstance().getWeiChatServer(WeichatHttpService.class);
        }
    }

    /**
     ********************************************--微信--********************************************************
     */

//    /**
//     * 微信获取ACCESS_TOKEN等信息的URL
//     */
//    @GET(URLUtils.ACCESS_TOKEN)
//    Observable<WeichatAccessTokenBean> getAccessToken(
//            @Query("appid") String AppID,
//            @Query("secret") String secret,
//            @Query("code") String code,
//            @Query("grant_type") String grant_type);
//
//    /**
//     * 微信获取个人信息
//     */
//    @GET(URLUtils.WEICHAT_MESSAGE)
//    Observable<WeichatPersonMessageBean> getWeichatMessage(
//            @Query("access_token") String access_token,
//            @Query("openid") String openid);
}