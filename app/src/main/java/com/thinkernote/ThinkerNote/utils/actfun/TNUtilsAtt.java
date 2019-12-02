package com.thinkernote.ThinkerNote.utils.actfun;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.StatFs;
import android.webkit.MimeTypeMap;

import com.thinkernote.ThinkerNote.utils.FileUtils;
import com.thinkernote.ThinkerNote.utils.MLog;
import com.thinkernote.ThinkerNote.utils.TNUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Map.Entry;

public class TNUtilsAtt {
    private static final String TAG = "TNUtilsAtt";
    public static final int THUMBNAIL_WIDTH = 160;
    public static final int THUMBNAIL_HEIGH = 160;

    private static Hashtable<String, Integer> typeMap = new Hashtable<String, Integer>();

    static {
        typeMap.put(".gif", 10001);
        typeMap.put(".jpg", 10002);
        typeMap.put(".jpeg", 10002);
        typeMap.put(".JPG", 10002);
        typeMap.put(".JPEG", 10002);
        typeMap.put(".bmp", 10003);
        typeMap.put(".png", 10004);
        typeMap.put(".svg", 10005);
        typeMap.put(".ico", 10006);
        typeMap.put(".tif", 10007);
        typeMap.put(".mp3", 20001);
        typeMap.put(".mid", 20002);
        typeMap.put(".wav", 20003);
        typeMap.put(".aac", 20004);
        typeMap.put(".amr", 20005);
        typeMap.put(".m4a", 20006);
        typeMap.put(".3gpp", 20007);
        typeMap.put(".mpeg", 30001);
        typeMap.put(".mov", 30002);
        typeMap.put(".avi", 30003);
        typeMap.put(".pdf", 40001);
        typeMap.put(".txt", 40002);
        typeMap.put(".doc", 40003);
        typeMap.put(".rtf", 40004);
        typeMap.put(".ppt", 40005);
        typeMap.put(".html", 40006);
        typeMap.put(".htm", 40007);
        typeMap.put(".xml", 40008);
        typeMap.put(".xls", 40009);
        typeMap.put(".docx", 40010);
        typeMap.put(".pptx", 40011);
        typeMap.put(".xlsx", 40012);
        typeMap.put(".mht", 40013);
    }

    public static int getAttType(String aName) {
        int index = aName.lastIndexOf(".");
        if (index < 0)
            return 50000;

        String suffix = aName.substring(index, aName.length());
        Integer type = typeMap.get(suffix);
        return (type == null) ? 50000 : type;
    }

    public static String getAttSuffix(int type) {
        for (Entry<String, Integer> entry : typeMap.entrySet()) {
            if (entry.getValue() == type) {
                return entry.getKey();
            }
        }
        return "";
    }

    public static String getAttPath(long aId, int type) {
        return getAttPath(aId, "sdcard", type);
    }

    public static String getAttPath(long aId, String disk, int type) {
        String path = null;
        if (disk.equals("sdcard")) {
            if (availableSpace(disk) > 10 * 1024 * 1024)
                //Android/data/com.thinkernote.ThinkerNote/files/Attachment/
                path = FileUtils.ABS_PACKAGE_PRIV_PATH + FileUtils.FILE_ATTAXHMENT_PATH +
                        +aId / 1000 / 1000 + "/" + aId / 1000 + "/" + aId
                        + getAttSuffix(type);
        } else if (disk.equals("data")) {
            if (availableSpace(disk) > 10 * 1024 * 1024)
                //data/data/包名下/Attachment/
                path = FileUtils.DEFAULT_INNER_PATH + "/Attachment/"
                        + aId / 1000 / 1000 + "/" + aId / 1000
                        + "/" + aId + getAttSuffix(type);
        }
        return path;
    }

    public static void deleteAllAtts() {
        MLog.d(TAG, "deleteAllAtts");
        if (hasExternalStorage()) {
            //Android/data/com.thinkernote.ThinkerNote/files/Attachment/
            recursionDeleteDir(new File(FileUtils.ABS_PACKAGE_PRIV_PATH + FileUtils.FILE_ATTAXHMENT_PATH));
        }
        //data/data/包名下/Attachment/
        recursionDeleteDir(new File(FileUtils.DEFAULT_INNER_PATH + "/Attachment/"));
    }

