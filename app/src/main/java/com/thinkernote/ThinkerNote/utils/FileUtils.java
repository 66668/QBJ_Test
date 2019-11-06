package com.thinkernote.ThinkerNote.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import android.os.Environment;
import android.os.StatFs;

public class FileUtils {
	private static final String TAG = "FileUtils";

    /**
     * 鑾峰彇鏂囦欢鍚庣紑
     * @param fileName
     * @return
     */
	public static String getAttSuffix(String fileName) {
		int dot = fileName.lastIndexOf(".");
		if(dot < 0){
			return "";
		}
		return fileName.substring(dot, fileName.length());
	}

    /**
     * 鑾峰彇sdcard鎴栫郴缁熷唴瀛樺彲鐢ㄧ┖闂?
     * @param type
     * @return
     */
	@SuppressWarnings("deprecation")
	public static long availableSpace(String type) {
		File dir = null;
		if (type.equals("sdcard")) {
			if (hasExternalStorage()) {
				dir = Environment.getExternalStorageDirectory();
			} else {
				return 0;
			}
		} else if (type.equals("data")) {
			dir = Environment.getDataDirectory();
		}
		StatFs sf = new StatFs(dir.getPath());
		long size = (long) sf.getBlockSize() * (long) sf.getAvailableBlocks();
		return size;
	}

    /**
     * 鑾峰彇鏂囦欢澶瑰崰鐢ㄧ┖闂村ぇ灏?
     * @param file
     * @return
     */
	public static long getFolderSize(File file){
        long size = 0;
        File[] fileList = file.listFiles();
        for (int i = 0; i < fileList.length; i++){
        	if (fileList[i].isDirectory()){
        		if(!fileList[i].getName().equals("user"))
        			size = size + getFolderSize(fileList[i]);
        	} else {
        		size = size + fileList[i].length();
        	}
        }
        return size;
    }

    /**
     * 鍒ゆ柇鏄惁鏈夊缃瓨鍌ㄥ崱
     * @return
     */
	public static boolean hasExternalStorage() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

    /**
     * 绉诲姩鏂囦欢
     * @param srcPath
     * @param oldPath
     * @throws Exception
     */
	public static void moveFile(String srcPath, String oldPath)
			throws Exception {
		File srcFile = new File(srcPath);
		if (!srcFile.exists()) {
			return;
		}
		copyFile(srcPath, oldPath);
		srcFile.delete();
	}

    /**
     * 鎷疯礉鏂囦欢
     * @param sFile
     * @param tFile
     * @throws Exception
     */
	public static void copyFile(String sFile, String tFile) throws Exception {
		saveStreamToFile(new FileInputStream(sFile), tFile);
	}

    /**
     * 灏嗚緭鍏ユ祦涓鍙栨垚瀛楄妭鏁扮粍
     * @param inStream
     * @return
     * @throws Exception
     */
	public static byte[] readStream(InputStream inStream) throws Exception {
		byte[] buffer = new byte[1024];
		int len = -1;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inStream.close();
		return data;
	}
	
