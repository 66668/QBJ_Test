
#重要说明：
本app的网络框架Okhttp第一次搭建，所以没有优化View层的封装，导致View层的网络逻辑比较复杂，影响View层的单一性。

修改建议：最好将view层的网络逻辑从View层中移出去。
#轻笔记升级的两个方向：
android的AyncTask类升级了四次，导致老的AyncTask已经无法调用
（1）最方便的做法是将老版的AyncTask及相关类集成jar包，替换成jar包的类
（2）还一种做法比较麻烦，技术不好容易出错，但是后续优化方便，就是换新的网络框架（前提是对源码了解透彻），有助于学习前人技术思想
经决定，本人使用后者，而且版本升级，涉及的不仅仅是AyncTask，还有权限，文件调用等各种问题。


#轻笔记大改说明：

本版本使用MVP+(Retrofit+Okhttp+Rxjava)网络框架，app中的for循环调用接口，改成position标记list的串行接口，（mainAct即可看到）同步的大块串行接口如有bug,请参考老版源码,新版已完全删除

具体同步位置（7个地方）：

1:三个fragment

2:TNMainAct

3:TNNoteListAct有两套同步块

4：TNPagerAct

5:讯飞语音优化so库

##本人修改没有注意的地方：
1没有添加back键中断网络操作
2笔记详情:下载文件的地方，没有写单击下载文件的提示，回调写的比较潦草（缺少单击回调）
3.
##本人认为最值得借鉴的地方：
1。数据库比较好，没有用Cursor,使用的Transaction机制
2。文件的选择打开样式
3。反射调用很经典，比如：多个act调用同一个接口，写一个接口方法，用反射调用就可以解决，节省不少代码量，但是，不容易找到引用位置，需要好好研究
##轻笔记app弊端：
无法解析web的表格页面

###需要优化

1forResult的RequestID没有修改（可修改bug）

2数据库比较乱，本次未修改，但是需要优化成一个数据库---sjy 0713

3.百度地图开源账号：行客记事/Qunbiji2015 (目前没有定位正确)

##参照旧源码说明：

1.新框架使用说明：
新增包1：_constructer，具体的mvp的操作，

新增包2：_interface,mvp的回调

新增包3：bean,各种接口返回数据的封装

新增包4：http，里头是Okhttp3+retrofit2+Rxjava的封装，最新网络框架

新框架MVP的使用说明：

每一个界面（eg：TNMainAct），都有对应的m v p的类，_interface包封装的是main的抽象接口（调用传递-->p,调用接口-->m，接口回调-->v），用于具体实现
在_constructer包和Activity包（act是V层的具体实现）中,具体显示更新ui在v的Activity中实现，具体接口http的封装使用在m的module类中使用。

2.旧框架说明：

不用包1：Action（但是数据库仍使用，无法根除，后续必删）

不用包2：NetWork（已删除，httpclient类封装）

不用包3:OAuth2 网络框架太老，不可用（已删除，httpclient类封装）


###其他说明：

1.Uri.fromFile(new File(temp))获取Uri的样式在android7.0+后，改成FileProvider.getUriForFile()样式，具体参考本app

2.android4.2以后，任何为JS暴露的接口，都需要加@JavascriptInterface

 
 ##error说明：
 

#数据库优化说明：
1。本app涉及大量数据插入查询，所以使用Transaction机制最合适。
2。耗时操作用handlerThread异步，否则app ANR
3。

#内存泄漏分析：
2-11-2异常
mGetNoteByNoteId 异常onError:java.util.regex.PatternSyntaxException: Incorrectly nested parentheses in regexp pattern near index 0


mGetNoteByNoteId 异常onError:java.net.UnknownHostException: Unable to resolve host "s.qingbiji.cn": No address associated with hostname
权限每丢失的情况下 断网了

2-11-2异常： java.util.regex.PatternSyntaxException: In a character range [x-y], x is greater than y near index 43
 原因：解析html转换成String的时候，转换异常引起
 
 
