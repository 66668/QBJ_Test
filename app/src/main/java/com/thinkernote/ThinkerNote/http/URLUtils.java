package com.thinkernote.ThinkerNote.http;

/**
 * URL 统一管理类
 * 说明：登录+注册接口不添加token
 */

public class URLUtils {
    //URL根目录
//    public final static String API_BASE_URL = "http://new.qingbiji.cn:8088/";//内网测试
    public final static String API_BASE_URL = "https://s.qingbiji.cn/";//正式1
//    public final static String API_BASE_URL = "http://new.qingbiji.cn/";//正式2

    public final static String WEICHAT_BASE_URL = "https://api.weixin.qq.com";//微信登陆BaseURL

    /**
     * =============================================================================================
     * ============================================通用=================================================
     * =============================================================================================
     *
     */

    /**
     * 微信 获取accessToken
     */
    public static final String ACCESS_TOKEN = "/sns/oauth2/access_token";

    /**
     * 微信 获取个人信息
     */
    public static final String WEICHAT_MESSAGE = "/sns/userinfo";

    /**
     * =============================================================================================
     * ============================================首页相关=================================================
     * =============================================================================================
     *
     */

    /**
     * 登录相关(登录 注册 找回密码)
     */
    public static class Log {
        /**
         * 正常登录
         */
        public static final String LOGINNORMAL = "api/login";
        /**
         * 登录后的同步更新
         */
        public static final String PROFILE = "api/user/profile";

        /**
         * qq登录
         */
        public static final String LOGIN_THIRD = "api/login/third";

        /**
         * 手机验证码接口
         */
        public static final String PHONE_QUERFY_CODE = "api/verifycode";
        /**
         * 邮箱验证码接口
         */
        public static final String EMAIL_QUERFY_CODE = "api/verifycode/email";

        /**
         * 绑定手机
         */
        public static final String LOGIN_BIND = "api/login/bind";

        /**
         * 图片验证
         */
        public static final String VERIFY_PIC = "api/captcha";

        /**
         * 找回密码 提交
         */
        public static final String SUBMIT_FINDPS = "api/user/password/reset";

        /**
         * 注册 提交
         */
        public static final String SUBMIT_RESGIST = "api/register";

        /**
         * 修改手机号 提交
         */
        public static final String CHANGE_PHONE = "api/user/profile";

        /**
         * 修改手机号 提交
         */
        public static final String CHANGE_PS = "api/user/password";

        /**
         * 修改手机号 提交
         */
        public static final String CHANGE_NAME_OR_EMAIL = "api/user/profile";

        /**
         * 获取用户信息
         */
        public static final String USER_INFO = "api/user/profile";

        /**
         * logout
         */
        public static final String LOGOUT = "api/logout";

    }

    public static class Home {
        /**
         * 检查更新
         */
        public static final String UPGRADE = "api/app/upgrade";

        /**
         * 下载路径 说明
         */
        @Deprecated
        public static final String DOWNLOAD = "static/ThinkerNote-Setup.apk";


        /**
         * 同步数据
         */
        public static final String SYNCHRONIZE = "api/app/upgrade";


        /**
         * 支付
         */
        public static final String PAY_TIP = "api/margin/deposit";

        /**
         * pic 上传 （feedback，等）
         * https://s.qingbiji.cn/api/attachment?filename=IMG_20180525_132850.jpg&session_token=7E3ECyCspLM7NXPD6wRbBatBV9SKrX4q89fUxmwf
         */
        public static final String UPLOAD_PIC = "api/attachment";


    }

    public static class Cat {
        /**
         * 设置默认路径
         */
        public static final String DEFAULT_FOLDER = "api/folders/default";

        /**
         * folderMove
         */
        public static final String FOLDER_MOVE = "api/folders/move";

        /**
         * folderMove
         */
        public static final String FOLDER_TRASH = "api/note/trash";

        /**
         * GetFolderNoteIds
         */
        public static final String FOLDER_NOTEIDS = "api/folders/note/ids";

        /**
         * GetFolderNoteIds
         */
        public static final String FOLDER = "api/folders";

    }

    public static class Note {
        /**
         * 文件
         */
        public static final String FOLDER = "api/folders";

        /**
         * 标签
         */
        public static final String TAG = "api/tags";
        /**
         * 标签
         */
        public static final String TAGLIST = "api/tags";
        /**
         * 标签
         */
        public static final String ALLNOTESID = "api/note/ids";

        /**
         * getNote
         */
        public static final String NOTE = "api/note";//
        /**
         * getTrashNote
         */
        public static final String TRASH_NOTE = "api/note/trash/ids";//

        /**
         * recovery
         */
        public static final String RECOVERY_NOTE = "api/note/trash";//

        /**
         * getNote：
         * https://s.qingbiji.cn/api/attachment?filename=IMG_20180525_132850.jpg&session_token=7E3ECyCspLM7NXPD6wRbBatBV9SKrX4q89fUxmwf
         */
        public static final String UPLOAD_PIC = "api/attachment";//

        /**
         * 设置默认路径
         */
        public static final String DEFAULT_FOLDER = "api/folders/default";

        /**
         * 设置默认路径
         */
        public static final String VERIFY_EMAIL = "api/user/verifyemail";

        /**
         * noteList
         */
        public static final String NOTE_LIST_FOLDERID = "api/folders/note";

        /**
         * noteList
         */
        public static final String NOTE_LIST_TAGID = TAGLIST;


    }

    /**
     * 设置相关
     */
    public static class Settings {
        /**
         * feedBack
         */
        public static final String FEEDBACK = "api/feedback";
    }


}
