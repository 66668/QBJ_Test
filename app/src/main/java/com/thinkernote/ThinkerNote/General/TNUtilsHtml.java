package com.thinkernote.ThinkerNote.General;

import android.text.TextUtils;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.Utils.MLog;

import org.apache.commons.lang3.StringUtils;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TNUtilsHtml {
    private static final String TAG = "TNUtilsHtml";

    public static String encodeHtml(String str) {
        String s = new String(str);
        s = s.replaceAll("&", "&amp;");

        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("'", "&apos;");

        s = s.replaceAll(" ", "&nbsp;");
        s = s.replaceAll("\n", "<br />");
        s = s.replaceAll("\"", "&quot;");
        return s;
    }

    public static String decodeHtml(String str) {
        String s = new String(str);
        s = s.replaceAll("&lt;", "<");
        s = s.replaceAll("&gt;", ">");
        s = s.replaceAll("&quot;", "\"");
        s = s.replaceAll("&apos;", "'");

        s = s.replaceAll("&nbsp;", " ");
        s = s.replaceAll("<br />", "\n");
        s = s.replaceAll("<br/>", "\n");

        s = s.replaceAll("&amp;", "&");
        return s;
    }

    public static String de(String str) {
        String s = new String(str);
        s = s.replaceAll("&quot;", "\"");
        s = s.replaceAll("&apos;", "'");
        s = s.replaceAll("&nbsp;", " ");
        s = s.replaceAll("&amp;", "&");
        s = s.replaceAll("<br />", "\n");
        s = s.replaceAll("<br/>", "\n");
        return s;
    }

    public static String en(String str) throws Exception {
        String s = new String(str);
//		s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll(" ", "&nbsp;");
        s = s.replaceAll("'", "&apos;");
        s = s.replaceAll("\n", "<br />");
        s = s.replaceAll("\"", "&quot;");

        return s;
    }

    /**
     * <p>
     * html转string
     * isDecode  true decode, false endecode
     */
    public static String codeHtmlContent(String htmlContent, boolean isDecode) throws Exception {

        StringBuffer contentbf = new StringBuffer();
        StringBuffer tmpbf = new StringBuffer();

        try {
            //处理html标签
            String htmlContent0 = getPlainText2(htmlContent);
            String rText = new String(htmlContent0);

            if (isDecode) {
                rText = rText.replaceAll("  <br />  ", "\n");
                rText = rText.replaceAll("  <br/>  ", "\n");
            } else {
                rText = rText.replaceAll("\n", "<br />");
            }

            char s1 = '<';
            char s2 = '>';
            int tag = 0;
            int len = rText.length();
            for (int i = 0; i < len; i++) {
                char c = rText.charAt(i);
                if (c == s1) {
                    if (tmpbf.length() > 0) {
                        if (isDecode) {
                            contentbf.append(TNUtilsHtml.de(tmpbf.toString()));
                        } else {
                            contentbf.append(TNUtilsHtml.en(tmpbf.toString()));
                        }
                        tmpbf = new StringBuffer();
                    }
                    contentbf.append(c);
                    tag += 1;
                } else if (c == s2) {
                    if (tmpbf.length() > 0) {
                        if (isDecode) {
                            contentbf.append(TNUtilsHtml.de(tmpbf.toString()));
                        } else {
                            contentbf.append(TNUtilsHtml.en(tmpbf.toString()));
                        }
                        tmpbf = new StringBuffer();
                    }
                    contentbf.append(c);
                    tag -= 1;
                } else {
                    if (tag == 0) {
                        tmpbf.append(c);
                    } else {
                        contentbf.append(c);
                    }
                }
            }
            if (tmpbf.length() > 0) {
                if (isDecode) {
                    contentbf.append(TNUtilsHtml.de(tmpbf.toString()));
                } else {
                    contentbf.append(TNUtilsHtml.en(tmpbf.toString()));
                }
                tmpbf = null;
            }
        } catch (Exception e) {
            return contentbf.toString().trim();
        }
        return contentbf.toString().trim();
    }

    public static String getPlainText(String content) {
        String str = content;

        str = str.replaceAll("  <br />  ", "\n");
        str = str.replaceAll("  <br/>  ", "\n");
        str = str.replaceAll("<br/>", "\n");
        str = str.replaceAll("<br />", "\n");

        int s = str.indexOf("<");
        int e = str.indexOf(">", s);
        while (s >= 0 && e > 0) {
            str = str.replace(str.substring(s, e + 1), "");
            s = str.indexOf("<");
            e = str.indexOf(">", s);
        }

        return str;
    }

    //TODO html bug

    /**
     * @param content
     * @return
     * @throws Exception
     */
    public static String getPlainText2(String content) throws Exception {

        //  方案1
//        String str = content.replaceAll("\\&[a-zA-Z]{1,10};", "") //去除类似< >  的字串
//                .replaceAll("<[a-zA-Z]+[1-9]?[^><]*>", "") //去除开始标签及没有结束标签的标签
//                .replaceAll("</[a-zA-Z]+[1-9]?>", ""); //去除结束标签


        //方案2
        String str = content;

        //处理table标签问题

        String str0 = content;
        String str1 = content;

        int startNum = 0;//
        int endNum = 0;//
        try {
            //
            int index0 = 0;
            while ((index0 = str0.indexOf("<table")) != -1) {
                if (str0.length() > (index0 + "<table".length())) {
                    str0 = str0.substring(index0 + "<table".length());
                }
                startNum++;
            }
            //
            int index01 = 0;
            while ((index01 = str1.indexOf("</table>")) != -1) {
                if (str1.length() > (index01 + "</table>".length())) {
                    str1 = str1.substring(index01 + "</table>".length());
                }
                endNum++;
            }
            str0 = null;
            str1 = null;

            //避免table标签不相等
            if (startNum == endNum) {
                //功能：替换<table></table>标签内的所有\n
                //索引位置
                int index1 = str.indexOf("<table");
                int index2 = str.indexOf("</table>");

                long count = 0;
                //循环2次
                while ((index1 >= 0 && index2 > 0) && count < 2) {
                    count++;
                    MLog.d("getPlainText2--while--count=" + count);
                    System.gc();//强制gc，html过大，容易oom
                    //获取子字符串
                    String temp = str.substring(index1, index2 + 1);
                    //替换所有的\n
                    String temp2 = temp.replaceAll("\n", "");
                    //str的temp子字符串替换成temp2
                    str = str.replaceAll(temp, temp2);
                    //下一个索引位置
                    index1 = str.indexOf("<table");
                    index2 = str.indexOf("</table>");

                }
            }
            //
            str = str.replaceAll("  <br />  ", "\n");
            str = str.replaceAll("  <br/>  ", "\n");
            str = str.replaceAll("<br/>", "\n");
            str = str.replaceAll("<br />", "\n");
            str = str.replaceAll("<html>", "");
            str = str.replaceAll("</html>", "");

            str = str.replaceAll("<body>", "");
            str = str.replaceAll("</body>", "");
            str = str.replaceAll("\n<tn-media", "<tn-media");
            str = str.replaceAll("\n</tn-media>", "</tn-media>");
        } catch (Exception e) {
            MLog.e("TNUtilsHtml--getPlainText2=" + e.toString());
            //继续执行
            return str;
        }

        return str;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (!TextUtils.isEmpty(str) && str != null) {
            Pattern p = Pattern.compile("(\r\n|\r|\n|\n\r)");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("").trim();
        }
        return dest;
    }

    public static TNNote insertStringInLocation(TNNote note, int location, String str) {
        MLog.i(TAG, "location:" + location + str);
        StringBuffer richText = new StringBuffer(note.richText);
        StringBuffer plainText = new StringBuffer(note.content);
        Vector<Integer> mapping = note.mapping;

        int lenPlain = plainText.length();
        int lenStr = str.length();

        if (lenStr < 0 || location > lenPlain) {
            return note;
        }

        for (int i = 0; i < lenStr; i++) {
            mapping.add(0);
        }

        for (int i = lenPlain + lenStr - 1; i >= location + lenStr; i--) {
            mapping.set(i, mapping.get(i - lenStr) + lenStr);
        }

        for (int i = location + lenStr - 1; i > location; i--) {
            mapping.set(i, mapping.get(location) + i - location);
        }

        if (location == lenPlain) {
            for (int i = 0; i < lenStr; i++) {
                if (location == 0) {
                    mapping.set(location + i, i);
                } else {
                    mapping.set(location + i, mapping.get(location + i - 1) + 1);
                }
            }
        }

        plainText.insert(location, str);
        richText.insert(mapping.get(location), str);

        note.content = plainText.toString();
        note.richText = richText.toString();
        note.mapping = mapping;
        return note;
    }

    public static TNNote deleteStringInRange(TNNote note, int location, int len) {
        MLog.i(TAG, "location:" + location + "len: " + len);
        StringBuffer richText = new StringBuffer(note.richText);
        StringBuffer plainText = new StringBuffer(note.content);
        Vector<Integer> mapping = note.mapping;

        int lenPlain = plainText.length();
        int lenStr = len;
        if (lenStr == 0) {
            return note;
        }
        int m = mapping.get(location + lenStr - 1) - mapping.get(location) + 1;

        if (m < lenStr) {
            return null;
        } else if (m == lenStr) {
            plainText.delete(location, location + lenStr);
            String s;
            s = richText.substring(mapping.get(location), mapping.get(location) + lenStr);
            MLog.i(TAG, s);
            richText.delete(mapping.get(location), mapping.get(location) + lenStr);
            for (int i = location; i < lenPlain - lenStr; i++) {
                mapping.set(i, mapping.get(i + lenStr) - lenStr);
            }
            for (int i = lenPlain - 1; i >= lenPlain - lenStr; i--) {
                mapping.remove(i);
            }

            note.richText = richText.toString();
            note.content = plainText.toString();
        } else {
            plainText.delete(location, location + lenStr);
            for (int i = location + lenStr - 1; i >= location; i--) {
                richText.delete(mapping.get(i), mapping.get(i) + 1);
                mapping.remove(i);
            }
            note.richText = richText.toString();
            note.content = plainText.toString();
        }

        return note;
    }

    public static TNNote WhileTextViewChangeText(TNNote note, String textTV) {
        textTV = textTV.replaceAll("<", "&lt;");
        textTV = textTV.replaceAll(">", "&gt;");
        note.content = note.content.replaceAll("<", "&lt;");
        note.content = note.content.replaceAll(">", "&gt;");
        int lenPlanText = note.content.length();
        int lenTV = textTV.length();
        int planTextLen = 0;
        int tVlen = 0;
        int planTextLocation = 0;
        int tVLocation = 0;

        if (lenPlanText == 0) {
            if (lenTV == 0) {
                return note;
            } else {
                note = insertStringInLocation(note, 0, textTV);
            }
        } else {
            if (lenPlanText < lenTV) {
                int j = lenPlanText;
                boolean diff = false;
                for (int i = 0; i < j; i++) {
                    if (note.content.charAt(i) != textTV.charAt(i)) {
                        planTextLocation = i;
                        tVLocation = i;
                        diff = true;
                        break;
                    }
                }
                if (!diff) {
                    note = insertStringInLocation(note, lenPlanText, textTV.substring(lenPlanText, lenTV));
                } else {
                    diff = false;
                    for (int i = 0; i < j - planTextLocation; i++) {
                        if (note.content.charAt(lenPlanText - 1 - i) != textTV.charAt(lenTV - 1 - i)) {
                            diff = true;
                            planTextLen = lenPlanText - i - planTextLocation;
                            tVlen = lenTV - i - tVLocation;
                            break;
                        }
                    }
                    if (!diff) {
                        note = insertStringInLocation(note, planTextLocation,
                                textTV.substring(tVLocation, tVLocation + lenTV - lenPlanText));
                    } else {
                        note = deleteStringInRange(note, planTextLocation, planTextLen);
                        note = insertStringInLocation(note, planTextLocation,
                                textTV.substring(tVLocation, tVLocation + tVlen));
                    }
                }
            } else {
                int j = lenTV;
                boolean diff = false;
                for (int i = 0; i < j; i++) {
                    if (textTV.charAt(i) != note.content.charAt(i)) {
                        diff = true;
                        planTextLocation = i;
                        tVLocation = i;
                        break;
                    }
                }
                if (!diff) {
                    note = deleteStringInRange(note, lenTV, lenPlanText - lenTV);
                } else {
                    diff = false;
                    for (int i = 0; i < j - tVLocation; i++) {
                        if (textTV.charAt(lenTV - 1 - i) != note.content.charAt(lenPlanText - 1 - i)) {
                            diff = true;
                            planTextLen = lenPlanText - i - planTextLocation;
                            tVlen = lenTV - i - tVLocation;
                            break;
                        }
                    }
                    if (!diff) {
                        note = deleteStringInRange(note, planTextLocation, lenPlanText - lenTV);
                    } else {
                        note = deleteStringInRange(note, planTextLocation, planTextLen);
                        note = insertStringInLocation(note, planTextLocation,
                                textTV.substring(tVLocation, tVLocation + tVlen));
                    }
                }
            }
        }
        note.content = note.content.replaceAll("&lt;", "<");
        note.content = note.content.replaceAll("&gt;", ">");
        return note;
    }
}