2-11-2oom异常：本人注释掉了 不影响显示
 
   java.lang.OutOfMemoryError: Failed to allocate a 272911368 byte allocation with 25165824 free bytes and 111MB until OOM, max allowed footprint 176454560, growth limit 268435456
         at java.util.Arrays.copyOf(Arrays.java:3260)
         at java.lang.AbstractStringBuilder.ensureCapacityInternal(AbstractStringBuilder.java:125)
         at java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:660)
         at java.lang.StringBuffer.append(StringBuffer.java:381)
         at java.util.regex.Matcher.appendEvaluated(Matcher.java:674)
         at java.util.regex.Matcher.appendReplacement(Matcher.java:633)
         at java.util.regex.Matcher.replaceAll(Matcher.java:750)
         at java.lang.String.replaceAll(String.java:2216)
         at com.thinkernote.ThinkerNote.General.TNUtilsHtml.getPlainText2(TNUtilsHtml.java:183)
         at com.thinkernote.ThinkerNote.General.TNUtilsHtml.codeHtmlContent(TNUtilsHtml.java:78)
         at com.thinkernote.ThinkerNote.Activity.TNMainAct.updateNote(TNMainAct.java:841)
         at com.thinkernote.ThinkerNote.Activity.TNMainAct$8.handleMessage(TNMainAct.java:713)
         at android.os.Handler.dispatchMessage(Handler.java:101)
         at android.os.Looper.loop(Looper.java:164)
         at android.os.HandlerThread.run(HandlerThread.java:65)
 
#Okhttp+Rxjava关于手动back键关闭网络连接的代码提示：

在具体的module中eg:

                 Subscription subscription = MyHttpService.Builder.getHttpServer()//固定样式，可自定义其他网络
                .syncNewNoteAdd(note.title, content, note.tagStr, note.catId, note.createTime, note.lastUpdate, note.lbsLongitude, note.lbsLatitude, note.lbsAddress, note.lbsRadius, settings.token)//接口方法
                .subscribeOn(Schedulers.io())//固定样式
                .unsubscribeOn(Schedulers.io())//固定样式
                .observeOn(AndroidSchedulers.mainThread())//固定样式
                .subscribe(new Observer<OldNoteAddBean>() {//固定样式，可自定义其他处理
                    @Override
                    public void onCompleted() {
                        MLog.d(TAG, "mNewNote--onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MLog.e("mNewNote 异常onError:" + e.toString());
                        listener.onSyncNewNoteAddFailed("异常", new Exception("接口异常！"), position, arraySize);
                    }

                    @Override
                    public void onNext(OldNoteAddBean bean) {
                        MLog.d(TAG, "mNewNote-onNext");

                        //处理返回结果
                        if (bean.getCode() == 0) {
                            listener.onSyncNewNoteAddSuccess(bean, position, arraySize, isNewDb);
                        } else {
                            listener.onSyncNewNoteAddFailed(bean.getMessage(), null, position, arraySize);
                        }
                    }

                });
  subscribe返回对象Subscription 
  通过如下代码 中断该次连接
         
         
          if (subscription != null && !subscription.isUnsubscribed()) {
              subscription.unsubscribe();
          }         


#解决8.0打开安装包失败的代码
act中设置：下载文件后调用checkIsAndroidO()方法即可，然后执行如下代码就完美解决


    /**
     * 判断是否是8.0,8.0需要处理未知应用来源权限问题,否则直接安装
     */
    private void checkIsAndroidO() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (b) {
                TNUtilsUi.openFile(this, installFile);//安装应用的逻辑(写自己的就可以)
            } else {
                //请求安装未知应用来源的权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 10001);
            }
        } else {
            TNUtilsUi.openFile(this, installFile);
        }

    } 
    
    
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10001:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MLog.d("4");
                    TNUtilsUi.openFile(this, installFile);
                } else {
                    //打开未知安装许可
                    Uri packageURI = Uri.parse("package:" + getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                    startActivityForResult(intent, 10002);
                }
                break;
        }
    }
    
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10002:
                MLog.d("6");
                checkIsAndroidO();
                break;
            default:
                break;
        }
    }
    
 
 ## 讯飞语音
 nill.chen@thinkernote.com /Qunbijixxxx   
 
 ## 接口异常：
 delete接口正确写法：
 
     @FormUrlEncoded
     @HTTP(method = "DELETE", path = URLUtils.Note.TAG, hasBody = true)
     Observable<CommonBean> deleteTag(@Field("tag_id") long tag_id
             , @Field("session_token") String session_token);