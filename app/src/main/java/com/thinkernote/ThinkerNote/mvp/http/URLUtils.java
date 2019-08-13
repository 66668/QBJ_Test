package com.thinkernote.ThinkerNote.mvp.http;

/**
 * URL 统一管理类
 * 说明：登录+注册接口不添加token
 */

public class URLUtils {
    //URL根目录
//    public final static String API_BASE_URL = "http://new.qingbiji.cn:8088/";//内网测试
    public final static String API_BASE_URL = "https://s.qingbiji.cn/";//正式1
//    public final static String API_BASE_URL = "http://new.qingbiji.cn/";//正式2

    //TODO
    public final static String WEICHAT_BASE_URL = "https://api.weixin.qq.com";//微信登陆BaseURL

    /**
     * =============================================================================================
     * ============================================通用=================================================
     * =============================================================================================
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
     * ============================================登陆验证相关=================================================
     * =============================================================================================
     *
     */

    /**
     * 登录相关(登录 注册 找回密码)
     */
    public static class Log {
        /**
         * 注册 提交
         */
        public static final String SUBMIT_RESGIST = "api/register";
        /**
         * 正常登录
         */
        public static final String LOGINNORMAL = "api/login";
        /**
         * 登录后的同步更新
         */
        public static final String PROFILE = "api/user/profile";


        /**
         * 手机验证码接口
         */
        public static final String PHONE_QUERFY_CODE = "api/verifycode";
        /**
         * 邮件找回密码，发送邮件验证码
         * <p>
         * 参数:
         * 1. email
         * 2. vtype (register, change, forgot)
         */
        public static final String EMAIL_QUERFY_CODE = "api/verifycode/email";

        /**
         * 第三方登陆的绑定接口
         * POST
         * 参数
         * <p>
         * 1. btype (2 微博，3qq) bid (第三方 id)
         * 2. name (第三方昵称，没有传空) access_token refresh_token
         * 3. stamp (时间戳)
         * 4. phone (轻笔记的手机号)
         * 5. sign (签名)
         */
        public static final String LOGIN_BIND = "api/login/bind";

        /**
         * 第三方登录
         * 参数
         * 1. btype (2微博，3qq)
         * 2. bid (第三方的 id)
         * 3. stamp (时间戳)
         * 4. sign (签名)
         */
        public static final String LOGIN_THIRD = "api/login/third";

        /**
         * 图片验证
         */
        public static final String VERIFY_PIC = "api/captcha";

        /**
         * 找回密码 提交
         */
        public static final String SUBMIT_FINDPS = "api/user/password/reset";

        /**
         * 修改手机号 提交
         */
        public static final String CHANGE_PS = "api/user/password";


        /**
         * 获取用户信息/修改个人信息
         * <p>
         * get方式 获取个人详细信息
         * <p>
         * put方式 修改个人信息：
         * 1. email 邮箱(可选)
         * 2. username 用户名(可选)
         * 3. phone 手机号(可选)
         * 4. vcode 手机验证码(可选)(change)
         */
        public static final String USER_INFO = "api/user/profile";

        /**
         * logout
         */
        public static final String LOGOUT = "api/logout";

        /**
         * 重发验证邮件
         */
        public static final String VERIFY_EMAIL = "api/user/verifyemail";

    }

    /**
     * =============================================================================================
     * ============================================首页相关=================================================
     * =============================================================================================
     */


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
         * 支付
         */
        public static final String PAY_TIP = "api/margin/deposit";

        /**
         * 上传附件
         * POST /api/attachment
         * 参数
         * 1. body 里面的文件字段为 file
         * 注: 需要登录
         * <p>
         * https://s.qingbiji.cn/api/attachment?filename=IMG_20180525_132850.jpg&session_token=7E3ECyCspLM7NXPD6wRbBatBV9SKrX4q89fUxmwf
         */
        public static final String UPLOAD_FILE = "api/attachment";