	/**
	 * 璇诲彇閮ㄥ垎鏂囦欢锛屼粠start寮?濮嬶紝闀垮害size
	 * @param targetFile
	 * @param start
	 * @param size
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFilePart(File targetFile, int start, int size) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(targetFile, "r");
		FileChannel read = raf.getChannel();
		MappedByteBuffer bb = read.map(FileChannel.MapMode.READ_ONLY, start, size);
		byte[] data = new byte[size];
		bb.get(data);
		bb.clear();
		read.close();
		raf.close();
		
		return data;
	}
	
	/**
	 * 灏嗘祦in鍐欏叆鍒版枃浠秚argetFile鐨勪綅缃畇tart鍚庨潰锛岄暱搴ize
	 * @param targetFile
	 * @param in
	 * @param start
	 * @param size
	 * @throws IOException
	 */
	public static void saveStreamToLocation(File targetFile, InputStream in, int start, int size) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(targetFile, "rws");
		FileChannel writer = raf.getChannel();
		MappedByteBuffer cc = null;
		if(start > targetFile.length()){
			cc = writer.map(FileChannel.MapMode.READ_WRITE, targetFile.length(), size);
		}else{
			cc = writer.map(FileChannel.MapMode.READ_WRITE, start, size);
		}
        byte[] buffer = new byte[size];
        in.read(buffer);
        cc.put(buffer);
        cc.clear();
        writer.close();
        raf.close();
	}
	
	/**
	 * 灏嗘枃浠秙rcFile鍐欏叆鍒版枃浠秚argetFile鐨勪綅缃畇tart鍚庨潰锛岄暱搴ize
	 * @param targetFile
	 * @param srcFile
	 * @param start
	 * @param size
	 * @throws IOException
	 */
	public static void saveStreamToLocation(File targetFile, File srcFile, int start, long size) throws IOException {
		RandomAccessFile readRaf = new RandomAccessFile(srcFile, "r");
		FileChannel read = readRaf.getChannel();
		RandomAccessFile writeRaf = new RandomAccessFile(targetFile, "rws");
		FileChannel writer = writeRaf.getChannel();
		MappedByteBuffer bb, cc = null;
		bb = read.map(FileChannel.MapMode.READ_ONLY, 0, srcFile.length());
		cc = writer.map(FileChannel.MapMode.READ_WRITE, start, size);
        cc.put(bb);
        bb.clear();
        cc.clear();
        read.close();
        readRaf.close();
        writer.close();
        writeRaf.close();
	}

	/**
	 * 灏嗘祦淇濆瓨鍒版枃浠?
	 * @param is
	 * @param outPath
	 * @throws Exception
	 */
	public static void saveStreamToFile(InputStream is, String outPath)
			throws Exception {
		// 鏂板缓鏂囦欢杈撳叆娴佸苟瀵瑰畠杩涜缂撳啿
		InputStream inBuff = new BufferedInputStream(is);
		// 鏂板缓鏂囦欢杈撳嚭娴佸苟瀵瑰畠杩涜缂撳啿
		File out = new File(outPath);
		if (!out.getParentFile().exists()) {
			out.getParentFile().mkdirs();
		}
		if(!out.exists())
			out.createNewFile();
		OutputStream outBuff = new BufferedOutputStream(new FileOutputStream(
				out));
		// 缂撳啿鏁扮粍
		byte[] b = new byte[4096];
		int len;
		while ((len = inBuff.read(b)) != -1) {
			outBuff.write(b, 0, len);
		}
		// 鍒锋柊姝ょ紦鍐茬殑杈撳嚭娴?
		outBuff.flush();
		// 鍏抽棴娴?
		inBuff.close();
		outBuff.close();
	}

	/**
	 * 灏嗗瓧绗︿覆鍐欏埌鏂囦欢涓?
	 * @param outPath
	 * @param content
	 * @param append
	 * @throws IOException
	 */
	public static void writeTextToFile(String outPath, String content,
			boolean append) throws IOException {
		FileWriter fw = null;
		try {
			File outFile = new File(outPath);
			if(!outFile.getParentFile().exists()){
				outFile.getParentFile().mkdirs();
			}
			if(!outFile.exists())
				outFile.createNewFile();
			
			fw = new FileWriter(outFile, append);
			fw.write(content, 0, content.length());
			fw.flush();
			fw.close();
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}

	/**
	 * 璇诲彇鏂囦欢鎴愬瓧绗︿覆
	 * @param filePath
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readTextFile(String filePath)
			throws FileNotFoundException, IOException {
		File f = new File(filePath);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			StringBuffer sb = new StringBuffer();
			String str = null;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
			return sb.toString();
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

    /**
     * 鍒犻櫎鏂囦欢
     * @param path
     */
	public static void deleteFile(String path){
		File f = new File(path);
		if(f.exists()){
			f.delete();
		}
	}

    /**
     * 鍒ゆ柇鏂囦欢鏄惁瀛樺湪
     * @param path
     * @return
     */
	public static boolean isFileExists(String path){
		File f = new File(path);
		return f.exists();
	}
	
	/**
	 * 閫掑綊鍒犻櫎鏂囦欢鍜屾枃浠跺す
	 * @param dir
	 */
	public static void recursionDelDirectory(File dir){
		if(dir.isDirectory()){
			for(File f : dir.listFiles()){
				recursionDelDirectory(f);
			}
		}
		dir.delete();
	}
	
	/**
	 * 閫掑綊娓呯┖鏂囦欢澶归噷鐨勬枃浠讹紝浣嗕笉鍒犻櫎鏂囦欢澶?
	 * @param dir
	 */
	public static void recursionDelFile(File dir){
		if(dir.isDirectory()){
			for(File f : dir.listFiles()){
				recursionDelFile(f);
			}
		}else{
			dir.delete();
		}
	}
}
