package com.android.baselibrary.picture;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Author: geng
 * Date: 2016/2/16
 */

/**
 * 能显示大长图
 * TODO: 解决ViewPager的冲突, 重写自己的ViewPager
 */
public class XImageView extends ImageView
{
    public final static String TAG = "SuperImageView";

    /**
     * 默认双击放大的时间
     */
    private final static int DOUBLE_SCALE_TIME = 100;

    /**
     * Gesture Detector
     */
    private XGestureDetector mGestureDetector = null;

    /**
     * Action Listener
     */
    private OnActionListener mActionListener = null;

    /**
     * bitmap 的管理器
     */
    private BitmapManager mBitmapManager = null;


    private float mDisplayDensity = 1;

    private final static Paint mPaint = new Paint();
    static {
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
    }

    public XImageView(Context context)
    {
        super(context);
        initialize();
    }

    public XImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialize();
    }

    public XImageView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize()
    {
        Context context = getContext();
        mDisplayDensity = context.getResources().getDisplayMetrics().density;
        float dpi = mDisplayDensity * 160.0f;
        mPhysicalCoeff = SensorManager.GRAVITY_EARTH  * 39.37f * dpi * 0.84f;

        mGestureDetector = new XGestureDetector(context);

        super.setOnLongClickListener(new OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                return false;
            }
        });
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener)
    {
        // nothing
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        mGestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                interceptParentTouchEvent(true);
                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                interceptParentTouchEvent(false);
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int width = getWidth();
        int height = getHeight();
        if (mBitmapManager != null) {
            boolean show = mBitmapManager.drawVisibleBitmap(canvas, width, height);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();

        if (mBitmapManager != null) {
            mBitmapManager.onDestroy();
        }
    }

    /**
     * 是否劫持输入事件, ViewPager
     */
    private void interceptParentTouchEvent(boolean intercept)
    {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(intercept);
        }
    }

    public void setImage(Bitmap bitmap)
    {
        setImage(bitmap, false);
    }

    /**
     * Bitmap 转换为 InputStream, 使用 BitmapRegionDecoder 管理
     * @param bitmap 图片
     * @param cache 是否需要将bitmap 保存为文件再 转换为InputStream
     */
    public void setImage(Bitmap bitmap, boolean cache)
    {
        if (mBitmapManager != null) {
            mBitmapManager.onDestroy();
        }

        mBitmapManager = new BitmapManager(this, bitmap, cache, mManagerCallback);
    }


    /**
     * @param path path
     */
    public void setImage(String path)
    {
        setImage(new File(path), Bitmap.Config.RGB_565);
    }

    public void setImage(String path, Bitmap.Config config)
    {
        setImage(new File(path), config);
    }

    /**
     * @param file File
     */
    public void setImage(File file)
    {
        setImage(file, Bitmap.Config.RGB_565);
    }

    public void setImage(File file, Bitmap.Config config)
    {
        if (file == null || !file.exists()) {
            setImage((InputStream) null, config);
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(file);
            setImage(fis, config);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param inputStream 输入流
     */
    public void setImage(InputStream inputStream)
    {
        setImage(inputStream, Bitmap.Config.RGB_565);
    }

    public void setImage(InputStream is, Bitmap.Config config)
    {
        if (mBitmapManager != null) {
            mBitmapManager.onDestroy();
        }

        mBitmapManager = new BitmapManager(this, is, config, mManagerCallback);
    }

    /**
     * 设置监听
     * @param listener
     */
    public void setActionListener(OnActionListener listener)
    {
        mActionListener = listener;
    }

    /**
     * 缩放到指定的大小, 起始是以当前的大小为准
     * 并且以屏幕中心进行缩放
     */
    public void scaleImage(float dest, boolean smooth, int smoothTime)
    {
        if (mBitmapManager != null) {
            mBitmapManager.scaleFromCenterTo(dest, smooth, smoothTime);
        }
    }

    /**
     * 以一点为中心缩放图片
     * @param cx 中心点
     * @param cy 中心点
     * @param dest 缩放的目标倍数， 这是以当前的放大倍数来计算的
     * @param smooth 是否使用动画
     * @param smoothTime 动画时间
     */
    public void scaleImage(int cx, int cy, float dest, boolean smooth, int smoothTime)
    {
        if (mBitmapManager != null) {
            mBitmapManager.scaleTo(cx, cy, dest, smooth, smoothTime);
        }
    }

    /**
     * 放大到最大适应View（就是图片宽高 >= View的宽高）
     */
    public void scaleToMaxFitView(int cx, int cy, boolean smooth, int smoothTime)
    {
        if (mBitmapManager != null) {
            mBitmapManager.scaleToMaxFitView(cx, cy, smooth, smoothTime);
        }
    }

    /**
     * 放大到最小适应View (就是图片宽高 <= View的宽高)
     */
    public void scaleToMinFitView(int cx, int cy, boolean smooth, int smoothTime)
    {
        if (mBitmapManager != null) {
            mBitmapManager.scaleToMinFitView(cx, cy, smooth, smoothTime);
        }
    }

    /**
     * 获取当前图片的缩放的倍数
     * @return 放大的倍数, 相对图片的原始图片的尺寸来说的
     */
    public float getScaleFactor()
    {
        return (mBitmapManager != null) ? mBitmapManager.getCurScaleFactor() : 0f;
    }

    /**
     * 滑动图片, 返回当前已经到达的边界
     * LEFT
     * RIGHT
     * TOP
     * BOTTOM
     * @param dx x轴滑动的像素
     * @param dy y轴滑动的像素
     * @return 当已经到达的边界的 与 值
     */
    public int scrollImage(int dx, int dy)
    {
        return (mBitmapManager != null) ? mBitmapManager.offsetShowBitmap(dx, dy) : 0;
    }

    /**
     * 获取图片的尺寸，注意最好在 onSetImageFinished() 之后获取这个值
     */
    public Rect getImageRect()
    {
        return (mBitmapManager != null) ? mBitmapManager.getImageRect() : new Rect();
    }


    private BitmapManager.IManagerCallback
            mManagerCallback = new BitmapManager.IManagerCallback()
    {
        @Override
        public void onSetImageFinished(boolean success, Rect image)
        {
            if (mActionListener != null) {
                mActionListener.onSetImageFinished(success, image);
            }
        }
    };

    /********************* Gesture Detector & Listener *******************************/

    private XOnGestureListener mGestureListener = new XOnGestureListener();
    private class XGestureDetector extends GestureDetector
    {
        /**
         * 放大手势检测
         */
        private ScaleGestureDetector mScaleDetector = null;

        public XGestureDetector(Context context)
        {
            super(context, mGestureListener);
            mScaleDetector = new ScaleGestureDetector(context, mGestureListener);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            stopFling();

            mScaleDetector.onTouchEvent(event);

            return super.onTouchEvent(event);
        }
    }

    private class XOnGestureListener extends
            GestureDetector.SimpleOnGestureListener implements ScaleGestureDetector.OnScaleGestureListener
    {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e)
        {
            int x = (int) e.getX();
            int y = (int) e.getY();
//            Log.e(TAG, "On Tapped: X: " + x + " Y: " + y + " Is: " + (mBitmapManager != null && mBitmapManager.isTapOnImage(x, y)));
            if (mActionListener != null) {
                mActionListener.onSingleTapped(e, mBitmapManager != null && mBitmapManager.isTapOnImage(x, y));
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            if (mBitmapManager == null) {
                return false;
            }

            int x = (int) e.getX();
            int y = (int) e.getY();
            //mBitmapManager.scaleToFitView(x, y, true, DOUBLE_SCALE_TIME);
            if (mActionListener != null) {
                mActionListener.onDoubleTapped(e);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e)
        {
            if (mActionListener != null) {
                mActionListener.onLongPressed(e);
            }
        }

        /*************************************滑动****************************************/
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            if (mBitmapManager == null) {
                return false;
            }

            int state = mBitmapManager.offsetShowBitmap((int) -distanceX, (int) -distanceY);

            if ((state & BitmapManager.LEFT) == BitmapManager.LEFT ||
                    (state & BitmapManager.RIGHT) == BitmapManager.RIGHT) {
//                Log.e(TAG, "dis intercept...");
                interceptParentTouchEvent(false);
            }

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
//            Log.e(TAG, "VX: " + velocityX + "  VY: " + velocityY);
            startFling(velocityX * 1.2f, velocityY * 1.2f);
            return true;
        }

        /*************************************缩放**************************************/

        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            if (mBitmapManager == null) {
                return false;
            }

            float factor = detector.getScaleFactor();
            mBitmapManager.scaleShowBitmap(
                    (int) detector.getFocusX(), (int) detector.getFocusY(), factor);

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector)
        {
            /**
             * 当缩放结束后，计算最新的的SampleSize, 如果SampleSize改变了，则重新解码最新的bitmap
             */
            if (mBitmapManager != null) {
                mBitmapManager.updateSampleSize();
            }
        }

    }

    /**********************************滑动惯性*******************************/

    private float mPhysicalCoeff;
    private float mFlingFriction = ViewConfiguration.getScrollFriction();
    private final static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
    private final static float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)

    private ValueAnimator mValueAnimator = null;

    private void stopFling()
    {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
    }

    private void startFling(final float velocityX, final float velocityY)
    {
        stopFling();

        final float fx = (velocityX < 0 ? 1 : -1);
        final float fy = (velocityY < 0 ? 1 : -1);

        final float velocity = (float) Math.hypot(velocityX, velocityY);
        final long duration = getSplineFlingDuration(velocity);

        mValueAnimator = ValueAnimator.ofFloat(1f, 0);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setDuration(duration);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            private Double mLastDisX = Double.NaN;
            private Double mLastDisY = Double.NaN;

            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                float value = (float) animation.getAnimatedValue();

                double curDisX = getSplineFlingDistance(value * velocityX) * fx;
                double curDisY = getSplineFlingDistance(value * velocityY) * fy;

                if (mLastDisX.isNaN() || mLastDisY.isNaN()) {
                    mLastDisX = curDisX;
                    mLastDisY = curDisY;
                    return;
                }

                int dx = (int) (curDisX - mLastDisX);
                int dy = (int) (curDisY - mLastDisY);

//                Log.e(TAG, "Dx: " + dx + "  DY: " + dy);

                if (mBitmapManager != null) {
                    mBitmapManager.offsetShowBitmap(dx, dy);
                }

                mLastDisX = curDisX;
                mLastDisY = curDisY;
            }
        });

        mValueAnimator.start();
    }

    private double getSplineDeceleration(float velocity)
    {
        return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
    }

    private int getSplineFlingDuration(float velocity)
    {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return (int) (1000.0 * Math.exp(l / decelMinusOne));
    }

    private double getSplineFlingDistance(float velocity)
    {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }


    public interface OnActionListener
    {
        /**
         * 在View上点击了一次（而且没有双击）
         * @param onImage 是否点击在了有效的图片上
         */
        void onSingleTapped(MotionEvent event, boolean onImage);

        /**
         * 双击了
         */
        void onDoubleTapped(MotionEvent event);

        /**
         * 长按了
         */
        void onLongPressed(MotionEvent event);

        /**
         * 初始化完成，图片已经显示
         * 返回是否成功，并返回图片的尺寸
         */
        void onSetImageFinished(boolean success, Rect image);
    }


    private int dpToPx(float dp)
    {
        return (int) (dp * mDisplayDensity + 0.5f);
    }

    private int pxToDp(float px)
    {
        return (int) (px / mDisplayDensity + 0.5f);
    }

}