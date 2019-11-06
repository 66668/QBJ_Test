package com.thinkernote.ThinkerNote.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

public class BitmapUtils {
	private static final String TAG = "BitmapUtils";

	/**
	 * 浣垮ご鍍忓彉鐏?
	 * 
	 * @param drawable
	 */
	public static void porBecomeGrey(ImageView imageView, Drawable drawable) {
		drawable.mutate();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
		drawable.setColorFilter(cf);
		imageView.setImageDrawable(drawable);
	}

	/**
	 * 鎶婂浘鐗囧彉鎴愬渾瑙?
	 * 
	 * @param bitmap
	 *            闇?瑕佷慨鏀圭殑鍥剧墖
	 * @param pixels
	 *            鍦嗚鐨勫姬搴?
	 * @return 鍦嗚鍥剧墖
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, float pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, pixels, pixels, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

    /**
     * 浠庡浘鐗囨涓鍓竴寮犲渾瑙掑浘鐗?, 濡傛灉鍥剧墖鐨勫鎴栭珮灏忎簬瑁佸壀瀹芥垨楂橈紝 鍒欐寜瑁佸壀瀹介珮姣斾緥缂╁皬瑁佸壀鍖哄煙锛岀洿鍒拌鍓楂樺皬浜庣瓑浜庡浘鐗囧楂橈紱
     * 瑁佸壀寰楀埌鐨勫浘鐗囩殑瀹介珮姣斾负浼犲叆鐨勮鍓楂樻瘮
     * @param bitmap 鍘熷浘
     * @param corpWith 瑁佸壀瀹?
     * @param corpHeight 瑁佸壀楂?
     * @param roundPx 鍦嗚瑙掑害
     * @return 瑁佸壀鍚庣殑鍥剧墖
     */
    public static Bitmap corpAutoScale(Bitmap bitmap, int corpWith, int corpHeight, float roundPx) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = corpHeight/(float)corpWith;
        corpWith = width;
        corpHeight = height;
        while(true){
            if(corpWith*ratio <= corpHeight) {
                corpHeight = (int)(corpWith * ratio);
                break;
            }
            corpWith--;
        }

        int srcLeft = (width - corpWith)/2;
        int srcTop = (height - corpHeight)/2;

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect dstRect = new Rect(0, 0, corpWith, corpHeight);
        final Rect srcRect = new Rect(srcLeft, srcTop, corpWith + srcLeft, corpHeight + srcTop);
        final RectF rectF = new RectF(dstRect);

        Bitmap output = Bitmap.createBitmap(corpWith, corpHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        return output;
    }

    public static Bitmap corpAutoScaleCircle(Bitmap bitmap, int roundPx) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale_x, scale_y;
        scale_x = roundPx/(float)width;
        scale_y = roundPx/(float)height;

        if (scale_x != 1.0 && scale_y != 1.0) {
            Matrix matrix = new Matrix();
            matrix.postScale(scale_x, scale_y);
            Bitmap bitmap2;
            bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            bitmap.recycle();
            bitmap = bitmap2;
        }

        float ratio = 1.0f;//corpHeight/(float)corpWith;
        int corpWith = bitmap.getWidth();
        int corpHeight = bitmap.getHeight();

        int srcLeft = 0;
        int srcTop = 0;

        final int color = Color.GRAY;
        final Paint paint = new Paint();
        final Rect dstRect = new Rect(0, 0, corpWith, corpHeight);
        final Rect srcRect = new Rect(srcLeft, srcTop, corpWith + srcLeft, corpHeight + srcTop);

        Bitmap output = Bitmap.createBitmap(corpWith, corpHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(roundPx/2, roundPx/2, roundPx/2, paint);
        
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawCircle(roundPx/2, roundPx/2, roundPx/2, paint);

        return output;
    }