    public static void createNomedia() {
        MLog.d(TAG, "createNomedia");
        try {
            if (hasExternalStorage()) {
                //Android/data/com.thinkernote.ThinkerNote/files/.nomedia
                File f = new File(FileUtils.ABS_PACKAGE_PRIV_PATH + "/.nomedia");
                if (!f.exists()) {
                    f.getParentFile().mkdirs();
                    if (!f.createNewFile())
                        MLog.i(TAG, "create .nomedia failed. " + f);
                }
            }
            //data/data/包名下/.nomedia/
            File f2 = new File(FileUtils.DEFAULT_INNER_PATH + "/.nomedia");
            if (!f2.exists()) {
                f2.getParentFile().mkdirs();
                if (!f2.createNewFile())
                    MLog.i(TAG, "create .nomedia failed. " + f2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTempPath(String aName) {
        String path = null;
        if (hasExternalStorage()) {
            //Android/data/com.thinkernote.ThinkerNote/files/Temp/
            path = FileUtils.ABS_PACKAGE_PRIV_PATH + FileUtils.FILE_TEMP_PATH;
        } else {
            //data/data/包名下/Temp/
            path = FileUtils.DEFAULT_INNER_PATH + FileUtils.FILE_TEMP_PATH;
        }
        String filename = String.valueOf(System.currentTimeMillis());
        String suffix = aName.substring(aName.lastIndexOf(".") + 1,
                aName.length());
        return path + filename + "." + suffix;

    }

    public static String getRecordTempPath(String aName) {
        String path = null;
        if (hasExternalStorage()) {
            //Android/data/com.thinkernote.ThinkerNote/files/Record/
            path = FileUtils.ABS_PACKAGE_PRIV_PATH + FileUtils.FILE_RECORD_PATH;
        } else {
            //data/data/包名下/Record/
            path = FileUtils.DEFAULT_INNER_PATH + FileUtils.FILE_RECORD_PATH;
        }
        String filename = String.valueOf(System.currentTimeMillis());
        String suffix = aName.substring(aName.lastIndexOf(".") + 1,
                aName.length());
        return path + filename + "." + suffix;
    }

    public static String getSkinsPath() {
        String path = null;
        if (hasExternalStorage()) {
            //Android/data/com.thinkernote.ThinkerNote/files/Skins/
            path = FileUtils.ABS_PACKAGE_PRIV_PATH + FileUtils.FILE_SKINS_PATH;
        } else {
            //data/data/包名下/Skins/
            path = FileUtils.DEFAULT_INNER_PATH + FileUtils.FILE_SKINS_PATH;
        }
        return path;
    }

    public static void deleteTempFiles() {
        MLog.d(TAG, "deleteTempFiles");
        if (hasExternalStorage()) {
            //Android/data/com.thinkernote.ThinkerNote/files/Temp/
            recursionDeleteDir(new File(FileUtils.ABS_PACKAGE_PRIV_PATH + FileUtils.FILE_TEMP_PATH));
        }
        //data/data/包名下/Temp/
        recursionDeleteDir(new File(FileUtils.DEFAULT_INNER_PATH + FileUtils.FILE_TEMP_PATH));
    }

    /**
     * 遍历删除文件夹下的内容
     *
     * @param fileOrDirectory
     */
    public static void recursionDeleteDir(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                recursionDeleteDir(child);

        fileOrDirectory.delete();
    }

    public static boolean isHasSpace() {
        return availableSpace("sdcard") > 10 * 1024 * 1024;
    }

    public static long availableSpace(String type) {
        File dir = null;
        if (type.equals("sdcard")) {
            if (hasExternalStorage()) {
                dir = FileUtils.DEFAULT_BASE_PIRV_FILE;
            } else {
                return 0;
            }
        } else if (type.equals("data")) {
            dir = Environment.getDataDirectory();
        }
        MLog.d(TAG, "dir=" + dir);
        StatFs sf = new StatFs(dir.getPath());
        return sf.getBlockSizeLong() * sf.getAvailableBlocksLong();
    }

    public static long totalSpace(String type) {
        File dir = null;
        if (type.equals("sdcard")) {
            if (hasExternalStorage()) {
                dir = FileUtils.DEFAULT_BASE_PIRV_FILE;
            } else {
                return 0;
            }
        } else if (type.equals("data")) {
            dir = Environment.getDataDirectory();
        }

        StatFs sf = new StatFs(dir.getPath());
        return sf.getBlockSizeLong() * sf.getAvailableBlocksLong();
    }

    public static String fileToMd5(String sFile) {
        String md5 = null;
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();

            // 新建文件输入流并对它进行缓冲
            InputStream inBuff = new BufferedInputStream(new FileInputStream(
                    sFile));

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                algorithm.update(b, 0, len);
            }

            // 关闭流
            inBuff.close();

            md5 = TNUtils.toHexString(algorithm.digest(), "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return md5;
    }

    public static void copyFile(String sFile, String tFile) throws Exception {
        copyFile(new FileInputStream(sFile), tFile);
    }

    public static void copyFile(InputStream sStream, String tFile)
            throws Exception {
        // 新建文件输入流并对它进行缓冲
        InputStream inBuff = new BufferedInputStream(sStream);

        // 新建文件输出流并对它进行缓冲
        File out = new File(tFile);
        if (!out.exists()) {
            new File(out.getParent()).mkdirs();
            out.createNewFile();
        }
        OutputStream outBuff = new BufferedOutputStream(new FileOutputStream(
                out));

        // 缓冲数组
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len = inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }

        // 刷新此缓冲的输出流
        outBuff.flush();

        // 关闭流
        inBuff.close();
        outBuff.close();
    }

    public static boolean compressionPicture(File file, String outPath) {
        File outfile = new File(outPath);
        FileOutputStream fo = null;
        try {
            if (!outfile.exists()) {
                new File(outfile.getParent()).mkdirs();
                outfile.createNewFile();
            }

            fo = new FileOutputStream(outfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        int angle = getImageOrientation(file.getPath());

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), opts);

        opts.inSampleSize = computeSampleSize(opts, -1, 1200 * 800);
        MLog.e("TNNoteAtt", "bitmap sampleSize: " + opts.inSampleSize);
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), opts);

        bitmap = rotateImage(bitmap, angle);

        // bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
        boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
        try {
            fo.flush();
            fo.close();
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return saved;
    }

    public static int getImageOrientation(String path) {
        int orientation = 0;
        try {
            ExifInterface ef = null;
            ef = new ExifInterface(path);
            int tag = ef.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (tag == ExifInterface.ORIENTATION_ROTATE_90) {
                orientation = 90;
            } else if (tag == ExifInterface.ORIENTATION_ROTATE_180) {
                orientation = 180;
            } else if (tag == ExifInterface.ORIENTATION_ROTATE_270) {
                orientation = 270;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MLog.e(TAG, path + " angle=" + orientation + "°");
        return orientation;
    }

    public static Bitmap rotateImage(Bitmap bitmap, int angle) {
        if (angle != 0) {
            // 下面的方法主要作用是把图片转一个角度
            Matrix m = new Matrix();
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            m.setRotate(angle); // 旋转angle度  
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                    m, true);// 从新生成图片
        }
        return bitmap;
    }

    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static String SaveBitmapToImage(Bitmap bitmap) {
        String path = getTempPath("1.jpg");
        return SaveBitmapToImage(bitmap, path);
    }

    public static String SaveBitmapToImage(Bitmap bitmap, String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                new File(file.getParent()).mkdirs();
                file.createNewFile();
            }

            FileOutputStream out = new FileOutputStream(file);
//			bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            MLog.i(TAG, "save ok: " + path);
            return path;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BitmapFactory.Options getImageSize(String path) {
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bfo);
        MLog.d(TAG, bfo.outWidth + "," + bfo.outHeight + "," + path);

        return bfo;
    }

    public static int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static Bitmap resizeImage(String path, int width, int height) {
        // get size of file
        BitmapFactory.Options bfo = getImageSize(path);

        // decodeFile scale
        int scale = 1;
        while (true) {
            if ((width <= 0 || bfo.outWidth / scale < width)
                    && (height <= 0 || bfo.outHeight / scale < height))
                break;
            scale *= 2;
        }
        MLog.d(TAG, bfo.outWidth + "," + bfo.outHeight + ", scale=" + scale);
        BitmapFactory.Options bfo2 = new BitmapFactory.Options();
        bfo2.inSampleSize = scale;
        Bitmap b1 = BitmapFactory.decodeFile(path, bfo2);

        return b1;
    }

    public static boolean isSmallImage(String path) {
        BitmapFactory.Options bfo = getImageSize(path);
        if ((bfo.outWidth > 0 && bfo.outWidth < 160)
                || (bfo.outHeight > 0 && bfo.outHeight < 120))
            return true;
        else
            return false;
    }

    public static String makeThumbnailForImage(String path) throws Exception {
        return makeThumbnailForImage(path, THUMBNAIL_WIDTH, THUMBNAIL_HEIGH);
    }

    public static Bitmap makeThumbnailBitmap(String path, int thumbnailWidth, int thumbnailHeight) {
        // get size of file
        BitmapFactory.Options bfo = getImageSize(path);
        if ((bfo.outWidth < thumbnailWidth || bfo.outHeight < thumbnailHeight)
                || (bfo.outWidth <= thumbnailWidth && bfo.outHeight <= thumbnailHeight)) {
            // small image
            return null;
        }

        // decodeFile scale
        int scale = 1;
        while (true) {
            if (bfo.outWidth / scale / 2 < thumbnailWidth
                    || bfo.outHeight / scale / 2 < thumbnailHeight)
                break;
            scale *= 2;
        }
        MLog.d(TAG, bfo.outWidth + "," + bfo.outHeight + ", scale=" + scale);
        BitmapFactory.Options bfo2 = new BitmapFactory.Options();
        bfo2.inSampleSize = scale;
        Bitmap b1 = BitmapFactory.decodeFile(path, bfo2);
        // if( b1.getWidth() < 160 || b1.getHeight() < 120){
        // // small image
        // //return bitmap;
        // return null;
        // }

        MLog.d(TAG, "b1: whidth=" + b1.getWidth() + " height=" + b1.getHeight());
        // scale
        Bitmap b2;
        if (b1.getWidth() == thumbnailWidth
                || b1.getHeight() == thumbnailHeight) {
            b2 = b1;
        } else {
            Matrix matrix = new Matrix();
            // 161f? 121f? a bit large to make sure image larger than 160x120
            // after scaled
            // float scale2 = Math.max(161f/b1.getWidth(), 121f/b1.getHeight());
            //
            float scale2 = Math.max((thumbnailHeight + 1f) / b1.getHeight(),
                    (thumbnailWidth + 1f) / b1.getWidth());
            matrix.postScale(scale2, scale2);
            b2 = Bitmap.createBitmap(b1, 0, 0, b1.getWidth(), b1.getHeight(),
                    matrix, true);
        }
        MLog.d(TAG, "b2: whidth=" + b2.getWidth() + " height=" + b2.getHeight());
        // crop
        Bitmap b3 = Bitmap.createBitmap(b2,
                (b2.getWidth() - thumbnailWidth) / 2,
                (b2.getHeight() - thumbnailHeight) / 2, thumbnailWidth,
                thumbnailHeight);

        MLog.d(TAG, b3.getWidth() + "," + b3.getHeight());

        if (b1 != b2)
            b1.recycle();
        b2.recycle();

        return b3;
    }

    public static String makeThumbnailForImage(String path, int thumbnailWidth, int thumbnailHeight) throws Exception {
        MLog.d(TAG, "makeThumbnailForImage:" + path);
        File orgfile = new File(path);
        if (!orgfile.exists())
            return null;

        // check if .thm already exist
        File thmfile = new File(path + ".thm");
        if (thmfile.exists()) {
            // return BitmapFactory.decodeFile(path + ".thm");
            return path + ".thm";
        }

        Bitmap b3 = makeThumbnailBitmap(path, thumbnailWidth, thumbnailHeight);
        if (b3 == null)
            return null;

        // save thumbnail
        new File(thmfile.getParent()).mkdirs();
        thmfile.createNewFile();
        OutputStream outBuff = new BufferedOutputStream(new FileOutputStream(
                thmfile));
        b3.compress(CompressFormat.JPEG, 100, outBuff);

        // return b3;
//		if (b1 != b2)
//			b1.recycle();
//		b2.recycle();
        b3.recycle();
        return path + ".thm";
    }

    public static Bitmap getBitmap(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(path);
    }

    public static String getShareNoteThumbnailPath(long thumbnailId) {
        String path = null;
        if (hasExternalStorage()) {
            //Android/data/com.thinkernote.ThinkerNote/files/Cache/
            path = FileUtils.ABS_PACKAGE_PRIV_PATH + FileUtils.FILE_CACHE_PATH;
        } else {
            //data/data/包名/cache/
            path = FileUtils.DEFAULT_INNER_PATH + FileUtils.FILE_CACHE_PATH;
        }
        return path + String.valueOf(thumbnailId) + ".jpg";
    }

    public static void deleteAtt(long attLocalId, int type) {
        String sdcardPath = getAttPath(attLocalId, "sdcard", type);
        if (sdcardPath != null) {
            File fSD = new File(sdcardPath);
            if (fSD.exists())
                fSD.delete();
            File fSDThm = new File(sdcardPath + ".thm");
            if (fSDThm.exists())
                fSDThm.delete();
        }

        String dataPath = getAttPath(attLocalId, "data", type);
        if (dataPath != null) {
            File fData = new File(dataPath);
            if (fData.exists())
                fData.delete();
            File fDataThm = new File(dataPath + ".thm");
            if (fDataThm.exists())
                fDataThm.delete();
        }
    }

    public static void deleteShareNoteThumbnail(long thumbAttId) {
        String thumbPath = getShareNoteThumbnailPath(thumbAttId);
        try {
            File f = new File(thumbPath);
            if (f.exists()) {
                f.delete();
                MLog.d(TAG, "delete thumbnail:" + thumbPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getMimeType(int type, String path) {
        if (type > 10000 && type < 20000)
            return "image/*";
        else if (type > 20000 && type < 30000)
            return "audio/*";
        else if (type > 30000 && type < 40000)
            return "vedio/*";
        else if (type == 40001)
            return "application/pdf";
        else if (type == 40002)
            return "text/plain";
        else if (type == 40003)
            return "application/msword";
        else if (type == 40004)
            return "application/rtf";
        else if (type == 40005)
            return "application/vnd.ms-powerpoint";
        else if (type == 40006 || type == 40007)
            return "text/html";
        else if (type == 40008)
            return "text/xml";
        else if (type == 40009)
            return "application/vnd.ms-excel";
        else if (type == 40010)
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        else if (type == 40011)
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        else if (type == 40012)
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        else if (type == 40013)
            return "message/rfc822";
        else {
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String extension = MimeTypeMap.getFileExtensionFromUrl(path);
            MLog.i(TAG, "extension=" + extension + " path=" + path);
            String mimeType = mimeTypeMap.getMimeTypeFromExtension(extension);
            MLog.i(TAG, "mimeType=" + mimeType);
            return mimeTypeMap.getMimeTypeFromExtension(extension);
        }
    }


    // 获取指定路径的图 ?
    public static Bitmap getImage(String path, int size) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = computeSampleSize(options, -1, size * size);
            MLog.i("size", options.inSampleSize + "   " + size);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static boolean isCanSendWeiboImage(int type) {
        if (type == 10001 || type == 10002 || type == 10004) {
            return true;
        }
        return false;
    }

}
