package com.wzx.library.video.mediacore;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import com.wzx.library.video.mediacontrol.IMyMediaPlayerControler;
import com.wzx.library.video.mediacontrol.MyMediaControler;
import java.io.File;
import java.io.IOException;
import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by wangzixu on 2018/1/8.
 */
public class MyVideoView extends TextureView  implements IMyMediaPlayerControler
        , IMediaPlayer.OnPreparedListener
        , IMediaPlayer.OnVideoSizeChangedListener
        , IMediaPlayer.OnCompletionListener
        , IMediaPlayer.OnInfoListener
        , IMediaPlayer.OnErrorListener
        , IMediaPlayer.OnBufferingUpdateListener
        , IMediaPlayer.OnSeekCompleteListener {
    private static final String TAG = "MyVideoView";
    private Context mContext;
    private SurfaceTexture mSurfaceTexture;

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
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    private int mAudioSession;
    private int mCurrentBufferPercentage;
    private MyVideoPlayerListener mMyVideoPlayerListener;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    public Uri mUri;
    private int mSeekWhenPrepared;
    private int mVideoSarNum;
    private int mVideoSarDen;
    private MyMediaControler mMediaController;

    public MyVideoView(Context context) {
        this(context, null);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context.getApplicationContext();
        mVideoWidth = 0;
        mVideoHeight = 0;
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "MyVideoView onSurfaceTextureAvailable mSurfaceTexture = " + mSurfaceTexture + ", w = " + width
                    + ", h = " + height);
                if (mSurfaceTexture == null) {
                    mSurfaceTexture = surface;
                    mSurfaceWidth = width;
                    mSurfaceHeight = height;
                    if (mMediaPlayer != null) {
                        mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
                    } else {
                        openVideo();
                    }
                } else {
                    setSurfaceTexture(mSurfaceTexture);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "MyVideoView onSurfaceTextureSizeChanged width = " + width
                        + ", height = " + height + ", mVideoWidth = " + mVideoWidth + ", mVideoHeight = " + mVideoHeight);
                mSurfaceWidth = width;
                mSurfaceHeight = height;

                boolean isValidState = (mTargetState == STATE_PLAYING);
//                boolean hasValidSize = mVideoWidth == width && mVideoHeight == height;
                boolean hasValidSize = true;
                if (mMediaPlayer != null && isValidState && hasValidSize) {
                    if (mSeekWhenPrepared != 0) {
                        seekTo(mSeekWhenPrepared);
                    }
                    start();
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.i(TAG, "MyVideoView onSurfaceTextureDestroyed");
                mSurfaceTexture = null;
                mSurfaceWidth = 0;
                mSurfaceHeight = 0;
                if (mMediaController != null) {
                    mMediaController.hide();
                }
//            release(true);
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//                Log.i(TAG, "MyVideoView onSurfaceTextureUpdated");
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //ijk官方的测量
//        mMeasureHelper.setVideoSize(mVideoWidth, mVideoHeight);
//        mMeasureHelper.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
//        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());

        //自己实现的测量
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        if (mVideoWidth > 0 && mVideoHeight > 0) {
            int wr = width * mVideoHeight;
            int hr = height * mVideoWidth;
            if (wr > hr) {
                //屏幕的宽高比大于视频源的宽高比, 视频的显示应该是高度顶满, 左右留白
                width = hr/mVideoHeight;
            } else if (wr < hr) {
                //屏幕的宽高比小于视频源的宽高比, 视频的显示应该是高度留白, 左右顶满
                height = wr/mVideoWidth;
            }
        }

        //google videoview的测量
//        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
//        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
//        Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", " + MeasureSpec.toString(heightMeasureSpec) + ")");
//        Log.i("@@@@", "onMeasure(width = " + width + ", height = " + height + ")");
//        if (mVideoWidth > 0 && mVideoHeight > 0) {
//
//            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
//            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
//
//            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
//            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
//
//            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
//                // the size is fixed
//                width = widthSpecSize;
//                height = heightSpecSize;
//
//                // for compatibility, we adjust size based on aspect ratio
//                if ( mVideoWidth * height  < width * mVideoHeight ) {
//                    //Log.i("@@@", "image too wide, correcting");
//                    width = height * mVideoWidth / mVideoHeight;
//                } else if ( mVideoWidth * height  > width * mVideoHeight ) {
//                    //Log.i("@@@", "image too tall, correcting");
//                    height = width * mVideoHeight / mVideoWidth;
//                }
//            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
//                // only the width is fixed, adjust the height to match aspect ratio if possible
//                width = widthSpecSize;
//                height = width * mVideoHeight / mVideoWidth;
//                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
//                    // couldn't match aspect ratio within the constraints
//                    height = heightSpecSize;
//                }
//            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
//                // only the height is fixed, adjust the width to match aspect ratio if possible
//                height = heightSpecSize;
//                width = height * mVideoWidth / mVideoHeight;
//                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
//                    // couldn't match aspect ratio within the constraints
//                    width = widthSpecSize;
//                }
//            } else {
//                // neither the width nor the height are fixed, try to use actual video size
//                width = mVideoWidth;
//                height = mVideoHeight;
//                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
//                    // too tall, decrease both width and height
//                    height = heightSpecSize;
//                    width = height * mVideoWidth / mVideoHeight;
//                }
//                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
//                    // too wide, decrease both width and height
//                    width = widthSpecSize;
//                    height = width * mVideoHeight / mVideoWidth;
//                }
//            }
//        } else {
//            // no size yet, just adopt the given spec sizes
//        }
        setMeasuredDimension(width, height);
    }


    /**
     * 设置视频地址。
     */
    @Override
    public void setVideoPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        mSeekWhenPrepared = 0;
        mUri = Uri.parse(path);
        openVideo();
    }

    @Override
    public void reset() {
        mMediaPlayer.reset();
    }

    public void openVideo() {
        if (mUri == null || mSurfaceTexture == null) {
            return;
        }

        //每次都要重新创建IMediaPlayer
        if (mMediaPlayer != null) {
            release(true);
        }

        mCurrentBufferPercentage = 0;

        try {
            int i = 2;
            if (i == 0) {
                //exo
//                IjkExoMediaPlayer IjkExoMediaPlayer = new IjkExoMediaPlayer(mContext);
//                mMediaPlayer = IjkExoMediaPlayer;
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
                mMediaPlayer.setDataSource(mContext, mUri, null);
            } else {
                mMediaPlayer.setDataSource(mUri.toString());
            }

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            //给mediaPlayer设置视图
            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            mMediaController.setEnabled(isInPlaybackState());
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

    private boolean isInPlaybackState() {
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
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    /*
 * release the media player in any state
 */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState  = STATE_IDLE;
            }
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    //mediaplayer接口实现
    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;

            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;

                AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                am.abandonAudioFocus(null);
            }
        }
        mTargetState = STATE_PAUSED;
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(pos);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = pos;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    //各种监听封装
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mCurrentState = STATE_PREPARED;

        mVideoWidth = iMediaPlayer.getVideoWidth();
        mVideoHeight = iMediaPlayer.getVideoHeight();

        mVideoSarNum = iMediaPlayer.getVideoSarNum();
        mVideoSarDen = iMediaPlayer.getVideoSarDen();

        if (mMediaController != null) {
            mMediaController.setEnabled(true);
//            mMediaController.show(0);
        }

        int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
        if (seekToPosition != 0) {
            seekTo(seekToPosition);
        }

        if (mVideoWidth != 0 && mVideoHeight != 0) {
            Log.i(TAG, "mMyVideoView ***onPrepared*** mVideoWidth = " + mVideoWidth + ", mVideoHeight = " + mVideoHeight
                    + ", mSurfaceWidth = " + mSurfaceWidth + ", mSurfaceHeight = " + mSurfaceHeight);
//            if (mVideoWidth == mSurfaceWidth && mVideoHeight == mSurfaceHeight) {
            if (mSurfaceWidth > 0 && mSurfaceHeight > 0) {
                // We didn't actually change the size (it was already at the size
                // we need), so we won't get a "surface changed" callback, so
                // start the video here instead of in the callback.
                if (mTargetState == STATE_PLAYING) {
                    start();
//                    if (mMediaController != null) {
//                        mMediaController.show();
//                    }
                } else if (!isPlaying()
//                        && (seekToPosition != 0 || getCurrentPosition() > 0)
                        ) {
                    if (mMediaController != null) {
                        // Show the media controls when we're paused into a video and make 'em stick.
                        mMediaController.onLoadingBuffer(false);
                        mMediaController.show(0);
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
        mVideoSarNum = mp.getVideoSarNum();
        mVideoSarDen = mp.getVideoSarDen();
        if (mVideoWidth != 0 && mVideoHeight != 0) {
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
        mMediaController.show(0);
        if (mMyVideoPlayerListener != null) {
            mMyVideoPlayerListener.onCompletion(mMediaPlayer);
        }
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        Log.i(TAG, "oninfo i = " + i + ", i1 = " + i1);

        switch (i) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: //只有在第一次渲染时调用
                if (mMediaController != null) {
                    mMediaController.onRenderStart();
                }
                if (mMyVideoPlayerListener != null) {
                    mMyVideoPlayerListener.onRenderStart(iMediaPlayer);
                }
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mMediaController != null) {
                    mMediaController.onLoadingBuffer(true);
                }
                if (mMyVideoPlayerListener != null) {
                    mMyVideoPlayerListener.onLoadingBuffer(true);
                }
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (mMediaController != null) {
                    mMediaController.onLoadingBuffer(false);
                }
                if (mMyVideoPlayerListener != null) {
                    mMyVideoPlayerListener.onLoadingBuffer(false);
                }
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                setRotation(i1);
                break;
            default:
                break;
        }

        if (mMyVideoPlayerListener != null) {
            mMyVideoPlayerListener.onInfo(iMediaPlayer, i, i1);
        }
        return true;
    }

    @Override
    public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
        mCurrentState = STATE_ERROR;
        mTargetState = STATE_ERROR;
        if (mMediaController != null) {
            mMediaController.hide();
        }
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
        mCurrentBufferPercentage = percent;
        if (mCurrentBufferPercentage >= 98) { //修改一个到了98就不在缓冲的bug, 不知道什么原因
            mCurrentBufferPercentage = 100;
        }
        Log.d("mymediacontor", "percent = " + mCurrentBufferPercentage);
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


    //mediaplayer相关
    public void setMediaController(MyMediaControler controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        Log.d("wangzixu", "setVideoPath setMediaController controller = " +controller);
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            mMediaController.setAnchorView((ViewGroup) this.getParent());
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
        requestLayout();
        invalidate();
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
