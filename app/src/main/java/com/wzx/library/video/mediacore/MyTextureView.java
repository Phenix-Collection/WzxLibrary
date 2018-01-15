package com.wzx.library.video.mediacore;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by wangzixu on 2017/12/22.
 */
public class MyTextureView extends TextureView implements IMyRenderView {
    private static final String TAG = "MyTextureView";
    private MeasureHelper mMeasureHelper;
    private IMyRenderCallback mMyRenderCallback;
    private SurfaceTexture mSurfaceTexture;

    public MyTextureView(Context context) {
        this(context, null);
    }

    public MyTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMeasureHelper = new MeasureHelper(this);
        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.i("wangzixu", "MyTextureView onSurfaceTextureAvailable");
                if (mSurfaceTexture == null) {
                    mSurfaceTexture = surface;
                    if (mMyRenderCallback != null) {
                        mMyRenderCallback.onSurfaceCreated(width, height);
                    }
                } else {
                    MyTextureView.this.setSurfaceTexture(mSurfaceTexture);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.i("wangzixu", "MyTextureView onSurfaceTextureSizeChanged width = " + width + ", height = " + height);
                if (mMyRenderCallback != null) {
                    mMyRenderCallback.onSurfaceChanged(width, height);
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.i("wangzixu", "MyTextureView onSurfaceTextureDestroyed");
                mSurfaceTexture = null;
                if (mMyRenderCallback != null) {
                    mMyRenderCallback.onSurfaceDestroyed();
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                Log.i("wangzixu", "MyTextureView onSurfaceTextureUpdated");
            }
        });
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void bindToMediaPlayer(IMediaPlayer mp) {
        if (mp != null && mSurfaceTexture != null) {
            mp.setSurface(new Surface(mSurfaceTexture));
        }
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    @Override
    public void setVideoRotation(int degree) {
        mMeasureHelper.setVideoRotation(degree);
        setRotation(degree);
    }

    @Override
    public void setAspectRatio(int aspectRatio) {
        mMeasureHelper.setAspectRatio(aspectRatio);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }

    @Override
    public void addRenderCallback(@NonNull IMyRenderCallback callback) {
        mMyRenderCallback = callback;
    }

    @Override
    public void removeRenderCallback(@NonNull IMyRenderCallback callback) {
        mMeasureHelper = null;
    }
}
