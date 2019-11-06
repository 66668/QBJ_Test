package com.thinkernote.ThinkerNote.utils.actfun;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Handler;
import android.os.Message;

import com.thinkernote.ThinkerNote.utils.MLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * 录音封装
 */
public class TNRecord implements OnInfoListener, OnErrorListener{
	private static final String TAG = "TNRecord";
	private static final int MAX_VU_SIZE = 101;
	private static final int HZ = 100;
	private static final int MAX_FILE_SIZE = 20 * 1024 * 1024 - 200 * 1024;
	
	private int mUsedFileSize = 0;

	private MediaRecorder mRecorder;
	private Vector<String> mRecordFragments;
	private Handler mHandler;

	private static final String SUFFIX = ".mp3";
	private int second = 0;
	private int minute = 0;
	private int c = 0;
	private Timer mTimer;
	private String mCurrentRecordPath;
	private String mFinalRecordPath;

	public TNRecordState mState = TNRecordState.stop;

	public enum TNRecordState {
		stop, recording, pause
	}

	public TNRecord(Handler handler) {
		mRecordFragments = new Vector<String>();

		mHandler = handler;
	}

	private void initRecord() throws IllegalStateException, IOException {
		MLog.d(TAG, "initRecord");
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

		// 录音文件保存这里
		mCurrentRecordPath = getTmpPath();
		mRecorder.setOutputFile(mCurrentRecordPath);
		mRecorder.setMaxFileSize(MAX_FILE_SIZE - mUsedFileSize);
		MLog.d(TAG, "setMaxFileSize=" + (MAX_FILE_SIZE - mUsedFileSize));
		mRecorder.setOnInfoListener(this);
		mRecorder.setOnErrorListener(this);
		mRecorder.prepare();

		if (mState == TNRecordState.stop) {
			mRecordFragments.clear();
			delete(mFinalRecordPath);
		}
		mRecordFragments.add(mCurrentRecordPath);
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		switch(what){
		case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
			MLog.e(TAG, "onInfo: max file size reached");
			asynStop(5);
			break;
		}
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		MLog.e(TAG, "record onError: what=" + what + ", extra=" + extra);
//		if(what == MediaPlayer.MEDIA_ERROR_SERVER_DIED){
//			return;
//		}
		asynStop(9);
	}

	public void start() {
		MLog.d(TAG, "start");
		// 需检查SDCrard
		if (!TNUtilsAtt.isHasSpace()) {
			mHandler.sendEmptyMessage(6);
			return;
		}

		if (mState == TNRecordState.recording) {
			return;
		}

		try {
			initRecord();
			mState = TNRecordState.recording;
			startTiming();
			mRecorder.start();
		} catch (Exception e) {
			e.printStackTrace();
			stop();
		}
	}

	public void pause() {
		MLog.d(TAG, "pause");
		if (mState != TNRecordState.recording) {
			return;
		}
		mState = TNRecordState.pause;		
		recordStop();
		
		File f = new File(mCurrentRecordPath);
		if(f.exists()){
			mUsedFileSize += f.length();
			MLog.d(TAG, "used file size=" + mUsedFileSize + " path=" + mCurrentRecordPath);
		}
	}

	public void stop() {
		MLog.d(TAG, "stop");
		mState = TNRecordState.stop;
		recordStop();
		mergerFragments();
		second = 0;
		minute = 0;
		c = 0;
		mUsedFileSize = 0;
	}
	
	public void asynStop(final int what){
		MLog.d(TAG, "asynStop");
		mState = TNRecordState.stop;
		recordStop();
		second = 0;
		minute = 0;
		c = 0;
		mUsedFileSize = 0;
		new Thread(){

			@Override
			public void run() {
				mergerFragments();
				mHandler.sendEmptyMessage(what);
				super.run();
			}
			
		}.run();		
	}

	public void cancle() {
		MLog.d(TAG, "cancle");
		mState = TNRecordState.stop;
		recordStop();
		deleteListRecord();
	}

	public boolean isPause() {
		return mState == TNRecordState.pause;
	}

	public boolean isStop() {
		return mState == TNRecordState.stop;
	}