    /**
     * 浠庡浘鐗囨涓鍓竴寮犲渾瑙掑浘鐗?
     * @param bitmap 鍘熷浘
     * @param corpWith 瑁佸壀瀹?
     * @param corpHeight 瑁佸壀楂?
     * @param roundPx 鍦嗚瑙掑害
     * @return 瑁佸壀鍚庣殑鍥剧墖
     */
	public static Bitmap corp(Bitmap bitmap, int corpWith, int corpHeight, float roundPx) {
		int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int srcLeft = 0;
        int srcTop = 0;
        int dstLeft = 0;
        int dstTop = 0;
        Bitmap output = Bitmap.createBitmap(corpWith, corpHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		
		if(corpWith >= width){
            dstLeft = (corpWith -width)/2;
			corpWith = width;
		}else{
			srcLeft = (width - corpWith)/2;
		}
        if(corpHeight >= height){
            dstTop = (corpHeight - height)/2;
        	corpHeight = height;
        }else{
        	srcTop = (height - corpHeight)/2;
        }

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect dstRect = new Rect(dstLeft, dstTop, corpWith + dstLeft, corpHeight + dstTop);
		final Rect srcRect = new Rect(srcLeft, srcTop, corpWith + srcLeft, corpHeight + srcTop);
		final RectF rectF = new RectF(dstRect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

		return output;
	}

	/**
     * 灏嗗師鍥炬嫹鎸変竴瀹氬昂瀵镐繚瀛樺埌涓?涓柊鐨勮矾寰勶紝鍙敤浜庣敓鎴愮缉鐣ュ浘
	 * @param photoPath  --鍘熷浘璺粡
	 * @param outPath  --淇濆瓨缂╁浘
	 * @param newWidth --缂╁浘瀹藉害
	 * @param newHeight --缂╁浘楂樺害
	 */
	public static boolean copyPhoto(String photoPath, String outPath,
			int newWidth, int newHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 鑾峰彇杩欎釜鍥剧墖鐨勫鍜岄珮
		BitmapFactory.decodeFile(photoPath, options);

		// 璁＄畻缂╂斁姣?
        options.inSampleSize = computeSampleSize(options, newWidth, newHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

		return saveBitmapToFile(bitmap, outPath);
	}

	/**
	 * 灏哹itmap淇濆瓨涓烘枃浠?
	 * 
	 * @param bitmap 鍥剧墖
	 * @param outPath 杈撳嚭璺緞
	 * @return
	 */
	public static boolean saveBitmapToFile(Bitmap bitmap, String outPath) {
        return saveBitmapToFile(bitmap, outPath, 100);
	}

    public static boolean saveBitmapToFile(Bitmap bitmap, String outPath, int quality){
        File outFile = new File(outPath);
        try {
            if (outFile.exists()) {
                outFile.delete();
            } else {
                outFile.getParentFile().mkdirs();
            }
            outFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e1) {
            e1.printStackTrace();
            if (outFile.exists()) {
                outFile.delete();
            }
            return false;
        }
    }

	/**
	 * 灏咲rawable杞寲涓築itmap
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 鐢熸垚涓?涓浘鐗囩殑鍊掑奖
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
				width, height / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);

		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);
		return bitmapWithReflection;
	}

    /**
     * bitmap杞珺yteArray
     * @param bitmap
     * @return
     */
	public static byte[] bitmapToByteArray(Bitmap bitmap) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}

    /**
     * byteArray杞琤itmap
     * @param temp
     * @return
     */
	public static Bitmap byteArrayToBitmap(byte[] temp) {
		if (temp != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
			return bitmap;
		} else {
			return null;
		}
	}

    /**
     * 鏍规嵁鍥剧墖Uri浠庣浉鍐岃幏鍙栧浘鐗?
     * @param context
     * @param uri
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
	public static Bitmap getBitmapFromUri(Context context, Uri uri) throws FileNotFoundException, IOException{
        ContentResolver resolver = context.getContentResolver();
		return MediaStore.Images.Media.getBitmap(resolver, uri);
	}

    /**
     * 绛夋瘮渚嬪帇缂╁浘鐗囷紝灏嗗師鍥剧瓑姣斾緥缂╂斁锛屼笉浼氭媺鍗囨垨鍘嬬缉锛屼篃涓嶄細鍙戠敓瑁佸壀
     * @param path
     * @param maxWidth
     * @param maxHeight
     * @return
     */
	public static Bitmap compressionPicture(String path, int maxWidth, int maxHeight){
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);

		opts.inSampleSize = computeSampleSize(opts, -1, maxWidth * maxHeight);
		opts.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, opts);
	}

