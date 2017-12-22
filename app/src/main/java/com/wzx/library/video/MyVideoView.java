package com.wzx.library.video;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.wzx.library.media.FileMediaDataSource;
import com.wzx.library.media.IRenderView;

import java.io.File;
import java.io.IOException;

import tv.danmaku.ijk.media.exo.IjkExoMediaPlayer;
import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by wangzixu on 2017/12/19.
 */
public class MyVideoView extends FrameLayout implements MediaController.MediaPlayerControl
        , IMediaPlayer.OnPreparedListener
        , IMediaPlayer.OnVideoSizeChangedListener
        , IMediaPlayer.OnCompletionListener
        , IMediaPlayer.OnInfoListener
        , IMediaPlayer.OnErrorListener
        , IMediaPlayer.OnBufferingUpdateListener
        , IMediaPlayer.OnSeekCompleteListener {
    /**
     * 由ijkplayer提供，用于播放视频，需要给他传入一个surfaceView
     */
    private IMediaPlayer mMediaPlayer = null;
    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    /**
     * 视频文件地址
     */
    private String mPath = "";

    private IMyRenderView mRenderView;

    private MyVideoPlayerListener mMyVideoPlayerListener;
    private Context mAppContext;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private Uri mUri;

    public MyVideoView(@NonNull Context context) {
        this(context, null);
    }

    public MyVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyVideoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAppContext = context.getApplicationContext();
        mVideoWidth = 0;
        mVideoHeight = 0;
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;

        if (false) {
            //初始化 需要添加一个surfaceview来承载视频
            mRenderView = new MySurfaceView(mAppContext);
            mRenderView.addRenderCallback(mMyRenderCallback);
            mRenderView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            mRenderView.getView().setLayoutParams(layoutParams);
            this.addView(mRenderView.getView());
        } else {

            mRenderView = new MyTextureView(mAppContext);
            mRenderView.addRenderCallback(mMyRenderCallback);
            mRenderView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            mRenderView.getView().setLayoutParams(layoutParams);
            this.addView(mRenderView.getView());
        }

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    /**
     * 设置视频地址。
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        mUri = Uri.parse(path);

        //每次都要重新创建IMediaPlayer
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;

            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }

        try {
            int i = 2;
            if (i == 0) {
                //exo
                IjkExoMediaPlayer IjkExoMediaPlayer = new IjkExoMediaPlayer(mAppContext);
                mMediaPlayer = IjkExoMediaPlayer;
            } else if (i == 1) {
                AndroidMediaPlayer androidMediaPlayer = new AndroidMediaPlayer();
                mMediaPlayer = androidMediaPlayer;
            } else {
                IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
                ijkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);

                //开启硬解码, 软解码时不会旋转视频角度这时需要你通过onInfo的what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED去获取角度，
                //自己旋转画面(surfaceview不支持旋转, 只有用textureview才可以)。或者开启硬解硬解码, 自动旋转角度，不过硬解码容易造成黑屏无声（硬件兼容问题）
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);

                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
                mMediaPlayer = ijkMediaPlayer;
            }

            initMediaPlayerListener();

            //***设置uri这段是ijk的官方Demo设置方法****
            String scheme = mUri.getScheme();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
                IMediaDataSource dataSource = new FileMediaDataSource(new File(mUri.toString()));
                mMediaPlayer.setDataSource(dataSource);
            }  else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mMediaPlayer.setDataSource(mAppContext, mUri, null);
            } else {
                mMediaPlayer.setDataSource(mUri.toString());
            }

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);

            //给mediaPlayer设置视图
            mRenderView.bindToMediaPlayer(mMediaPlayer);

            mMediaPlayer.prepareAsync();

            mCurrentState = STATE_PREPARING;

//            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
//            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//            requestLayout();
//            invalidate();
        } catch (IOException e) {
            e.printStackTrace();
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
        }
    }

    private void initMediaPlayerListener() {
        if (mMediaPlayer == null) {
            return;
        }

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
    }

    private boolean isInPlayEnableState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    public void setMyVideoPlayerListener(MyVideoPlayerListener listener) {
        this.mMyVideoPlayerListener = listener;
    }

    public IMediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    //mediaplayer接口实现
    @Override
    public void start() {
        Log.i("wangzixu", "mMyVideoView start mCurrentState = " + mCurrentState);
        if (isInPlayEnableState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlayEnableState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    //各种监听封装
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mCurrentState = STATE_PREPARED;
        int mVideoWidth = iMediaPlayer.getVideoWidth();
        int mVideoHeight = iMediaPlayer.getVideoHeight();
        int mVideoSarNum = iMediaPlayer.getVideoSarNum();
        int mVideoSarDen = iMediaPlayer.getVideoSarDen();

        if (mVideoWidth != 0 && mVideoHeight != 0) {
            if (mRenderView != null) {
                mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                Log.i("wangzixu", "mMyVideoView ***onPrepared*** mVideoWidth = " + mVideoWidth + ", mVideoHeight = " + mVideoHeight);
                Log.i("wangzixu", "mMyVideoView ***onPrepared*** mSurfaceWidth = " + mSurfaceWidth + ", mSurfaceHeight = " + mSurfaceHeight);

//                if (mVideoWidth == mSurfaceWidth && mVideoHeight == mSurfaceHeight) {
                if (mSurfaceWidth > 0 && mSurfaceHeight > 0) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the video here instead of in the callback.
                    if (mTargetState == STATE_PLAYING) {
                        start();
                    }
                }
            }
        } else {
            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            if (mTargetState == STATE_PLAYING) {
                start();
            }
        }

        if (mMyVideoPlayerListener != null) {
            mMyVideoPlayerListener.onPrepared(iMediaPlayer);
        }
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();
        int mVideoSarNum = mp.getVideoSarNum();
        int mVideoSarDen = mp.getVideoSarDen();
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            if (mRenderView != null) {
                mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
            }
            // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            requestLayout();
        }

        if (mMyVideoPlayerListener != null) {
            mMyVideoPlayerListener.onVideoSizeChanged(mp, width, height, sarDen, sarDen);
        }
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        mCurrentState = STATE_PLAYBACK_COMPLETED;
        mTargetState = STATE_PLAYBACK_COMPLETED;

        if (mMyVideoPlayerListener != null) {
            mMyVideoPlayerListener.onCompletion(mMediaPlayer);
        }
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        if (mMyVideoPlayerListener != null) {
            mMyVideoPlayerListener.onInfo(iMediaPlayer, i, i1);
        }

        if (i == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
            if (mRenderView != null) {
                mRenderView.setVideoRotation(i1);
            }
        }
        return true;
    }

    @Override
    public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
        mCurrentState = STATE_ERROR;
        mTargetState = STATE_ERROR;

                    /* If an error handler has been supplied, use it and finish. */
        if (mMyVideoPlayerListener != null) {
            if (mMyVideoPlayerListener.onError(mMediaPlayer, framework_err, impl_err)) {
                return true;
            }
        }

                    /* Otherwise, pop up an error dialog so the user knows that
                     * something bad has happened. Only try and pop up the dialog
                     * if we're attached to a window. When we're going away and no
                     * longer have a window, don't bother showing the user an error.
                     */
        if (getWindowToken() != null) {
            String messageId;

            if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                messageId = "无效的播放源";
            } else {
                messageId = "未知错误";
            }

            new AlertDialog.Builder(getContext())
                    .setMessage(messageId)
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                            /* If we get here, there is no onError listener, so
                                             * at least inform them that the video is over.
                                             */
                                    if (mMyVideoPlayerListener != null) {
                                        mMyVideoPlayerListener.onCompletion(mMediaPlayer);
                                    }
                                }
                            })
                    .setCancelable(false)
                    .show();
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
        if (mMyVideoPlayerListener != null) {
            mMyVideoPlayerListener.onBufferingUpdate(mp, percent);
        }
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        if (mMyVideoPlayerListener != null) {
            mMyVideoPlayerListener.onSeekComplete(mp);
        }
    }

    //***RenderCallBack
    private IMyRenderView.IMyRenderCallback mMyRenderCallback = new IMyRenderView.IMyRenderCallback() {
        @Override
        public void onSurfaceCreated(int width, int height) {
            mSurfaceWidth = 0;
            mSurfaceHeight = 0;
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            mSurfaceWidth = width;
            mSurfaceHeight = height;

            Log.i("wangzixu", "mMyVideoView ***onPrepared*** onSurfaceChanged width = " + width + ", height = " + height);

            boolean isValidState = (mTargetState == STATE_PLAYING);
//            boolean hasValidSize = mVideoWidth == mSurfaceWidth && mVideoHeight == mSurfaceHeight;
            boolean hasValidSize = true;
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                start();
            }
        }

        @Override
        public void onSurfaceDestroyed() {
            mSurfaceWidth = 0;
            mSurfaceHeight = 0;
        }
    };
}
