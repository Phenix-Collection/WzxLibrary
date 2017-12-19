package com.wzx.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by wangzixu on 2017/12/19.
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    // SurfaceHolder
    private SurfaceHolder mSurfaceHolder;
    // 画布
    private Canvas mCanvas;
    // 子线程标志位
    private volatile boolean mIsDestory;
    private volatile boolean mIsDrawing = false; //是否需要绘制
    // 画笔
    Paint mPaint;
    // 路径
    Path mPath;
    private float mLastX, mLastY;//上次的坐标
    private Thread mThread;


    public MySurfaceView(Context context) {
        this(context, null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//初始化 SurfaceHolder mSurfaceHolder
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        //画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStrokeWidth(10f);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        //路径
        mPath = new Path();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDestory = false;
        mSurfaceHolder = holder;
        clear();
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDestory = true;
        Log.i("wangzixu", "surfaceview destory--");
    }

    @Override
    public void run() {
        while (!mIsDestory) {
            drawing();
        }
    }

    private void drawing() {
        if (mIsDrawing) {
            try {
                mCanvas = mSurfaceHolder.lockCanvas();
                mCanvas.drawColor(Color.WHITE);
                mCanvas.drawPath(mPath, mPaint);
            } finally {
                if (mCanvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);
                mIsDrawing = true;
                break;
            case MotionEvent.ACTION_MOVE:
//                float dx = Math.abs(x - mLastX);
//                float dy = Math.abs(y - mLastY);
//                if (dx >= 3 || dy >= 3) {
//                    mPath.quadTo(mLastX, mLastY, (mLastX + x) / 2, (mLastY + y) / 2);
//                }
                mPath.lineTo(x,y);

                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsDrawing = false;
                break;
            default:
                break;
        }
        return true;
    }

    public void clear() {
        try {
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawColor(Color.WHITE);
            mPath.reset();
        } finally {
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    /**
     * 测量
     */
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
//        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
//        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
//        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
//
//        if (wSpecMode == MeasureSpec.AT_MOST && hSpecMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(300, 300);
//        } else if (wSpecMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(300, hSpecSize);
//        } else if (hSpecMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(wSpecSize, 300);
//        }
//    }
}