	public boolean isRecording() {
		return mState == TNRecordState.recording;
	}

	private void recordStop() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mRecorder != null) {
			try {
				mRecorder.stop();
				mRecorder.release();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			mRecorder = null;
		}
	}

	private String getTmpPath() {
		String name = String.valueOf(System.currentTimeMillis()) + SUFFIX;
		String path = TNUtilsAtt.getRecordTempPath(name);
		try {
			File f = new File(path);
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		MLog.d(TAG, path);
		return path;
	}

	private void startTiming() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				Message amplitude = new Message();
				amplitude.what = 3;
				amplitude.arg1 = getAmplitude();
				mHandler.sendMessage(amplitude);

				if (c >= (1000 / HZ)) {
					c = 0;
					second++;
					if (second >= 60) {
						second = 0;
						minute++;
					}

					Message time = new Message();
					time.what = 4;
					time.arg1 = minute;
					time.arg2 = second;
					mHandler.sendMessage(time);
				}
				c++;
			}
		};
		mTimer = new Timer();
		mTimer.schedule(timerTask, 0, HZ);
	}

	private int getAmplitude() {
		if (mState == TNRecordState.recording) {
			int vuSize = MAX_VU_SIZE * mRecorder.getMaxAmplitude() / 32768;
			if (vuSize >= MAX_VU_SIZE) {
				vuSize = MAX_VU_SIZE - 1;
			}
			return vuSize;
		} else {
			return 0;
		}
	}

	private void mergerFragments() {
		// 创建音频文件,合并的文件放这里
		MLog.d(TAG, "record count=" + mRecordFragments.size());
		if (mRecordFragments.size() == 1) {
			mFinalRecordPath = mRecordFragments.get(0);
			File f = new File(mFinalRecordPath);
			MLog.d(TAG, "file length=" + f.length());
			if(!f.exists() || f.length() == 0){
				mFinalRecordPath = null;
			}
			return;
		}
		mFinalRecordPath = getTmpPath();
		File outFile = new File(mFinalRecordPath);
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(outFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// list里面为暂停录音 所产生的 几段录音文件的名字，中间几段文件的减去前面的6个字节头文件
		for (int i = 0; i < mRecordFragments.size(); i++) {
			File file = new File((String) mRecordFragments.get(i));
			try {
				if(file.length() == 0){
					continue;
				}
				FileInputStream fileInputStream = new FileInputStream(file);
				// byte[] myByte = new byte[fileInputStream.available()];
				// 文件长度
				// int length = myByte.length;
				byte[] myByte = new byte[4096];
				int len = -1;
				// 头文件
				if (i == 0) {
					while ((len = fileInputStream.read(myByte)) != -1) {
						fileOutputStream.write(myByte, 0, len);
					}
				}
				// 之后的文件，去掉头文件就可以了
				else {
					boolean isFirst = true;
					while ((len = fileInputStream.read(myByte)) != -1) {
						if (isFirst) {
							isFirst = false;
							fileOutputStream.write(myByte, 6, len - 6);
						} else {
							fileOutputStream.write(myByte, 0, len);
						}
					}
				}
				fileOutputStream.flush();
				fileInputStream.close();
				MLog.d(TAG, "合成文件长度=" + outFile.length());
				if(outFile.length() == 0){
					mFinalRecordPath = null;
					outFile.delete();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 结束后关闭流
		try {
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 合成一个文件后，删除之前暂停录音所保存的零碎合成文件
		deleteListRecord();
	}

	// public void clearTmpFile(){
	// File dir = new File(Environment.getExternalStorageDirectory().getPath()
	// + "/audioRecord/");
	// if(dir.exists() && dir.isDirectory()){
	// for(File child : dir.listFiles()){
	// child.delete();
	// }
	// }
	// }

	public String getRecordTmpPath() {
		if (mState == TNRecordState.stop) {
			return mFinalRecordPath;
		} else
			return null;
	}

	private void deleteListRecord() {
		for (int i = 0; i < mRecordFragments.size(); i++) {
			delete(mRecordFragments.get(i));
		}
	}

	private void delete(String path) {
		if (path == null) {
			return;
		}
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}
}
