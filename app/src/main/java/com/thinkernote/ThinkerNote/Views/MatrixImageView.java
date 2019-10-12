package com.thinkernote.ThinkerNote.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * @author LinJ
 * @ClassName: MatrixImageView
 * @Description: 甯︽斁澶с?佺缉灏忋?佺Щ鍔ㄦ晥鏋滅殑ImageView
 * @date 2015-1-7 涓婂崍11:15:07
 */
@SuppressLint("AppCompatCustomView")
public class MatrixImageView extends ImageView {
    private final static String TAG = "MatrixImageView";
    private GestureDetector mGestureDetector;
    /**
     * 妯℃澘Matrix锛岀敤浠ュ垵濮嬪寲
     */
    private Matrix mMatrix = new Matrix();
    /**
     * 鍥剧墖闀垮害
     */
    private float mImageWidth;
    /**
     * 鍥剧墖楂樺害
     */
    private float mImageHeight;

    public MatrixImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        MatrixTouchListener mListener = new MatrixTouchListener();
        setOnTouchListener(mListener);
        mGestureDetector = new GestureDetector(getContext(), new GestureListener(mListener));
        //鑳屾櫙璁剧疆涓篵alck
        setBackgroundColor(Color.BLACK);
        //灏嗙缉鏀剧被鍨嬭缃负FIT_CENTER锛岃〃绀烘妸鍥剧墖鎸夋瘮渚嬫墿澶?/缂╁皬鍒癡iew鐨勫搴︼紝灞呬腑鏄剧ず
        setScaleType(ScaleType.FIT_CENTER);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        //璁剧疆瀹屽浘鐗囧悗锛岃幏鍙栬鍥剧墖鐨勫潗鏍囧彉鎹㈢煩闃?
        mMatrix.set(getImageMatrix());
        float[] values = new float[9];
        mMatrix.getValues(values);
        //鍥剧墖瀹藉害涓哄睆骞曞搴﹂櫎缂╂斁鍊嶆暟
        mImageWidth = getWidth() / values[Matrix.MSCALE_X];
        mImageHeight = (getHeight() - values[Matrix.MTRANS_Y] * 2) / values[Matrix.MSCALE_Y];
    }

    public class MatrixTouchListener implements OnTouchListener {
        /**
         * 鎷栨媺鐓х墖妯″紡
         */
        private static final int MODE_DRAG = 1;
        /**
         * 鏀惧ぇ缂╁皬鐓х墖妯″紡
         */
        private static final int MODE_ZOOM = 2;
        /**
         * 涓嶆敮鎸丮atrix
         */
        private static final int MODE_UNABLE = 3;
        /**
         * 鏈?澶х缉鏀剧骇鍒?
         */
        float mMaxScale = 6;
        /**
         * 鍙屽嚮鏃剁殑缂╂斁绾у埆
         */
        float mDobleClickScale = 2;
        private int mMode = 0;//
        /**
         * 缂╂斁寮?濮嬫椂鐨勬墜鎸囬棿璺?
         */
        private float mStartDis;
        /**
         * 褰撳墠Matrix
         */
        private Matrix mCurrentMatrix = new Matrix();

        /**
         * 鐢ㄤ簬璁板綍寮?濮嬫椂鍊欑殑鍧愭爣浣嶇疆
         */
        private PointF startPoint = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    //璁剧疆鎷栧姩妯″紡
                    mMode = MODE_DRAG;
                    startPoint.set(event.getX(), event.getY());
                    isMatrixEnable();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    reSetMatrix();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mMode == MODE_ZOOM) {
                        setZoomMatrix(event);
                    } else if (mMode == MODE_DRAG) {
                        setDragMatrix(event);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (mMode == MODE_UNABLE) return true;
                    mMode = MODE_ZOOM;
                    mStartDis = distance(event);
                    break;
                default:
                    break;
            }

            return mGestureDetector.onTouchEvent(event);
        }

        public void setDragMatrix(MotionEvent event) {
            if (isZoomChanged()) {
                float dx = event.getX() - startPoint.x; // 寰楀埌x杞寸殑绉诲姩璺濈
                float dy = event.getY() - startPoint.y; // 寰楀埌x杞寸殑绉诲姩璺濈
                //閬垮厤鍜屽弻鍑诲啿绐?,澶т簬10f鎵嶇畻鏄嫋鍔?
                if (Math.sqrt(dx * dx + dy * dy) > 10f) {
                    startPoint.set(event.getX(), event.getY());
                    //鍦ㄥ綋鍓嶅熀纭?涓婄Щ鍔?
                    mCurrentMatrix.set(getImageMatrix());
                    float[] values = new float[9];
                    mCurrentMatrix.getValues(values);
                    dx = checkDxBound(values, dx);
                    dy = checkDyBound(values, dy);
                    mCurrentMatrix.postTranslate(dx, dy);
                    setImageMatrix(mCurrentMatrix);
                }
            }
        }

        /**
         * 鍒ゆ柇缂╂斁绾у埆鏄惁鏄敼鍙樿繃
         *
         * @return true琛ㄧず闈炲垵濮嬪??, false琛ㄧず鍒濆鍊?
         */
        private boolean isZoomChanged() {
            float[] values = new float[9];
            getImageMatrix().getValues(values);
            //鑾峰彇褰撳墠X杞寸缉鏀剧骇鍒?
            float scale = values[Matrix.MSCALE_X];
            //鑾峰彇妯℃澘鐨刋杞寸缉鏀剧骇鍒紝涓よ?呭仛姣旇緝
            mMatrix.getValues(values);
            return scale != values[Matrix.MSCALE_X];
        }

        /**
         * 鍜屽綋鍓嶇煩闃靛姣旓紝妫?楠宒y锛屼娇鍥惧儚绉诲姩鍚庝笉浼氳秴鍑篒mageView杈圭晫
         *
         * @param values
         * @param dy
         * @return
         */
        private float checkDyBound(float[] values, float dy) {
            float height = getHeight();
            if (mImageHeight * values[Matrix.MSCALE_Y] < height)
                return 0;
            if (values[Matrix.MTRANS_Y] + dy > 0)
                dy = -values[Matrix.MTRANS_Y];
            else if (values[Matrix.MTRANS_Y] + dy < -(mImageHeight * values[Matrix.MSCALE_Y] - height))
                dy = -(mImageHeight * values[Matrix.MSCALE_Y] - height) - values[Matrix.MTRANS_Y];
            return dy;
        }

        /**
         * 鍜屽綋鍓嶇煩闃靛姣旓紝妫?楠宒x锛屼娇鍥惧儚绉诲姩鍚庝笉浼氳秴鍑篒mageView杈圭晫
         *
         * @param values
         * @param dx
         * @return
         */
        private float checkDxBound(float[] values, float dx) {
            float width = getWidth();
            if (mImageWidth * values[Matrix.MSCALE_X] < width)
                return 0;
            if (values[Matrix.MTRANS_X] + dx > 0)
                dx = -values[Matrix.MTRANS_X];
            else if (values[Matrix.MTRANS_X] + dx < -(mImageWidth * values[Matrix.MSCALE_X] - width))
                dx = -(mImageWidth * values[Matrix.MSCALE_X] - width) - values[Matrix.MTRANS_X];
            return dx;
        }

        /**
         * 璁剧疆缂╂斁Matrix
         *
         * @param event
         */
        private void setZoomMatrix(MotionEvent event) {
            //鍙湁鍚屾椂瑙﹀睆涓や釜鐐圭殑鏃跺?欐墠鎵ц
            if (event.getPointerCount() < 2) return;
            float endDis = distance(event);// 缁撴潫璺濈
            if (endDis > 10f) { // 涓や釜鎵嬫寚骞舵嫝鍦ㄤ竴璧风殑鏃跺?欏儚绱犲ぇ浜?10
                float scale = endDis / mStartDis;// 寰楀埌缂╂斁鍊嶆暟
                mStartDis = endDis;//閲嶇疆璺濈
                mCurrentMatrix.set(getImageMatrix());//鍒濆鍖朚atrix
                float[] values = new float[9];
                mCurrentMatrix.getValues(values);

                scale = checkMaxScale(scale, values);
                setImageMatrix(mCurrentMatrix);
            }
        }

        /**
         * 妫?楠宻cale锛屼娇鍥惧儚缂╂斁鍚庝笉浼氳秴鍑烘渶澶у?嶆暟
         *
         * @param scale
         * @param values
         * @return
         */
        private float checkMaxScale(float scale, float[] values) {
            if (scale * values[Matrix.MSCALE_X] > mMaxScale)
                scale = mMaxScale / values[Matrix.MSCALE_X];
            mCurrentMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
            return scale;
        }

        /**
         * 閲嶇疆Matrix
         */
        private void reSetMatrix() {
            if (checkRest()) {
                mCurrentMatrix.set(mMatrix);
                setImageMatrix(mCurrentMatrix);
            }
        }

        /**
         * 鍒ゆ柇鏄惁闇?瑕侀噸缃?
         *
         * @return 褰撳墠缂╂斁绾у埆灏忎簬妯℃澘缂╂斁绾у埆鏃讹紝閲嶇疆
         */
        private boolean checkRest() {
            float[] values = new float[9];
            getImageMatrix().getValues(values);
            //鑾峰彇褰撳墠X杞寸缉鏀剧骇鍒?
            float scale = values[Matrix.MSCALE_X];
            //鑾峰彇妯℃澘鐨刋杞寸缉鏀剧骇鍒紝涓よ?呭仛姣旇緝
            mMatrix.getValues(values);
            return scale < values[Matrix.MSCALE_X];
        }

        /**
         * 鍒ゆ柇鏄惁鏀寔Matrix
         */
        private void isMatrixEnable() {
            //褰撳姞杞藉嚭閿欐椂锛屼笉鍙缉鏀?
            if (getScaleType() != ScaleType.CENTER) {
                setScaleType(ScaleType.MATRIX);
            } else {
                mMode = MODE_UNABLE;//璁剧疆涓轰笉鏀寔鎵嬪娍
            }
        }

        /**
         * 璁＄畻涓や釜鎵嬫寚闂寸殑璺濈
         *
         * @param event
         * @return
         */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 浣跨敤鍕捐偂瀹氱悊杩斿洖涓ょ偣涔嬮棿鐨勮窛绂? */
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        /**
         * 鍙屽嚮鏃惰Е鍙?
         */
        public void onDoubleClick() {
            float scale = isZoomChanged() ? 1 : mDobleClickScale;
            mCurrentMatrix.set(mMatrix);//鍒濆鍖朚atrix
            mCurrentMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
            setImageMatrix(mCurrentMatrix);
        }
    }


    private class GestureListener extends SimpleOnGestureListener {
        private final MatrixTouchListener listener;

        public GestureListener(MatrixTouchListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            //鎹曡幏Down浜嬩欢
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //瑙﹀彂鍙屽嚮浜嬩欢
            listener.onDoubleClick();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }


        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            simpleOnGestureListener.onSingleTapConfirmed(e);
            return super.onSingleTapConfirmed(e);
        }

    }


    private SimpleOnGestureListener simpleOnGestureListener;

    public void setSimpleOnGestureListener(SimpleOnGestureListener listener) {
        simpleOnGestureListener = listener;
    }
}