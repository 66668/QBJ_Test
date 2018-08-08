#轻笔记大改说明：

本版本使用MVP+(Retrofit+Okhttp+Rxjava)网络框架，同步的大块串行接口，如有bug,请参考老版源码,新版已完全删除

具体同步位置（7个act）：

1:三个fragment

2:TNMainAct

3:TNNoteListAct有两套同步块

4：TNPagerAct

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

下载2张图片逻辑顺序：
 下载第一个图片响应---》更新att数据库（哪里操作？）-->回调----》下一个循环
 
 ##error说明：
 （1）ANR异常：非按键事件，事件等待队列不为空且头事件分发超时500ms
 Input dispatching timed out (com.thinkernote.ThinkerNote/com.thinkernote.ThinkerNote.Activity.TNPagerAct, Waiting to send non-key event because the touched window has not finished processing certain input events that were delivered to it over 500.0ms ago.  Wait queue length: 15.  Wait queue head age: 9187.5ms
 
（2）2-11-2异常
mGetNoteByNoteId 异常onError:java.util.regex.PatternSyntaxException: Incorrectly nested parentheses in regexp pattern near index 0


mGetNoteByNoteId 异常onError:java.net.UnknownHostException: Unable to resolve host "s.qingbiji.cn": No address associated with hostname
权限每丢失的情况下 断网了

 （3）2-11-2异常： java.util.regex.PatternSyntaxException: In a character range [x-y], x is greater than y near index 43
 原因：解析html转换成String的时候，转换异常引起
 
#数据库优化说明：
1。本app涉及大量数据插入查询，所以使用Transaction机制最合适。
2。耗时操作用handlerThread异步，
3。

#内存泄漏分析：
无法解析的返回
{"msg":"ok","note":{"attachments":[],"tags":[],"folder_id":31931555,"id":37879967,"size":8225,"accept_at":"2018-08-08 10:54:44","title":"\u70c2\u7b14\u5934\u57df\u540d 2012-03-14 22:11:06","update_at":"2018-08-08 10:53:02","create_at":"2018-08-08 10:53:02","summary":"\n\n\n\u57df\u540dDomainName\nlanbitou.com\u00a0\u00a0\u8bbf\u95ee\u8be5\u7f51\u7ad9\n\n\n\u57df\u540d\u72b6\u6001(\u8fd9\u662f\u4ec0\u4e48?)Domain Status\nok\n\n\n\u6ce8\u518c\u5546Sponsoring Registrar\n\u5317\u4eac\u4e07\u7f51\u5fd7\u6210\u79d1\u6280","content":"<html><body><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width: 100%;\">\n<tbody>\n<tr>\n<td class=\"info_text15\" style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 12px; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\" width=\"27%\"><strong style=\"font-weight: bold;\">\u57df\u540d<\/strong><br\/>DomainName<\/td>\n<td style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 1.5em; color: #333333; border-style: initial; border-color: initial; height: 26px; white-space: pre-wrap; word-break: break-all; border-width: 0px; padding: 5px; margin: 0px;\" width=\"73%\">lanbitou.com\u00a0\u00a0<a class=\"linkblue_query\" href=\"http:\/\/www.lanbitou.com\/\" style=\"text-decoration: underline; color: #48607a;\" target=\"_blank\">\u8bbf\u95ee\u8be5\u7f51\u7ad9<\/a><\/td>\n<\/tr>\n<tr bgcolor=\"#F3F3F3\">\n<td class=\"info_text15\" style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 12px; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\"><strong style=\"font-weight: bold;\">\u57df\u540d\u72b6\u6001<\/strong>(<a class=\"linkblue_query\" href=\"http:\/\/www.net.cn\/service\/faq\/yuming\/ymzc\/200708\/2462.html\" style=\"text-decoration: underline; color: #48607a;\" target=\"_blank\">\u8fd9\u662f\u4ec0\u4e48?<\/a>)<br\/>Domain Status<\/td>\n<td style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 1.5em; color: #333333; border-style: initial; border-color: initial; height: 26px; cursor: pointer; border-width: 0px; padding: 5px; margin: 0px;\"><span title=\"\u6b63\u5e38\u72b6\u6001\">ok<\/span><\/td>\n<\/tr>\n<tr>\n<td class=\"info_text15\" style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 12px; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\"><strong style=\"font-weight: bold;\">\u6ce8\u518c\u5546<\/strong><br\/>Sponsoring Registrar<\/td>\n<td style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 1.5em; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\">\u5317\u4eac\u4e07\u7f51\u5fd7\u6210\u79d1\u6280\u6709\u9650\u516c\u53f8<\/td>\n<\/tr>\n<tr bgcolor=\"#F3F3F3\">\n<td class=\"info_text15\" style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 12px; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\"><strong style=\"font-weight: bold;\">DNS \u670d\u52a1\u5668<\/strong><br\/>Name Server<\/td>\n<td style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 1.5em; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\">DNS10.HICHINA.COM, DNS9.HICHINA.COM<\/td>\n<\/tr>\n<tr>\n<td class=\"info_text15\" style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 12px; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\"><strong style=\"font-weight: bold;\">\u6ce8\u518c\u65e5\u671f<\/strong><br\/>Registration Date<\/td>\n<td style=\"font-family: \u5b8b\u4f53, Arial, Verdana
    ; font-size: 12px; line-height: 1.5em; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\">14-oct-2005<\/td>\n<\/tr>\n<tr bgcolor=\"#F3F3F3\">\n<td class=\"info_text15\" style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 12px; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\"><strong style=\"font-weight: bold;\">\u5230\u671f\u65e5\u671f<\/strong><br\/>Expiration Date<\/td>\n<td style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 1.5em; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\">14-oct-2015<\/td>\n<\/tr>\n<tr>\n<td colspan=\"2\" style=\"font-family: \u5b8b\u4f53, Arial, Verdana; font-size: 12px; line-height: 1.5em; color: #333333; border-style: initial; border-color: initial; height: 26px; border-width: 0px; padding: 5px; margin: 0px;\">\n<div class=\"linkblue_query\" style=\"border-style: initial; border-color: initial; outline-width: 0px; outline-style: initial; outline-color: initial; font-weight: inherit; font-style: inherit; font-family: inherit; text-align: left; vertical-align: baseline; color: #48607a; text-decoration: underline; cursor: pointer; border-width: 0px; padding: 0px; margin: 0px;\"><img alt=\"\" id=\"img1\" src=\"http:\/\/www.net.cn\/static\/member\/images\/icon_close.gif\" style=\"border-style: initial; border-color: initial; outline-width: 0px; outline-style: initial; outline-color: initial; font-weight: inherit; font-style: inherit; font-family: inherit; text-align: left; vertical-align: middle; border-width: 0px; padding: 0px; margin: 0px;\"\/>\u00a0<strong style=\"font-weight: bold;\">\u67e5\u770b\u5b8c\u6574\u6ce8\u518c\u4fe1\u606f<\/strong><\/div>\n<div id=\"tab1\" style=\"border-style: initial; border-color: initial; outline-width: 0px; outline-style: initial; outline-color: initial; font-weight: inherit; font-style: inherit; font-family: inherit; text-align: left; vertical-align: baseline; border-width: 0px; padding: 0px; margin: 0px;\"><br\/><a class=\"linkblue_query\" href=\"http:\/\/www.net.cn\/service\/faq\/yuming\/ymzc\/201108\/4968.html\" style=\"text-decoration: underline; color: #48607a;\" target=\"_blank\">\u4e0d\u61c2\u4ee5\u4e0b\u6ce8\u518c\u4fe1\u606f\u4e2d\u7684\u82f1\u6587\u9879\u542b\u4e49\uff1f<\/a><br\/>\n<div class=\"dbody\" style=\"border-style: initial; border-color: initial; outline-width: 0px; outline-style: initial; outline-color: initial; font-weight: inherit; font-style: inherit; font-family: inherit; text-align: left; vertical-align: baseline; width: 620px; border-width: 0px; padding: 0px; margin: 0px;\">\n<pre style=\"white-space: pre-wrap; word-wrap: break-word;\">Domain Name ..................... lanbitou.com\nName Server ..................... dns9.hichina.com\n                                  dns10.hichina.com\nRegistrant ID ................... hc961508640-cn\nRegistrant Name ................. Guobin Chang\nRegistrant Organization ......... GuobinChang\nRegistrant Address .............. No.26-1-2-14C Longhai Middle Road,Zhengzhou,PRC\nRegistrant City ................. zhengzhoushi\nRegistrant Province\/State ....... henan\nRegistrant Postal Code .......... 450052\nRegistrant Country Code ......... CN\nRegistrant Phone Number ......... +86.03718237130 - \nRegistrant Fax .................. +86. - \nRegistrant Email ................ changgb@hotmail.com\nAdministrative ID ............... hc961508640-cn\nAdministrative Name ............. Guobin Chang\nAdministrative Organization ..... GuobinChang\nAdministrative Address .......... No.26-1-2-14C Longhai Middle Road,Zhengzhou,PRC\nAdministrative City ............. zhengzhoushi\nAdministrative Province\/State ... henan\nAdministrative Postal Code ...... 450052\nAdministrative Country Code ..... CN\nAdministrative Phone Number ..... +86.0371823713
    0 - \nAdministrative Fax .............. +86. - \nAdministrative Email ............ changgb@hotmail.com\nBilling ID ...................... hichina001-cn\nBilling Name .................... hichina\nBilling Organization ............ HiChina Web Solutions Limited\nBilling Address ................. 3\/F., HiChina Mansion\n                                  No.27 Gulouwai Avenue\n                                  Dongcheng District\nBilling City .................... Beijing\nBilling Province\/State .......... Beijing\nBilling Postal Code ............. 100011\nBilling Country Code ............ CN\nBilling Phone Number ............ +86.01064242299 - \nBilling Fax ..................... +86.01064258796 - \nBilling Email ................... domainadm@hichina.com\nTechnical ID .................... hichina001-cn\nTechnical Name .................. hichina\nTechnical Organization .......... HiChina Web Solutions Limited\nTechnical Address ............... 3\/F., HiChina Mansion\n                                  No.27 Gulouwai Avenue\n                                  Dongcheng District\nTechnical City .................. Beijing\nTechnical Province\/State ........ Beijing\nTechnical Postal Code ........... 100011\nTechnical Country Code .......... CN\nTechnical Phone Number .......... +86.01064242299 - \nTechnical Fax ................... +86.01064258796 - \nTechnical Email ................. domainadm@hichina.com\nExpiration Date ................. 2015-10-14 11:07:35<\/pre>\n<\/div>\n<\/div>\n<\/td>\n<\/tr>\n<\/tbody>\n<\/table><\/body><\/html>","content_digest":"84ACD8178B5F40C336A9F4B6C398C540","access_times":2,"trash":0},"code":0}