	/**
	 * compute Sample Size
	 * 璁＄畻鍥剧墖鍘嬬缉姣?
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
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

	/**
	 * compute Initial Sample Size
	 * 鍒濇璁＄畻鍘嬬缉姣?
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
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

    /**
     * 鑾峰彇鍥剧墖鐨勬柟鍚戝睘鎬?
     * @param path
     * @return
     */
	public static int getImageOrientation(String path){
		int orientation = 0;
		try {
			ExifInterface ef = new ExifInterface(path);
			int tag = ef.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
			if(tag == ExifInterface.ORIENTATION_ROTATE_90){
				orientation = 90;
			}else if (tag == ExifInterface.ORIENTATION_ROTATE_180) {
				orientation = 180;
			} else if (tag == ExifInterface.ORIENTATION_ROTATE_270) {
				orientation = 270;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return orientation;
	}
	
	/**
     * 灏嗗浘鐗囨棆杞琣ngle搴?
	 * @param bitmap 闇?瑕佹棆杞殑鍘熷浘
	 * @param angle 鏃嬭浆鐨勮搴?
	 * @param recycle 鏄惁recycle鍘熷浘
	 * @return 杩斿洖鏃嬭浆鍚庣殑鏂板浘鐗?
	 */
	public static Bitmap rotateImage(Bitmap bitmap, int angle, boolean recycle){
		if(angle != 0){
			// 涓嬮潰鐨勬柟娉曚富瑕佷綔鐢ㄦ槸鎶婂浘鐗囪浆涓?涓搴?
            Matrix m = new Matrix();  
            int width = bitmap.getWidth();  
            int height = bitmap.getHeight(); 
            m.setRotate(angle); // 鏃嬭浆angle搴?  
            Bitmap b = Bitmap.createBitmap(bitmap, 0, 0, width, height,  
                    m, true);// 浠庢柊鐢熸垚鍥剧墖
            if(recycle)
            	bitmap.recycle();
            return b;
		}
		return bitmap;
	}

    /**
     * 鍔犺浇鍥剧墖骞跺埗瀹氬帇缂╂瘮锛屼緥濡俹pt=2锛? 鍒欑敓鎴愬師鍥?1/2澶у皬鐨勫浘鐗?
     * @param path
     * @param opt
     * @return
     */
	public static Bitmap addImage(String path,int opt) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = opt;
		Bitmap b = BitmapFactory.decodeFile(path, opts);
		return b;
	}

    /**
     * 缂╂斁鍥剧墖,浼氬鑷村浘鐗囧彉褰?
     *
     * @param bmp
     * @param width
     * @param height
     * @return
     */
    public static Bitmap picZoom(Bitmap bmp, float width, float height) {
        int bmpWidth = bmp.getWidth();
        int bmpHeght = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale((float) width / bmpWidth, (float) height / bmpHeght);

        return Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeght, matrix, true);
    }

    public static Bitmap createBitmap(Context context, DisplayMetrics dm, int w, int h, String text, int textColor, Typeface font, int canvasColor, double scaleTo) {
       int size = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, (float)(scaleTo * 10), dm);
//        h = w = DisplayUtils.dp2px(context, w);
        w = size;
        h = size;

//        int width = dm.widthPixels;
//        int height = dm.heightPixels;
//        double xxx = Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
//        double inc = xxx/dm.densityDpi;

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(canvasColor);
        Paint p = new Paint();
        p.setColor(textColor);
        p.setTypeface(font);
        p.setAntiAlias(true);//鍘婚櫎閿娇
        p.setFilterBitmap(true);//瀵逛綅鍥捐繘琛屾护娉㈠鐞?
        p.setTextSize(DisplayUtils.dp2px(context, getFontSize(dm, scaleTo)));
//        p.setTextSize(size);
        float tX = (w - getTextLength(p, text)) / 2;
        float tY = (h - getFontHeight(p)) / 2 + getTextLeading(p);
        canvas.drawText(text, tX, tY, p);

//        Matrix matrix = new Matrix();
//        matrix.postScale((float) scaleTo, (float) scaleTo);
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return bitmap;
    }

    public static Bitmap createBitmap(Context context, DisplayMetrics dm, int w, int h, String text, int textColor, Typeface font, double scaleTo) {
        return createBitmap(context, dm, w, h, text, textColor, font, Color.WHITE, scaleTo);
    }

    private static float getTextLeading(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.leading - fontMetrics.ascent;
    }

    private static float getTextLength(Paint paint, String text) {
        return paint.measureText(text);
    }

    private static float getFontHeight(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.descent - fontMetrics.ascent;
    }

    private static int getFontSize(DisplayMetrics dm, double scaleTo) {
//        float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, (float)scaleTo, dm);
        return (int) (scaleTo * dm.xdpi * (1.0f/2.54f));
    }
}
