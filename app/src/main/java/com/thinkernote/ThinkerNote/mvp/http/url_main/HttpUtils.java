package com.thinkernote.ThinkerNote.mvp.http.url_main;

import android.content.Context;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thinkernote.ThinkerNote.utils.actfun.TNSettings;
import com.thinkernote.ThinkerNote.utils.CheckNetworkUtils;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.base.Constants;
import com.thinkernote.ThinkerNote.mvp.http.HttpHead;
import com.thinkernote.ThinkerNote.mvp.http.NullOnEmptyConverterFactory;
import com.thinkernote.ThinkerNote.mvp.http.ParamNames;
import com.thinkernote.ThinkerNote.mvp.http.URLUtils;
import com.thinkernote.ThinkerNote.mvp.http.fileprogress.FileProgressInterceptor;
import com.thinkernote.ThinkerNote.mvp.http.fileprogress.FileProgressListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sjy on 2018/6/14.
 * 网络请求工具类 用于集成retrofit+okhttp3
 * 由 MyHttpService负责调用
 * <p>
 */

public class HttpUtils {
    private static HttpUtils instance;
    private Gson gson;
    private Context context;

    private Object defaultHttps;//默认
    private Object fileHttps;//进度条
    private Object uploadHttps;//上传
    Cache cache = null;
    static File httpCacheDirectory;

    private boolean debug;//判断 app版本，由application设置

    private final static String TAG = "HttpUtils";

    /**
     * 获取实例对象
     */
    public static HttpUtils getInstance() {
        if (instance == null) {
            synchronized (HttpUtils.class) {
                if (instance == null) {
                    instance = new HttpUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化 application中初始化
     *
     * @param context
     * @param debug
     */
    public void init(Context context, boolean debug) {
        this.context = context;
        this.debug = debug;
        HttpHead.init(context);//设置http头
        //设置单独缓存
        initCache();
    }
    //=================================== 获取Retrofit样式====================================

    /**
     * 01 Retrofit构建 默认样式：Retrofit+okhttp
     * <p>
     * header+token+无缓存
     */
    public <T> T getDefaultServer(Class<T> clz) {
        if (defaultHttps == null) {
            synchronized (HttpUtils.class) {
                defaultHttps = getDefaultBuilder(URLUtils.API_BASE_URL).build().create(clz);
            }
        }
        initCache();//再设置一遍，可以注销
//        MLog.i("defaulthttp");
        return (T) defaultHttps;
    }

    /**
     * 02 Retrofit构建:Retrofit+okhttp
     * 查看文件上传下载进度/结合progress使用
     */

    public <T> T getFileServer(Class<T> clz, FileProgressListener listener) {
        if (fileHttps == null) {
            synchronized (HttpUtils.class) {
                fileHttps = getFileBuilder(URLUtils.API_BASE_URL, listener).build().create(clz);
            }
        }
//        MLog.i("FileDownloadhttp");
        return (T) fileHttps;
    }

    /**
     * 02 Retrofit构建:Retrofit+okhttp
     * 查看文件上传下载进度/结合progress使用
     */

    public <T> T upLoadServer(Class<T> clz) {
        if (uploadHttps == null) {
            synchronized (HttpUtils.class) {
                uploadHttps = uploadBuilder(URLUtils.API_BASE_URL).build().create(clz);
            }
        }
//        MLog.i("uploadhttp");
        return (T) uploadHttps;
    }


    //设置缓存
    private void initCache() {
        if (httpCacheDirectory == null || cache == null) {
            synchronized (HttpUtils.class) {
                httpCacheDirectory = new File(context.getCacheDir(), Constants.APP_HTTP_ACACHE_FILE);
                try {
                    cache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);
                } catch (Exception e) {
                    Log.e("OKHttp", "Could not create http cache", e);
                }
            }
        }
    }

    //================================= Retrofit构建 可以根据自己缓存需要，灵活设置====================================

    /**
     * 01 retrofit配置
     * （可用链式结构，但需要返回处理build()，就不用链式）
     */
    private Retrofit.Builder getDefaultBuilder(String apiUrl) {

        //retrofit配置
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.client(getDefaultOkhttp());//设置okhttp（重点），不设置走默认的
        builder.baseUrl(apiUrl);//设置远程地址
        //如下1--4 至少四选一
        builder.addConverterFactory(new NullOnEmptyConverterFactory());      //01:添加自定义转换器，处理null响应
        builder.addConverterFactory(GsonConverterFactory.create(getGson())); //02:添加Gson转换器,将规范的gson及解析成实体
        //builder.addConverterFactory(GsonConverterFactory.create());        //03:添加Gson转换器,将规范的gson及解析成实体
        //builder.addConverterFactory(JsonResultConvertFactory.create());    //04:自定义的json解析器处理不规范json
        //
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());//添加RxJavaCallAdapter,把Retrofit请求转化成RxJava的Observable

        return builder;
    }


    /**
     * 02 GET
     * <p>
     * 查看文件上传下载进度/结合progress使用
     */
    private Retrofit.Builder getFileBuilder(String apiUrl, FileProgressListener listener) {
//        MLog.d("Retrofit-->getFileBuilder");
        //retrofit配置
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.client(getFileOkHttp(listener));//设置okhttp（重点），不设置走默认的
        builder.baseUrl(apiUrl);//设置远程地址
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());//添加RxJavaCallAdapter,把Retrofit请求转化成RxJava的Observable

        //如下1--4 至少四选一
        //builder.addConverterFactory(new NullOnEmptyConverterFactory());      //01:添加自定义转换器，处理null响应
        //builder.addConverterFactory(GsonConverterFactory.create(getGson())); //02:添加Gson转换器,将规范的gson及解析成实体
        builder.addConverterFactory(GsonConverterFactory.create());        //03:添加Gson转换器,将规范的gson及解析成实体
        //builder.addConverterFactory(JsonResultConvertFactory.create());    //04:自定义的json解析器处理不规范json

        return builder;
    }

    /**
     * 03 文件上传
     * <p>
     */
    private Retrofit.Builder uploadBuilder(String apiUrl) {
//        MLog.d("Retrofit-->uploadBuilder");
        //retrofit配置
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.client(uploadOkHttp());//设置okhttp（重点），不设置走默认的
        builder.baseUrl(apiUrl);//设置远程地址
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());//添加RxJavaCallAdapter,把Retrofit请求转化成RxJava的Observable

        //如下1--4 至少四选一
        //builder.addConverterFactory(new NullOnEmptyConverterFactory());      //01:添加自定义转换器，处理null响应
        //builder.addConverterFactory(GsonConverterFactory.create(getGson())); //02:添加Gson转换器,将规范的gson及解析成实体
        builder.addConverterFactory(GsonConverterFactory.create());        //03:添加Gson转换器,将规范的gson及解析成实体
        //builder.addConverterFactory(JsonResultConvertFactory.create());    //04:自定义的json解析器处理不规范json

        return builder;
    }

