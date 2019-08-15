package com.thinkernote.ThinkerNote.Data;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.thinkernote.ThinkerNote.General.TNConst;
import com.thinkernote.ThinkerNote.General.TNSettings;
import com.thinkernote.ThinkerNote.General.TNUtils;
import com.thinkernote.ThinkerNote.General.TNUtilsHtml;
import com.thinkernote.ThinkerNote.Service.LocationService;
import com.thinkernote.ThinkerNote.Utils.MLog;

import java.io.Serializable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TNNote implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String TAG = "TNNoteData";

    public static final int BRIEFCONTENTLENGTH = 200;
    public static final int PINGYININDEX = 32;

    public long noteLocalId;
    public String title;
    public int syncState;//1表示未完全同步，2表示完全同步，3表示本地新增，4表示本地编辑，5表示彻底删除，6表示删除到回收站，7表示从回收站还原

    public long creatorUserId;
    public String creatorNick;
    public long catId;
    public String content;
    public String shortContent;
    public String contentDigest;
    public int trash;
    public String source;
    public int createTime;
    public int lastUpdate;

    public String thumbnail;
    public Drawable thmDrawable;

    //location info
    public int lbsLongitude;
    public int lbsLatitude;
    public int lbsRadius;
    public String lbsAddress;

    public Vector<Long> tags;
    public String tagStr;

    public int attCounts;
    public Vector<TNNoteAtt> atts;
    public TNNoteAtt currentAtt;

    public long noteId;
    public long revision;

    public TNNote originalNote;

    public String richText;
    public Vector<Integer> mapping;
