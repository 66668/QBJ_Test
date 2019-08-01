package com.thinkernote.ThinkerNote.General;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;

import com.thinkernote.ThinkerNote.Data.TNNote;
import com.thinkernote.ThinkerNote.R;
import com.thinkernote.ThinkerNote.Utils.MLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TNUtils {
    private static final String TAG = "TNUtils";

    public static final String USERNAME_REGEX = "^[A-Za-z0-9\\u4e00-\\u9fa5][\\w\\u4e00-\\u9fa5-.]{1,49}$";
    public static final String FULL_EMAIL_REGEX = "^([a-zA-Z0-9]+([\\.+_-][a-zA-Z0-9]+)*)@(([a-zA-Z0-9]+((\\.|[-]{1,2})[a-zA-Z0-9]+)*)\\.[a-zA-Z]{2,6})$";
    public static final String PLAINTEXT_REGEX = "[\\s\\S]*(?!<br />)(?!<p>)(?!</p>)(?!<div>)(?!</div>)(<.*>)[\\s\\S]*";

    public static final String PHONE_REGEX = "^(0(10|2\\d|[3-9]\\d\\d)[- ]{0,3}\\d{7,8}|0?1[3584]\\d{9})$";
    public static final String EMAIL_REGEX = "([a-zA-Z0-9]+([\\.+_-][a-zA-Z0-9]+)*)@(([a-zA-Z0-9]+((\\.|[-]{1,2})[a-zA-Z0-9]+)*)\\.[a-zA-Z]{2,6})";

    public static final String PUNCTUATION_REGEX = "[,.!?，？！，]";

    public static void showObject(Object aObj) {
        MLog.d(TAG, aObj.getClass().toString() + " \tValue:" + aObj.toString());
    }

    public static void showViews(View aView, String prefix) {
        MLog.d(TAG, String.format("%s%s:(%d,%d)-(%d,%d)", prefix, aView
                .getClass().toString(), aView.getLeft(), aView.getTop(), aView
                .getWidth(), aView.getHeight()));

        prefix = (prefix == "") ? ("|--") : ("|  " + prefix);

        if (ViewGroup.class.isInstance(aView)) {
            ViewGroup vg = (ViewGroup) aView;
            for (int i = 0; i < vg.getChildCount(); i++) {
                TNUtils.showViews(vg.getChildAt(i), prefix);
            }
        }
    }

    public static String getPingYinIndex(String title) {
        title = title.trim();
        if (title.length() > 8) {
            title = title.substring(0, 8);
        }
        String pingYinIndex = TNUtilsPingYin.getPingYin(title);
        if (pingYinIndex.length() == 0) {
            pingYinIndex = "z";
        }
        if (pingYinIndex.length() > TNNote.PINGYININDEX) {
            pingYinIndex = pingYinIndex.substring(0, TNNote.PINGYININDEX);
        }
        if (title.length() == 0) {
            pingYinIndex = "#";
        }
        return pingYinIndex;
    }

    public static String getBriefContent(String contentStr) {
        String str = new String(contentStr);
        str = str.trim();

        str = str.replaceAll("<br />", " ");
        str = str.replaceAll("</p>", " ");
        str = str.replaceAll("</div>", " ");
        str = str.replaceAll("\n", " ");

        int s = str.indexOf("<");
        int e = str.indexOf(">", s);
        while (s >= 0 && e > 0) {
            str = str.replace(str.substring(s, e + 1), "");
            s = str.indexOf("<");
            e = str.indexOf(">", s);
        }
        str = (TNUtilsHtml.decodeHtml(str));

        if (str.length() > TNNote.BRIEFCONTENTLENGTH) {
            str = str.substring(0, TNNote.BRIEFCONTENTLENGTH);
        }

        return str;
    }

    public static String getPlainText(String richText) {
        String str = richText.trim();
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&gt;", ">");
        str = str.replaceAll("&quot;", "\"");
        str = str.replaceAll("&apos;", "'");
        str = str.replaceAll("&nbsp;", " ");
        str = str.replaceAll("<br />", "\n");
//		str = str.replaceAll(" ", "\r");
//		str = str.replace("\\\\", "\\");

        return str;
    }

    public static Context getAppContext() {
        return TNSettings.getInstance().appContext;
    }

    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    public static byte[] longToByteArray(long value) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = i * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    public static int byteArrayToInt(byte[] b) {
        return (b[0] & 0xFF) + ((b[1] & 0xFF) << 8) + ((b[2] & 0xFF) << 16)
                + ((b[3] & 0xFF) << 24);
    }

    public static int doubleToInt(double value, int mul) {
        return (int) (value * mul);
    }

    public static JSONObject makeJSON(String[] keys, Object[] values) {
        JSONObject jsonObject = new JSONObject();
        try {
            for (int i = 0; i < keys.length; i++) {
                jsonObject.put(keys[i], values[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    //TODO 对于有html的内容，禁止使用该模式，容易导致bug
    public static JSONObject makeJSON(Object... aArray) {
        JSONObject jsonObject = new JSONObject();
        try {
            for (int i = 0; i < aArray.length; i += 2) {
                jsonObject.put((String) aArray[i], aArray[i + 1]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            MLog.e("makeJSON()异常:" + e.toString());
        }
        return jsonObject;
    }

    public static void putToJson(JSONObject aJson, String key, Object value) {
        try {
            aJson.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject stringToJson(String key) {
        JSONObject object = null;
        try {
            object = new JSONObject(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static Object getFromJSON(JSONObject aJson, String aKey) {
        Object obj = null;
        try {
            if (aJson.has(aKey))
                obj = aJson.get(aKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Object getFromLongJSON(JSONObject aJson, String aKey) {
        long obj = -1;
        try {
            if (aJson.has(aKey))
                obj = Long.valueOf(aJson.get(aKey).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static Object getFromJSON(JSONArray aJson, int aIndex, String aKey) {
        return getFromJSON((JSONObject) getFromJSON(aJson, aIndex), aKey);
    }

    public static Object getFromJSON(JSONArray aJson, int aIndex) {
        Object obj = null;
        try {
            if (aJson.length() > aIndex)
                obj = aJson.get(aIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static String getClient() {
        MLog.d(TAG, "BOARD:" + Build.BOARD + "\r\nBRAND:" + Build.BRAND
                + "\r\nDEVICE:" + Build.DEVICE + "\r\nDISPLAY:" + Build.DISPLAY
                + "\r\nFINGERPRINT:" + Build.FINGERPRINT + "\r\nHOST:"
                + Build.HOST + "\r\nID:" + Build.ID + "\r\nMODEL:"
                + Build.MODEL + "\r\nPRODUCT:" + Build.PRODUCT + "\r\nTAGS:"
                + Build.TAGS + "\r\nTIME:" + String.valueOf(Build.TIME)
                + "\r\nTYPE:" + Build.TYPE + "\r\nUSER:" + Build.USER
                + "\r\nINCREMENTAL:" + Build.VERSION.INCREMENTAL
                + "\r\nRELEASE:" + Build.VERSION.RELEASE + "\r\nSDK:"
                + Build.VERSION.SDK);
        return Build.MODEL + "( Android " + Build.VERSION.RELEASE + ")";
        // return Build.FINGERPRINT;
    }

    public static String getPhoneData() {
        TelephonyManager tm = (TelephonyManager) TNSettings.getInstance().appContext.getSystemService(Context.TELEPHONY_SERVICE);
        StringBuilder sb = new StringBuilder();
        sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
        sb.append("\nDeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion());
        sb.append("\nLine1Number = " + tm.getLine1Number());
        sb.append("\nNetworkCountryIso = " + tm.getNetworkCountryIso());
        sb.append("\nNetworkOperator = " + tm.getNetworkOperator());
        sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());
        sb.append("\nNetworkType = " + tm.getNetworkType());
        sb.append("\nPhoneType = " + tm.getPhoneType());
        sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
        sb.append("\nSimOperator = " + tm.getSimOperator());
        sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
        sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
        sb.append("\nSimState = " + tm.getSimState());
        sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
        sb.append("\nVoiceMailNumber = " + tm.getVoiceMailNumber());
        MLog.d(TAG, sb.toString());
        return sb.toString();
    }

    public static String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) TNSettings.getInstance().appContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        if (imei != null) {
            return imei;
        } else
            return "unkown";
    }

    public static String getTimezone() {
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)
                + " ("
                + TimeZone.getDefault().getDisplayName(false, TimeZone.LONG)
                + ")";
    }

    public static String toMd5(String aString) {
        return toMd5(aString.getBytes());
    }

    public static String toMd5(byte[] bytes) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes);
            return toHexString(algorithm.digest(), "");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toHexString(byte[] bytes, String separator) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            if ((0xFF & b) < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & b)).append(separator);
        }
        return hexString.toString().toUpperCase();
    }

    public static boolean checkRegex(String aRegex, String aStr) {
        return Pattern.compile(aRegex).matcher(aStr).matches();
    }

    public static void showSMS() {
        // content://sms/inbox 收件箱
        // content://sms/sent 已发送
        // content://sms/draft 草稿
        // content://sms/outbox 发件箱
        // content://sms/failed 发送失败
        // content://sms/queued 待发送列表
        ContentResolver cr = getAppContext().getContentResolver();
        MLog.d(TAG, "maincats_icons_sync");
        String[] uris = {"content://sms/inbox", "content://sms/sent",
                "content://sms/draft", "content://sms/outbox",
                "content://sms/failed", "content://sms/queued",
                "content://sms/sim"};
        for (int i = 0; i < uris.length; i++) {
            MLog.d(TAG, uris[i]);
            Uri uri = Uri.parse(uris[i]);
            Cursor cur = cr.query(uri, null, null, null, null);
            if (cur.moveToFirst()) {
                do {
                    for (int j = 0; j < cur.getColumnCount(); j++) {
                        String info = "name:" + cur.getColumnName(j) + "="
                                + cur.getString(j);
                        MLog.d("====>", info);
                    }
                } while (cur.moveToNext());
            }
        }
    }

    public static boolean isNetWork() {
        NetworkInfo networkInfo = ((ConnectivityManager) getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            MLog.i(TAG,
                    networkInfo.getTypeName() + " "
                            + networkInfo.getSubtypeName());
            return true;
        }
        return false;
    }

    public static boolean checkNetwork(Activity act) {

        boolean network = isNetWork();
        if (!network) {
            TNUtilsUi.alert(act, R.string.alert_Net_NotWork);
        }
        return network;
    }

    public static boolean isNetWorkWifiAnd4G() {
        NetworkInfo networkInfo = ((ConnectivityManager) getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            MLog.i(TAG,
                    networkInfo.getTypeName() + " "
                            + networkInfo.getSubtypeName());
            if (networkInfo.getTypeName().equals("WIFI") || networkInfo.getTypeName().equals("4G")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAutoSync() {
        NetworkInfo networkInfo = ((ConnectivityManager) getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            MLog.i(TAG,
                    networkInfo.getTypeName() + " "
                            + networkInfo.getSubtypeName());
//			原来可以手动设置在什么网络下自动同步
//			TNSettings settings = TNSettings.getInstance();
//			if (settings.sync == 0) {
//				return true;
//			} else if (settings.sync == 1
//					&& networkInfo.getTypeName().equals("WIFI")) {
//				return true;
//			}
//			if (networkInfo.getTypeName().equals("WIFI")) {
//				return true;
//			}
            return true;
        }
        return false;
    }

    public static void getSDCardInfo() {
        if (TNUtilsAtt.hasExternalStorage()) {
            try {
                File input = new File("/sys/class/mmc_host/mmc1");
                String cid_directory = null;
                int i = 0;
                File[] sid = input.listFiles();

                for (i = 0; i < sid.length; i++) {
                    if (sid[i].toString().contains("mmc1:")) {
                        cid_directory = sid[i].toString();
                        String SID = (String) sid[i].toString().subSequence(
                                cid_directory.length() - 4,
                                cid_directory.length());
                        MLog.d(TAG, " SID of MMC = " + SID);
                        break;
                    }
                }
                BufferedReader CID = new BufferedReader(new FileReader(
                        cid_directory + "/cid"));
                String sd_cid = CID.readLine();
                MLog.d(TAG, "CID of the MMC = " + sd_cid);

            } catch (Exception e) {
                MLog.e(TAG, "Can not read SD-card cid");
            }

        } else {
            MLog.d(TAG, "External Storage Not available!!");
        }
    }

    public static final String WORDS = "jqschwartzfkungdvpilemybox";

    public static String toInviteCode(long id) {
        StringBuilder sb = new StringBuilder();
        long num = id * 10 + id % 10;
        while (num > 0) {
            sb.append(WORDS.charAt((int) (num % 26)));
            num = num / 26;
        }
        return sb.toString();
    }

    public static long fromInviteCode(String code) {
        long num = 0;
        for (int i = code.length() - 1; i >= 0; i--) {
            int index = WORDS.indexOf(code.charAt(i));
            if (index >= 0)
                num = num * 26 + index;
            else
                return 0;
        }
        long id = num / 10;
        return id % 10 == num % 10 ? id : 0;
    }

    // public static final String URL_REGEX =
    // "(http|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
    public static final String URL_REGEX = "(http|https):\\/\\/[A-Za-z0-9_\\-_]+(\\.[A-Za-z0-9_\\-_]+)+([A-Za-z0-9_\\-\\.,@?^=%&amp;:/~\\+#]*[A-Za-z0-9_\\-\\@?^=%&amp;/~\\+#])?";

    public static int getWordCount2(String s) {
        s = s.trim();
        if (s.length() > 0) {
            Pattern p = Pattern.compile(URL_REGEX);
            Matcher m = p.matcher(s);

            int allUrlLen = 0;
            while (m.find()) {
                String url = m.group();
                int len = url.length();
                if (url.startsWith("http://t.cn")) {
                    allUrlLen += len;
                } else if (url.startsWith("http://weibo.com")
                        || url.startsWith("http://weibo.cn")) {
                    allUrlLen += (len <= 41) ? len : ((len <= 140) ? 20
                            : (len - 140 + 20));
                } else {
                    allUrlLen += (len <= 140) ? 20 : (len - 140 + 20);
                }
                s = s.replace(url, "");
            }

            int length = 0;
            try {
                length = allUrlLen + s.getBytes("GBK").length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return length / 2 + length % 2;
        } else {
            return 0;
        }
    }

    public static int getWordCount(String s) {
        int length = 0;
        try {
            length = s.getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        length = length / 2 + length % 2;
        return length;
    }

    public static String cutByWordCount(String s, int len) {
        byte[] bytes = null;
        try {
            bytes = s.getBytes("GBK");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        if (bytes.length <= 2 * len)
            return s;

        int length = 0;
        int index = 0;
        while (true) {
            if (bytes[length] < 0)
                length += 2;
            else
                length += 1;
            if (length > 2 * len)
                break;
            index += 1;
        }
        return s.substring(0, index);
    }

    public static Queue<Integer> getPath(String s) {
        String inner = s.substring(1, s.length() - 1);
        String[] subs = inner.split("[,]");

        LinkedList<Integer> pp = new LinkedList<Integer>();
        for (String sub : subs) {
            if (sub.trim().length() > 0)
                pp.add(Integer.valueOf(sub.trim()));
        }

        return pp;
    }

    public static Integer getInteger(String s) {
        int n = 0;
        try {
            n = Integer.valueOf(s);
        } catch (Exception e) {
            return 0;
        }
        return n;
    }

    public static long Hash17(long noteId) {
        if (noteId < 1) {
            return -1;
        }

        return (17 * noteId) ^ 6379;
    }

    public static long Hash17Encode(long id) {
        if (id < 1) {
            return -1;
        }

        return (id ^ 6379) / 17;
    }

    public static long formatStringToTime(String date) {
        Date d = null;
        try {
            d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            return -1;
        }
        return d.getTime();
    }

    public static String formatDateToDay(long milliseconds) {
        Date date = new Date(milliseconds);
        return String.format("%d年%d月%d日  %02d:%02d ", date.getYear() + 1900,
                date.getMonth() + 1, date.getDate(), date.getHours(),
                date.getMinutes());
    }

    public static String formatDateToWeeks(long milliseconds) {
        long now = System.currentTimeMillis() / 1000;
        long tzOffset = Calendar.getInstance().getTimeZone().getRawOffset() / 1000;
        long secToday = (now + tzOffset) / 86400 * 86400 - tzOffset;
        float hours = (secToday - milliseconds) / 3600f;

        Vector<String> weeks = new Vector<String>();
        weeks.add("星期日");
        weeks.add("星期一");
        weeks.add("星期二");
        weeks.add("星期三");
        weeks.add("星期四");
        weeks.add("星期五");
        weeks.add("星期六");

        Date date = new Date(milliseconds * 1000L);
        String formated = null;
        if (hours < 144 && hours > -24) {
            if (hours <= 0) { // today
                formated = String.format("%s %02d点%02d分", "今天",
                        date.getHours(), date.getMinutes());
            } else if (hours < 24) { // yestoday
                formated = String.format("%s %02d点%02d分", "昨天",
                        date.getHours(), date.getMinutes());
            } else { // this week
                formated = String.format("%2d月%d日 %s", date.getMonth() + 1,
                        date.getDate(), weeks.get(date.getDay()));
            }
        } else {
            formated = String.format("%d年%d月%d日", date.getYear() + 1900,
                    date.getMonth() + 1, date.getDate());
        }

        MLog.i(TAG, "milliseconds=" + milliseconds + "now=" + now + "tzOffset="
                + tzOffset + "secToday=" + secToday + "hours=" + hours
                + "formated=" + formated);

        return formated;
    }

    public static void unzip(String filePath, String outputDirectory) {
        MLog.i("unzip", "srcPath:" + filePath + " outDirectory:"
                + outputDirectory);
        try {
            FileInputStream zipFileName = null;
            zipFileName = new FileInputStream(filePath);
            unZipStream(zipFileName, outputDirectory);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void unZipStream(InputStream zipFileName,
                                   String outputDirectory) {
        try {
            ZipInputStream in = new ZipInputStream(zipFileName);
            // 获取ZipInputStream中的ZipEntry条目，一个zip文件中可能包含多个ZipEntry，
            // 当getNextEntry方法的返回值为null，则代表ZipInputStream中没有下一个ZipEntry，
            // 输入流读取完成；
            ZipEntry entry = in.getNextEntry();
            while (entry != null) {

                // 创建以zip包文件名为目录名的根目录
                File file = new File(outputDirectory);
                file.mkdirs();
                if (entry.isDirectory()) {
                    String name = entry.getName();
                    name = name.substring(0, name.length() - 1);

                    file = new File(outputDirectory + File.separator + name);
                    file.mkdir();

                } else {
                    file = new File(outputDirectory + File.separator
                            + entry.getName());
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    int b = -1;
                    byte[] bytes = new byte[1024];
                    while ((b = in.read(bytes)) != -1) {
                        out.write(bytes, 0, b);
                    }
                    out.close();
                    out = null;
                }
                // 读取下一个ZipEntry
                entry = in.getNextEntry();
            }
            if (in != null) {
                in.close();
                in = null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMetaData(String name) {
        ApplicationInfo appInfo;
        try {
            appInfo = TNSettings.getInstance().appContext.getPackageManager()
                    .getApplicationInfo("com.thinkernote.ThinkerNote", PackageManager.GET_META_DATA);
            return appInfo.metaData.get(name).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknow";
        }
    }

    public static String getNewWeiboImage(String path) {
        MLog.i(TAG, "srcPath:" + path);
        File file = new File(path);
        if (!file.exists()) {
            return "";
        }
        String newPath = path;
        if (file.length() > 5 * 1024 * 1024) {
            newPath = TNUtilsAtt.getTempPath(file.getName());
            TNUtilsAtt.compressionPicture(file, newPath);
        }
        MLog.i(TAG, "newPath:" + newPath);
        return newPath;
    }

    public static String readFile(String fileName) {
        File f = new File(fileName);

        if (f.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(f));
                StringBuffer sb = new StringBuffer();
                String str = null;
                while ((str = br.readLine()) != null) {
                    sb.append(str);
                }
                MLog.d("devkey", sb.toString());
                br.close();
                return sb.toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return null;
    }

    // 写文件
    public static void writeTextFile(String fileName, String content) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(fileName);
            byte[] bytes = content.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void writeFile(String fileName, String content) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(fileName, true);
            fw.write(content, 0, content.length());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 生成签名数据
     *
     * @param data 待加密的数据
     * @param key  加密使用的key
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public static String hmacSha1(String data, String key) {
        StringBuilder sb = new StringBuilder();
        try {
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            for (byte b : rawHmac) {
                sb.append(byteToHexString(b));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }

    private static String byteToHexString(byte ib) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
                'b', 'c', 'd', 'e', 'f'};
        char[] ob = new char[2];
        ob[0] = Digit[(ib >>> 4) & 0X0f];
        ob[1] = Digit[ib & 0X0F];
        String s = new String(ob);
        return s;
    }

    public static int dipToPx(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dipToPx(scale, dipValue);
    }

    public static int dipToPx(float density, float dipValue) {
        return (int) (dipValue * density + 0.5f);
    }

    public static int pxToDip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return pxToDip(scale, pxValue);
    }

    public static int pxToDip(float density, float pxValue) {
        return (int) (pxValue / density + 0.5f);
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles) {
	    /* 
			    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188 
			    联通：130、131、132、152、155、156、185、186 
			    电信：133、153、180、189、（1349卫通） 
			    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9 
	    */
        String telRegex = "[1][34578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobiles))
            return false;
        else
            return mobiles.matches(telRegex);
    }

    public static void goToLogin(Context context) {
        Intent i = new Intent();
        i.setClassName(TNSettings.kThinkerNotePackage, TNSettings.kActivityPackage + ".TNLoginAct");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    //将字符串转换成Bitmap类型
    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // 将Bitmap转换成字符串
    public static String bitmapToString(Bitmap bitmap, int bitmapQuality) {
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, bitmapQuality, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        return string;
    }

    /**
     * 修改图片大小
     *
     * @param bitmap
     * @param size   缩放比
     * @return
     */
    public static Bitmap changeBitmapSize(Bitmap bitmap, double size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //获取想要缩放的matrix
        Matrix matrix = new Matrix();
        matrix.postScale((float) size, (float) size);
        //获取新的bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

}