    /**
     * 04 retrofit配置
     * （可用链式结构，但需要返回处理build()，就不用链式）
     * 特殊缓存接口使用
     */
    private Retrofit.Builder getNoCacheBuilder(String apiUrl) {

        //retrofit配置 可用链式结构
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.client(getNoCacheOkHttp());//设置okhttp3（重点），不设置走默认的
        builder.baseUrl(apiUrl);//设置远程地址

        //官方的json解析，要求格式必须规范才不会异常，但后台的不一定规范，这就要求自定义一个解析器避免这个情况
        builder.addConverterFactory(GsonConverterFactory.create());//将规范的gson及解析成实体
        //        builder.addConverterFactory(JsonResultConvertFactory.create());//自定义的json解析器


        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create()); //把Retrofit请求转化成RxJava的Observable
        return builder;
    }

    /**
     * 05 retrofit配置：header+token+无缓存
     * （可用链式结构，但需要返回处理build()，就不用链式）
     * 统一header请求头
     */
    private Retrofit.Builder getGETBuilder(String apiUrl) {

        //retrofit配置 可用链式结构
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.client(deGetOkHttp());//设置okhttp3（重点），不设置走默认的
        builder.baseUrl(apiUrl);//设置远程地址

        //官方的json解析，要求格式必须规范才不会异常，但后台的不一定规范，这就要求自定义一个解析器避免这个情况
        builder.addConverterFactory(GsonConverterFactory.create());//将规范的gson及解析成实体
        //        builder.addConverterFactory(JsonResultConvertFactory.create());//自定义的json解析器


        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create()); //把Retrofit请求转化成RxJava的Observable
        return builder;
    }


    //================================= okhttp构建 该处根据你的后台灵活设置====================================

    /**
     * 01 header+token+无缓存+utf-8
     *
     * @return
     */
    private OkHttpClient getDefaultOkhttp() {
        //log打印级别
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        try {
            //具体配置，可用链式结构
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
            okBuilder.readTimeout(60, TimeUnit.SECONDS);//读超时
            okBuilder.connectTimeout(60 * 1000, TimeUnit.MILLISECONDS);//链接超时
            okBuilder.writeTimeout(60, TimeUnit.SECONDS);//写超时
            okBuilder.addInterceptor(loggingInterceptor);//设置拦截器,打印// getInterceptor() 为默认的，现在改为自定义 loggingInterceptor
            okBuilder.addInterceptor(new Interceptor() {//设置统一请求头，由后台要求，灵活设置
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request okhttpRequest = chain.request();
                    TNSettings settings = TNSettings.getInstance();
                    String myToken = settings.token;//添加你的token

                    Request.Builder okhttpRequst = okhttpRequest.newBuilder()
                            .addHeader("user-agent", HttpHead.getHeader());
                    if (myToken != null) {
                        okhttpRequst.addHeader("session_token", myToken);// token 位置
                    }

                    return chain.proceed(okhttpRequst
                            // .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), URLDecoder.decode(okhttpRequest.body().toString(), "UTF-8")))
                            .build());
                }
            });
            okBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    MLog.d("HttpUtils", "hostname: " + hostname);
                    return true;
                }
            });

            return okBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 02 查看文件上传下载进度/结合progress使用
     * 不需要使用token
     *
     * @return
     */
    private OkHttpClient getFileOkHttp(FileProgressListener listener) {
//        MLog.d("OkHttpClient-->getFileOkHttp");
        //log打印级别
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        try {
            //具体配置，可用链式结构
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
            okBuilder.connectTimeout(60 * 1000, TimeUnit.MILLISECONDS);//链接超时
            okBuilder.retryOnConnectionFailure(true);
            okBuilder.addInterceptor(loggingInterceptor);//设置拦截器,打印// getInterceptor() 为默认的，现在改为自定义 loggingInterceptor
            okBuilder.addInterceptor(new FileProgressInterceptor(listener));//下载文件进度 拦截器

            return okBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 02 查看文件上传下载进度/结合progress使用
     * 不需要使用token
     *
     * @return
     */
    private OkHttpClient uploadOkHttp() {
//        MLog.d("OkHttpClient-->getFileOkHttp");
        //log打印级别
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        try {
            //具体配置，可用链式结构
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
            okBuilder.connectTimeout(60 * 1000, TimeUnit.MILLISECONDS);//链接超时
            okBuilder.retryOnConnectionFailure(true);
            okBuilder.addInterceptor(loggingInterceptor);//设置拦截器,打印// getInterceptor() 为默认的，现在改为自定义 loggingInterceptor
            okBuilder.addInterceptor(new Interceptor() {//设置统一请求头，由后台要求，灵活设置
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request okhttpRequest = chain.request();
                    TNSettings settings = TNSettings.getInstance();
                    String myToken = settings.token;//添加你的token

                    Request.Builder okhttpRequst = okhttpRequest.newBuilder()
                            .addHeader("user-agent", HttpHead.getHeader());
                    if (myToken != null) {
                        okhttpRequst.addHeader("session_token", myToken);// token 位置
                    }

                    return chain.proceed(okhttpRequst
//                             .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), URLDecoder.decode(okhttpRequest.body().toString(), "UTF-8")))//请求体格式设置
                            .build());
                }
            });
            return okBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 01 缓存
     * <p>
     * 如果服务端没有配合处理cache请求头，会抛出如下504异常,可以自定义cache策略：
     * onError:retrofit2.adapter.rxjava.HttpException: HTTP 504 Unsatisfiable Request (only-if-cached)
     *
     * @return
     */
    private OkHttpClient getCacheOkhttp() {
        //log打印级别
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        try {
            //具体配置，可用链式结构
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
            okBuilder.readTimeout(20, TimeUnit.SECONDS);//读超时
            okBuilder.connectTimeout(10 * 1000, TimeUnit.MILLISECONDS);//链接超时
            okBuilder.writeTimeout(20, TimeUnit.SECONDS);//写超时
            okBuilder.addInterceptor(new CacheInterceptor());//添加缓存拦截器
            okBuilder.addNetworkInterceptor(new CacheInterceptor());//添加网络缓存拦截器
            okBuilder.addInterceptor(loggingInterceptor);//设置拦截器,打印// getInterceptor() 为默认的，现在改为自定义 loggingInterceptor
            okBuilder.cache(cache);//设置缓存 自定义缓存路径
            okBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    MLog.d("HttpUtils", "hostname: " + hostname);
                    return true;
                }
            });

            return okBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 04 无缓存设置
     *
     * @return
     */
    private OkHttpClient getNoCacheOkHttp() {
        try {
            //具体配置，可用链式结构
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
            okBuilder.readTimeout(60, TimeUnit.SECONDS);
            okBuilder.connectTimeout(60, TimeUnit.SECONDS);
            okBuilder.writeTimeout(60, TimeUnit.SECONDS);
            okBuilder.addInterceptor(getInterceptor());//设置拦截器,打印

            return okBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 05 GET方式调用：token+header+无缓存
     *
     * @return
     */
    private OkHttpClient deGetOkHttp() {
        //log打印级别
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        try {
            //具体配置，可用链式结构
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
            okBuilder.readTimeout(60, TimeUnit.SECONDS);
            okBuilder.connectTimeout(60, TimeUnit.SECONDS);
            okBuilder.writeTimeout(60, TimeUnit.SECONDS);
            okBuilder.addInterceptor(new HttpCacheInterceptor());//公共缓存拦截器
            okBuilder.addInterceptor(loggingInterceptor);//设置拦截器,打印body
            okBuilder.addInterceptor(new Interceptor() {//设置统一请求头，由后台要求，灵活设置
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request okhttpRequest = chain.request();
                    TNSettings settings = TNSettings.getInstance();
                    String myToken = settings.token;//添加你的token

                    Request.Builder okhttpRequst = okhttpRequest.newBuilder()
                            .addHeader("user-agent", HttpHead.getHeader());
                    if (myToken != null) {
                        okhttpRequst.addHeader("session_token", myToken);// token 位置
                    }

                    return chain.proceed(okhttpRequst.build());
                }
            });
            okBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    MLog.d("HttpUtils", "hostname: " + hostname);
                    return true;
                }
            });

            return okBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //================================okhttp-缓存自定义相关======================================

    /**
     * 01 缓存拦截器，需要有缓存文件
     * <p>
     * 离线读取本地缓存，在线获取最新数据
     */

    class HttpCacheInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            //可添加token验证，oAuth验证，可用链式结构
            Request.Builder builder = request.newBuilder();
            builder.addHeader("Accept", "application/json;versions=1");
            if (CheckNetworkUtils.isNetworkConnected(context)) {//在线
                int maxAge = 60;//缓存时间
                builder.addHeader("Cache-Control", "public, max-age=" + maxAge);//设置请求的缓存时间
                MLog.e("sjy-cache", "在线缓存在1分钟内可读取原接口数据");
            } else {//离线
                int maxStale = 60 * 60 * 24 * 28;// tolerate 4-weeks stale
                builder.addHeader("Cache-Control", "public, only-if-cached, max-stale=" + maxStale);
            }
            // 可添加token
            //            if (listener != null) {
            //                builder.addHeader("token", listener.getToken());
            //            }
            // 如有需要，添加请求头
            //            builder.addHeader("a", HttpHead.getHeader(request.method()));
            return chain.proceed(builder.build());
        }
    }

    /**
     * 02 设置缓存拦截器
     */
    class CacheInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();
            if (CheckNetworkUtils.isNetworkConnected(context)) {//在线缓存
                Response response = chain.proceed(request);
                int maxAge = 60; // 在线缓存在6s内可读取
                String cacheControl = request.cacheControl().toString();
                MLog.e("sjy-cache", "在线缓存在1分钟内可读取" + cacheControl);
                return response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {//离线缓存
                MLog.e("sjy-cache", "离线时缓存时间设置");
                request = request.newBuilder()
                        .cacheControl(FORCE_CACHE1)//此处设置了7秒---修改了默认系统方法，不能用默认的CacheControl.FORCE_CACHE--是int型最大值，就相当于断网的情况下，一直不清除缓存
                        .build();

                Response response = chain.proceed(request);
                //下面注释的部分设置也没有效果，因为在上面已经设置了
                return response.newBuilder()
                        //                        .removeHeader("Pragma")
                        //                        .removeHeader("Cache-Control")
                        //                        .header("Cache-Control", "public, only-if-cached, max-stale=50")
                        .build();
            }
        }
    }

    //---修改了系统方法--这是设置在多长时间范围内获取缓存里面
    public static final CacheControl FORCE_CACHE1 = new CacheControl.Builder()
            .onlyIfCached()
            .maxStale(60 * 60 * 24 * 28, TimeUnit.SECONDS)//这里是60 * 60 * 24 * 28s，CacheControl.FORCE_CACHE--是int型最大值
            .build();
    //================================okhttp-log自定义相关======================================

    /**
     * 设置log打印拦截器
     *
     * 可以通过 setLevel 改变日志级别,共包含四个级别：NONE、BASIC、HEADER、BODY
     * NONE 不记录
     * BASIC 请求/响应行
     * HEADERS 请求/响应行 + 头
     * BODY 请求/响应行 + 头 + 体
     */
    private HttpLoggingInterceptor getInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //
        /**

         */
        if (debug) {
            // 打印okhttp
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 测试
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE); // 打包
        }
        return interceptor;
    }
//================================okhttp-gson自定义相关======================================

    // 01自定义gson处理
    private Gson getGson() {
//        Log.d(TAG, "getGson: HttpUtils走gson转换方法");
        if (gson == null) {
            GsonBuilder builder = new GsonBuilder();
            builder.setLenient();
            builder.setFieldNamingStrategy(new AnnotateNaming());
            builder.serializeNulls();
            gson = builder.create();
        }
        return gson;
    }

    private static class AnnotateNaming implements FieldNamingStrategy {
        @Override
        public String translateName(Field field) {
            ParamNames a = field.getAnnotation(ParamNames.class);
            return a != null ? a.value() : FieldNamingPolicy.IDENTITY.translateName(field);
        }
    }
}