//

    @Override
    public String toString() {
        return "TNNote{" +
                "noteLocalId=" + noteLocalId +
                ", title='" + title + '\'' +
                ", syncState=" + syncState +
                ", creatorUserId=" + creatorUserId +
                ", creatorNick='" + creatorNick + '\'' +
                ", catId=" + catId +
                ", content='" + content + '\'' +
                ", shortContent='" + shortContent + '\'' +
                ", contentDigest='" + contentDigest + '\'' +
                ", trash=" + trash +
                ", source='" + source + '\'' +
                ", createTime=" + createTime +
                ", lastUpdate=" + lastUpdate +
                ", thumbnail='" + thumbnail + '\'' +
                ", thmDrawable=" + thmDrawable +
                ", lbsLongitude=" + lbsLongitude +
                ", lbsLatitude=" + lbsLatitude +
                ", lbsRadius=" + lbsRadius +
                ", lbsAddress='" + lbsAddress + '\'' +
                ", tags=" + tags +
                ", tagStr='" + tagStr + '\'' +
                ", attCounts=" + attCounts +
                ", atts=" + atts +
                ", currentAtt=" + currentAtt +
                ", noteId=" + noteId +
                ", revision=" + revision +
                ", originalNote=" + originalNote +
                ", richText='" + richText + '\'' +
                ", mapping=" + mapping +
                '}';
    }

    public static TNNote newNote() {
        MLog.d(TAG, "newNote");
        TNNote note = new TNNote();
        note.noteLocalId = -1;
        note.title = "";
        note.syncState = 1;
        note.creatorUserId = TNSettings.getInstance().userId;
        note.catId = -1;
        note.content = "";
        note.shortContent = "";
        note.trash = 0;
        note.contentDigest = "";
        note.thumbnail = "";

        note.createTime = (int) (System.currentTimeMillis() / 1000);
        note.lastUpdate = (int) (System.currentTimeMillis() / 1000);

        note.tags = new Vector<Long>();
        note.tagStr = "";

        note.attCounts = 0;
        note.atts = new Vector<TNNoteAtt>();

        note.noteId = -1;
        note.revision = -1;

        note.mapping = new Vector<Integer>();

//		BDLocation location = TNLBSService.getInstance().getLocation();
        BDLocation location = LocationService.getInstance().getLocation();
        note.lbsLongitude = TNUtils.doubleToInt(location.getLongitude(), 1000000);
        note.lbsLatitude = TNUtils.doubleToInt(location.getLatitude(), 1000000);
        note.lbsRadius = TNUtils.doubleToInt(location.getRadius(), 10000);
        note.lbsAddress = location.getAddrStr();

        note.shortContent = "";
        note.creatorNick = TNSettings.getInstance().username;
        note.source = TNSettings.getInstance().username;

        return note;
    }

    public boolean isEditable() {
        MLog.d(TAG, "isEditable:" + content.length() + "|" + atts.size());
        String str = content;
        if (atts.size() > 0) {
            int f = str.indexOf("<p><tn-media");
            if (f < 0)
                return false;

            if (str.length() - f != 59 * atts.size())
                return false;

            // remove att part
            str = str.substring(0, f);
        }

        return !TNUtils.checkRegex(TNUtils.PLAINTEXT_REGEX, str);
    }

    public void resetTitle() {
        String defaultTitle = TNConst.DEFAULT_TITLE;
        if (title.equals(defaultTitle)) {
            String s = getPlainText().trim();
            if (s.length() >= 4) {
                int end = (s.length() >= 60) ? 60 : s.length();
                s = s.substring(0, end);
                String[] subs = s.split("[.?!。？！\n]");
                String first = subs.length > 0 ? subs[0] : s;
                if (first.length() >= 4) {
                    int e = (first.length() >= 30) ? 30 : first.length();
                    title = first.substring(0, e);
                } else {
                    s = s.replaceAll("\n", "");
                    if (s.length() >= 4) {
                        int e = (s.length() >= 30) ? 30 : s.length();
                        title = s.substring(0, e);
                    } else {
                        String date = TNUtils.formatDateToDay(System.currentTimeMillis());
                        if (atts.size() > 0) {
                            TNNoteAtt att = atts.get(0);
                            title = date + getAttTypeString(att.type);
                        } else {
                            title = date + "笔记";
                        }
                    }
                }
            } else {
                String date = TNUtils.formatDateToDay(System.currentTimeMillis());
                if (atts.size() > 0) {
                    TNNoteAtt att = atts.get(0);
                    title = date + getAttTypeString(att.type);
                } else {
                    title = date + "笔记";
                }
            }
        }
    }

    public boolean isModified() {
        boolean isModified = true;
        if (noteLocalId == -1) {
            // new note
            if ((title.length() == 0
                    || title.equals(TNConst.DEFAULT_TITLE))
                    && content.length() == 0
                    && tagStr.length() == 0
                    && atts.size() == 0
            )
                isModified = false;
        } else if (originalNote != null) {
            // edit
            if (title.equals(originalNote.title)
                    && content.equals(originalNote.getPlainText())
                    && tagStr.equals(originalNote.tagStr)
                    && atts.equals(originalNote.atts)
                    && catId == originalNote.catId)
                isModified = false;
        } else if (originalNote != null) {
            // edit rich text
            if (title.equals(originalNote.title)
                    && richText.equals(originalNote.content)
                    && tagStr.equals(originalNote.tagStr)
                    && atts.equals(originalNote.atts)
                    && catId == originalNote.catId)
                isModified = false;
        } else {//新笔记保存时，同时点返回，可能产生noteLocalId>0并且originalNote=null的情况
            isModified = false;
        }

        MLog.i(TAG, "isModified " + noteLocalId + originalNote + isModified);
        return isModified;
    }

    public String getAttTypeString(int type) {
        String s = "";
        if (type > 10000 && type < 20000) {
            s = "图片";
        } else if (type > 20000 && type < 30000) {
            s = "录音";
        } else if (type > 30000 && type < 40000) {
            s = "视频";
        } else {
            s = "附件";
        }
        return s;
    }

    public String getPlainText() {
        String str = content;

        str = str.replaceAll("<br />", "\n");
        str = str.replaceAll("<br/>", "\n");

        int s = str.indexOf("<");
        int e = str.indexOf(">", s);
        while (s >= 0 && e > 0) {
            str = str.replace(str.substring(s, e + 1), "");
            s = str.indexOf("<");
            e = str.indexOf(">", s);
        }
        str = TNUtilsHtml.decodeHtml(str);

        return str;
    }

    public void prepareToSave() {
        StringBuilder sb = new StringBuilder();
        if (originalNote != null && !originalNote.isEditable()) {
            if (richText.length() > 0) {
                try {
                    sb.append(TNUtilsHtml.codeHtmlContent(richText, false));
                } catch (Exception e) {
                    e.printStackTrace();
                    //该异常只在TNMainAct的2-11-2的数据处理函数使用，这里只使用try catch 不处理即可 sjy
                    MLog.e("TNNote--prepareToSave:" + e.toString());
                }
            }
        } else {
            if (content.length() > 0) {
                sb.append(TNUtilsHtml.encodeHtml(content));
            }
        }

        // add new att
        for (TNNoteAtt att : atts) {
            if (att.attLocalId < 0 ||
                    (originalNote != null && originalNote.isEditable())) {
                sb.append(String.format("<p><tn-media hash=\"%s\" /></p>",
                        att.digest));
            }
        }

        // delete att
        if (originalNote != null) {
            for (TNNoteAtt att : originalNote.atts) {
                boolean isExists = false;
                for (TNNoteAtt att1 : atts) {
                    if (att.attLocalId == att1.attLocalId) {
                        isExists = true;
                        break;
                    }
                    isExists = false;
                }
                if (!isExists) {
                    String attStr = String.format("<tn-media hash=\"%s\" />",
                            att.digest);
                    int index = sb.indexOf(attStr);
                    if (index > 0) {
                        sb.replace(index, index + attStr.length(), "");
                    }
                }
            }
        }

        content = sb.toString();
        contentDigest = TNUtils.toMd5(content);
        shortContent = TNUtils.getBriefContent(content);
//		BDLocation location = TNLBSService.getInstance().getLocation();
        BDLocation location = LocationService.getInstance().getLocation();
        lbsLongitude = TNUtils.doubleToInt(location.getLongitude(), 1000000);
        lbsLatitude = TNUtils.doubleToInt(location.getLatitude(), 1000000);
        lbsRadius = TNUtils.doubleToInt(location.getRadius(), 10000);
        lbsAddress = location.getAddrStr();

        resetTitle();
        lastUpdate = (int) (System.currentTimeMillis() / 1000);

//        MLog.d(TAG, "content:" + content);
    }

    public String makeHtml(int width) {
        String htmlFormat =
                "<!DOCTYPE html>"
                        + "<html>"
                        + "<head>"
                        + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf8\" />"
                        + "<meta name=\"viewport\" content=\"width=device-width; minimum-scale=0.1; maximum-scale=4; initial-scale=1; user-scalable=1;\" />"
                        + "<script language=\"javascript\">"
                        + "var dots=\"..........\";"
                        + "var imgIndex = 0, isLoading = 0;"
                        + "function loading(){if(isLoading==0){isLoading=1;loadingGif();}}"
                        + "function loadingGif(){"
                        + "var imgs = document.getElementsByName(\"abcd\");"
                        + "if(imgs.length==0){isLoading=0;return;}"
                        + "for (var j = 0; j < imgs.length; j++)"
                        + "	imgs[j].innerHTML = \"下载中\" + dots.substring(0,imgIndex);"
                        + "imgIndex = (imgIndex+1 > 6) ? 0 : imgIndex + 1;"
                        + "setTimeout(\"loadingGif()\", 800);"
                        + "}"
                        + "function wave(id, html) {"
                        + "document.getElementById(id).innerHTML = html;"
                        + "}"
                        + "function waveImg(id, src) {"
                        + "document.getElementById(id).src = src;"
                        + "}"
                        + "function waveAddHtml(id, html2){"
                        + "document.getElementById(id).innerHTML += html2;"
                        + "}"
                        + "function waveComment(id, name, timer, content){"
                        + "}"
                        + "</script>"
                        + "<style type=\"text/css\">"
                        + "* {margin:0;padding:0}"
                        + "body {font:15px Helvetica;padding:10px;text-align:left;background-color:transparent;word-wrap:break-word;}"
                        + "div.tn_title {margin: 5.0px 0.0px 0.0px 0.0px; font: bold 16.0px 'Helvetica';}"
                        + "div.tn_tag {color:rgb(76,76,76); margin: 5.0px 0.0px 5.0px 0.0px; text-align: left; font: 12.0px 'Helvetica';}"
                        + "span.tn_time {margin: 0.0px 20.0px 0.0px 0.0px; text-align: left; font: 12.0px 'Helvetica';}"
                        + "div.tn_logo {margin: 0.0px 10.0px 0.0px 10.0px; text-align: right;}"
                        + "div.tn_note {font:15.0px Helvetica; text-align: left;word-wrap:break-word;}"
                        + "div.com_box {margin: 0;padding-bottom:3px;border-bottom:1px solid #e4e4e4;}"
                        + "div.com_header {height:15px;line-height:15px;margin: 3.0px 0.0px 0.0px 0.0px;}"
                        + "div.com_creator {float:left;font:12px Helvetica;line-height:15px;max-width:200px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;-webkit-text-overflow:ellipsis;margin:0.0px 5.0px 0.0px 5.0px;color:#4485d6;}"
                        + "div.com_timer {float:right;font:10px Helvetica;line-height:15px;margin: 0.0px 5.0px 0.0px 5.0px;color:#4485d6;}"
                        + "div.com_content {min-height:30px;margin: 0.0px 3.0px 0.0px 3.0px;}"
                        + "div.com_info {font:14.0px Helvetica; margin: 0.0px 3.0px 0.0px 3.0px; text-align: left;word-wrap:break-word;color:#656565;}"
                        + "div.com_comment{font:15.0px Helvetica; margin: 0px;padding-left:3px; text-align: left;background:#e4e4e4;}"
                        + "div.com_more{height:30px;line-height:30px;font:15.0px Helvetica;margin 0px;padding-top: 8.0px;text-align: center;border-top:1px solid #e4e4e4}"
                        + "hr.tn_hr1 {margin: 0.0px 0.0px 0.0px 0.0px; border: 1px dashed #aaa;}"
                        + "img {margin:0px; vertical-align:middle; max-width:314px; width:expression(document.body.clientWidth > 314 ? \"314px\": \"auto\" ); } "
                        + "</style>"
                        + "</head>"
                        + "<body>"
                        + "<div id=\"tnbody\">"
                        + "<div class=\"tn_title\">%s</div>"
                        + "<div class=\"tn_tag\"><span class=\"tn_time\">发布时间： %s</span><img src=\"file:///android_asset/tag.png\" height=14/>%s</div>"
                        + "<div class=\"tn_note\">%s</div>"
                        + "<div >%s</div>"    //sourceShowText
                        + "</div>"
                        + "</body>"
                        + "</html>";

        htmlFormat = htmlFormat.replaceAll("314", String.valueOf(width - 20));

        String mHtml = "";

        content = content.replaceAll("\n", "<br />");
        ;

        Pattern pattern = Pattern.compile(TNUtils.PHONE_REGEX);
        Matcher matcher = pattern.matcher(content);
        int start = 0;
        while (matcher.find()) {
            String find = matcher.group();
            // 在标签内则不处理
            int s = content.indexOf(">", matcher.end());
            int e = content.indexOf("<", matcher.end());
            if (s > 0 && (s < e || e < 0)) {
                continue;
            }

            // 替换
            String clean = find.replaceAll("\\D", "");
            //上面replace方法会替换掉原字符序列中所有与find字符串相同的字符序列，导致显示异常，现在采用重构字符串的方式替换
            mHtml = mHtml + content.substring(start, matcher.start()) +
                    "<a href=\"tel:" + clean + "\">" + find + "</a>";
            start = matcher.end();
        }
        if (start < content.length())
            mHtml = mHtml + content.substring(start, content.length());
        for (TNNoteAtt att : atts) {
            String s = String.format("<tn-media att-id=\"%s\" hash=\"%s\"></tn-media>", att.attId, att.digest);
            //web端复制粘贴同一个附件是，只会增加tn-media，而附件不会增加，故在此使用replaceAll替换相同的tn-media，以便能将所有tn-media显示出来
//            MLog.d(TAG, mHtml);
            mHtml = mHtml.replaceAll(s, replaceHtmlOfAtt(att));
            String s2 = String.format("<tn-media hash=\"%s\" />", att.digest);
            mHtml = mHtml.replaceAll(s2, replaceHtmlOfAtt(att));
            String s3 = String.format("<tn-media hash=\"%s\"></tn-media>", att.digest);
            mHtml = mHtml.replaceAll(s3, replaceHtmlOfAtt(att));
        }

        String sourceText = "";
        mHtml = String.format(htmlFormat, title, TNUtils.formatDateToWeeks(createTime), tagStr, mHtml, sourceText);
//        MLog.d(TAG, "html:" + mHtml);
        return mHtml;
    }

    private String replaceHtmlOfAtt(TNNoteAtt att) {
        MLog.d(TAG, "replaceHtmlOfAtt");
        String t = null;
        // file exist at local
        if (!TextUtils.isEmpty(att.path) && att.syncState != 1) {
            if (att.type > 10000 && att.type < 20000)
                t = String.format("<div id=\"%d\"><a onClick=\"window.demo.openAtt(%d)\"><img src=\"file://%s\" /></a></div>",
                        att.attLocalId, att.attLocalId, att.path);
            else if (att.type > 20000 && att.type < 30000)
                t = String.format("<div id=\"%d\"><a onClick=\"window.demo.openAtt(%d)\"><img src=\"file:///android_asset/audio.png\" /><br />%s(%s)</a></div>",
                        att.attLocalId, att.attLocalId, att.attName, (att.size * 100 / 1024) / 100f + "K");
            else if (att.type == 40001)
                t = String.format("<div id=\"%d\"><a onClick=\"window.demo.openAtt(%d)\"><img src=\"file:///android_asset/pdf.png\" /><br />%s(%s)</a></div>",
                        att.attLocalId, att.attLocalId, att.attName, (att.size * 100 / 1024) / 100f + "K");
            else if (att.type == 40002)
                t = String.format("<div id=\"%d\"><a onClick=\"window.demo.openAtt(%d)\"><img src=\"file:///android_asset/txt.png\" /><br />%s(%s)</a></div>",
                        att.attLocalId, att.attLocalId, att.attName, (att.size * 100 / 1024) / 100f + "K");
            else if (att.type == 40003 || att.type == 40010)
                t = String.format("<div id=\"%d\"><a onClick=\"window.demo.openAtt(%d)\"><img src=\"file:///android_asset/word.png\" /><br />%s(%s)</a></div>",
                        att.attLocalId, att.attLocalId, att.attName, (att.size * 100 / 1024) / 100f + "K");
            else if (att.type == 40005 || att.type == 40011)
                t = String.format("<div id=\"%d\"><a onClick=\"window.demo.openAtt(%d)\"><img src=\"file:///android_asset/ppt.png\" /><br />%s(%s)</a></div>",
                        att.attLocalId, att.attLocalId, att.attName, (att.size * 100 / 1024) / 100f + "K");
            else if (att.type == 40009 || att.type == 40012)
                t = String.format("<div id=\"%d\"><a onClick=\"window.demo.openAtt(%d)\"><img src=\"file:///android_asset/excel.png\" /><br />%s(%s)</a></div>",
                        att.attLocalId, att.attLocalId, att.attName, (att.size * 100 / 1024) / 100f + "K");
            else {
                t = String.format("<div id=\"%d\"><a onClick=\"window.demo.openAtt(%d)\"><img src=\"file:///android_asset/unknown.png\" /><br />%s(%s)</a></div>",
                        att.attLocalId, att.attLocalId, att.attName, (att.size * 100 / 1024) / 100f + "K");
            }
        }
        //TODO  is downloading 下载流程代码没有加对应的回调机制，需要补充
        else if (false) {//TNActionUtils.isDownloadingAtt(att.attId)
            t = String.format("<div id=\"%d\"><img name=\"loading\" src=\"file:///android_asset/download.png\" /><span name=\"abcd\"></span><br />%s(%s)</div>",
                    att.attLocalId, att.attName, (att.size * 100 / 1024) / 100f + "K");
        }
        // need download
        else {
            t = String.format("<div id=\"%d\"><a onClick=\"window.demo.downloadAtt(%d)\"><img id=\"img%d\" src=\"file:///android_asset/needdownload.png\" /><br />%s(%s)</a></div>",
                    att.attLocalId, att.attLocalId, att.attLocalId, att.attName, (att.size * 100 / 1024) / 100f + "K");
        }
        return t;
    }

    public TNNoteAtt getAttDataById(long aId) {
        for (TNNoteAtt att : atts) {
            if (att.attId == aId) {
                return att;
            }
        }
        return null;
    }

    public TNNoteAtt getAttDataByLocalId(long aLocalId) {
        for (TNNoteAtt att : atts) {
            if (att.attLocalId == aLocalId) {
                return att;
            }
        }
        return null;
    }

    public void setMappingAndPlainText() {
        try {
            richText = TNUtilsHtml.codeHtmlContent(content, true);
        } catch (Exception e) {
            e.printStackTrace();
            //该异常只在TNMainAct的2-11-2的数据处理函数使用，这里只使用try catch 不处理即可 sjy
            MLog.e("TNNote--setMappingAndPlainText:" + e.toString());
        }
        if (mapping == null) {
            mapping = new Vector<Integer>();
        }
        StringBuffer pText = new StringBuffer();
        char s1 = '<';
        char s2 = '>';
        int tag = 0;
        int len = richText.length();
        for (int i = 0; i < len; i++) {
            char c = richText.charAt(i);
            if (c == s1) {
                tag += 1;
            } else if (c == s2) {
                tag -= 1;
            } else {
                if (tag == 0) {
                    mapping.add(i);
                    pText.append(c);
                }
            }
        }

        try {
            content = TNUtilsHtml.de(pText.toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        content = content.replaceAll("&lt;", "<");
        content = content.replaceAll("&gt;", ">");
    }

}
