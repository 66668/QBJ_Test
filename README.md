#轻笔记大改说明：

本版本使用MVP+(Retrofit+Okhttp+Rxjava)网络框架，同步的大块串行接口，如有bug,请参考老版源码,新版已完全删除

具体同步位置（7个act）：

1:三个fragment

2:TNMainAct

3:TNNoteListAct有两套同步块

4：TNPagerAct

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

下载2张图片逻辑顺序：
 下载第一个图片响应---》更新att数据库（哪里操作？）-->回调----》下一个循环