        /**
         * get /attachment/(id) 下载附件
         *
         * <p>
         * 注: 需要登录。
         * <p>
         * 附件的处理和轻笔记之前的处理文件相同，先上传附件，然后将附件信息写到笔记内容中，后 台为自动判断有没有附件并将附件对应到笔记上。
         */
        public static final String DOWNLOAD_FILE = "/attachment";

    }
    /**
     * =============================================================================================
     * ============================================Tags=================================================
     * =============================================================================================
     *
     */


    /**
     * 标签
     */
    public static class Tags {
        /**
         * 标签
         * （1）POST  创建标签
         * 1. name 名字 注: 需要登录
         * <p>
         * （2）PUT 修改标签
         * 参数
         * 1. name 名字
         * 2. tag_id 标签 id
         * <p>
         * （3）DELETE 删除标签
         * <p>
         * 参数
         * 1. tag_id 标签 id
         * <p>
         * 注: 需要登录，删除标签会把所有属于此标签的笔记标签清除，不会删除笔记。
         */
        public static final String TAG = "api/tags";

    }
    /**
     * =============================================================================================
     * ============================================文件夹相关=================================================
     * =============================================================================================
     *
     */

    /**
     * 文件夹相关
     */
    public static class Cat {
        /**
         * （1）GET 获取文件夹
         * 参数
         * 1. folder_id 文件夹 id
         * <p>
         * 注: 需要登录，不传为获取所有根目录文件夹，传 folder_id 为获取此文件夹下的所有文件夹， 不分页。
         * <p>
         * （2）POST 创建文件夹
         * 参数
         * 1. pid 父文件夹 id
         * 2. name 文件夹名
         * <p>
         * 注: 需要登录。最多可为 5 级目录
         * <p>
         * （3）PUT 修改文件夹
         * 参数
         * 1. folder_id
         * 2. name 注: 需要登录
         * <p>
         * （4）DELETE 删除文件夹
         * <p>
         * 参数
         * 1. folder_id 文件夹 id
         * <p>
         * 注: 需要登录，删除文件夹会把属于此文件夹的所有笔记文件夹信息删除，不会删除笔记，文件夹只可一级一级删除。
         */
        public static final String FOLDER = "api/folders";

        /**
         * 设置默认文件夹
         * 参数
         * <p>
         * 1. folder_id 文件夹 id
         * <p>
         * 注: 需要登录，设置笔记的默认文件夹
         */
        public static final String FOLDER_DEFAULT = "api/folders/default";

        /**
         * 移动文件夹
         * 参数
         * <p>
         * 1. folder_id 文件夹 id
         * 2. parent_id 目录文件夹 id
         * <p>
         * 注: 需要登录，文件夹只可一级一级移动，有子文件夹则不能移动。
         */
        public static final String FOLDER_MOVE = "api/folders/move";


        /**
         * folder_id下的所有note id列表
         */
        public static final String NOTE_ID_LIST_BY_FOLDER = "api/folders/note/ids";

        /**
         * 获取文件夹下的笔记列表
         * get
         * 参数
         * 1. folder_id 文件夹 id
         * 2. pagesize 每页大小
         * 3. pagenum 页码
         * 4. sortord 排序值(create_at, update_at)
         */
        public static final String NOTE_LIST_BY_FOLDER = "api/folders/note";
    }

    /**
     * =============================================================================================
     * ============================================笔记相关=================================================
     * =============================================================================================
     */

    public static class Note {

        /**
         * (1)post 创建笔记:
         * <p>
         * 参数
         * 1. title 标题
         * 2. content 内容
         * 3. tags 标签(以 , 分隔)
         * 4. folder_id 文件夹 id(可选)
         * 5. longitude 经度(可选)
         * 6. latitude 纬度(可选)
         * 7. address 地址(可选)
         * 8. radius 半径(可选)
         * <p>
         * 注: 需要登录，地址位置如果传，需要全部传，文件夹 id 为这篇笔记创建在哪个文件夹下面
         * <p>
         * (2)PUT 修改笔记
         * <p>
         * 参数
         * 1. note_id 笔记 id
         * 2. folder_id 文件夹 id(可选)
         * 3. title 标题(可选)
         * 4. content 内容(可选)
         * 5. tags 标签(可选)
         * 6. create_time 创建时间(可选)
         * <p>
         * 注: 需要登录，传 folder_id 表示修改笔记所属文件夹，创建时间可修改。
         * <p>
         * <p>
         * (3)get 获取笔记
         * <p>
         * 参数
         * <p>
         * 1. note_id 笔记 id(可选)
         * 2. pagesize 每页大小
         * 3. pagenum 页码
         * 4. sortord 排序(create_at, update_at)
         * <p>
         * 注: 需要登录，传 note_id 表示获取单篇笔记。传 pagenum 和 pagesize 表示分页获取笔记 列表。sortord 为笔记排序方式
         * <p>
         * （4）DELETE  删除笔记到回收站
         * 参数
         * <p>
         * 1. note_id 笔记 id
         * <p>
         * 注: 需要登录
         */
        public static final String NOTE = "api/note";//

        /**
         * （1）PUT 恢复回收站的笔记
         * <p>
         * 参数
         * 1. note_id
         * 2. folder_id
         * <p>
         * 注: 需要登录
         * <p>
         * （2）GET 获取回收站笔记列表
         * <p>
         * 参数
         * 1. pagesize
         * 2. pagenum
         * <p>
         * 注: 需要登录
         * <p>
         * （3）DELETE 从回收站删除笔记
         * <p>
         * <p>
         * 参数
         * <p>
         * 1. note_id
         */
        public static final String NOTE_TRASH = "api/note/trash";

        /**
         * 获取回收站笔记id 列表
         */
        public static final String NOTE_TRASH_ID = "api/note/trash/ids";//

        /**
         * 获取所有笔记的id
         */
        public static final String NOTE_ALL_ID = "api/note/ids";

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
