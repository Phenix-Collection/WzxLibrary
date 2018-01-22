package com.wzxlib.videoplaylibrary.mediacontrol;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wzxlib.videoplaylibrary.R;
import com.wzxlib.videoplaylibrary.mediacore.MyVideoView;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by wangzixu on 2018/1/9.
 */
public class MyMediaControler extends FrameLayout implements IMyMediaPlayerControler {
    protected MyVideoView mPlayer;
    protected Context mContext;
    protected View mRoot;
    protected ProgressBar mProgress;
    protected TextView mEndTime, mCurrentTime;
    protected boolean mDragging;
    protected boolean mUseFastForward = false;
    protected OnClickListener mNextListener, mPrevListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    protected ImageButton mPauseButton;
    protected ImageButton mFullScreenButton;
    protected CharSequence mPlayDescription;
    protected CharSequence mPauseDescription;
    protected View mContentView;
    protected View mLoadingView;
    protected OnClickListener mOnFullScreenListener;
    protected OnClickListener mOnPauseBtnClickListener;

    public MyMediaControler(Context context, boolean useFastForward) {
        this(context);
        mUseFastForward = useFastForward;
    }

    public MyMediaControler(@NonNull Context context) {
        this(context, null);
    }

    public MyMediaControler(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyMediaControler(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRoot = this;
        mContext = context;

        makeControllerView();
    }

    protected View makeControllerView() {
        mRoot = LayoutInflater.from(mContext).inflate(R.layout.mymedia_contorl, this, true);
        initControllerView(mRoot);
        setEnabled(false);
        return mRoot;
    }

    protected void initControllerView(View v) {
        mPlayDescription = "播放";
        mPauseDescription = "暂停";
        mContentView = v.findViewById(R.id.controlcontent);
        mLoadingView = v.findViewById(R.id.bufferloadingview);

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnPauseBtnClickListener != null) {
                        mOnPauseBtnClickListener.onClick(v);
                    } else {
                        if (isPlaying()) {
                            pause();
                        } else {
                            start();
                        }
                    }
                }
            });
        }

        mFullScreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
        if (mFullScreenButton != null) {
            mFullScreenButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnFullScreenListener != null) {
                        mOnFullScreenListener.onClick(v);
                    }
                }
            });
        }

        mProgress = v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            mProgress.setMax(1000);
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStartTrackingTouch(SeekBar bar) {
                        show(0);
                        mDragging = true;
                        removeCallbacks(mUpdateProgressRunnable);
                    }

                    @Override
                    public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                        if (!fromuser) {
                            return;
                        }

                        long duration = getDuration();
                        long newposition = (duration * progress) / 1000L;
                        seekTo( (int) newposition);
                        if (mCurrentTime != null) {
                            mCurrentTime.setText(stringForTime( (int) newposition));
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar bar) {
                        mDragging = false;
                        setProgress();
                        updatePausePlay();
                        // Ensure that progress is properly updated in the future,
                        // the call to show() does not guarantee this because it is a
                        // no-op if we are already showing.
                        post(mUpdateProgressRunnable);
                    }
                });
            }
        }
    }

    /**
     * 显示主控制界面, 进度条, 时间, 等, 不包括loadingview, loadingview单独控制
     * @param timeout 自动隐藏界面的等待时间, 若为0, 则始终显示, 不自动隐藏
     */
    public void show(int timeout) {
        updatePausePlay();
        if (mContentView.getVisibility() != VISIBLE) {
            mContentView.setVisibility(VISIBLE);
            try {
                ValueAnimator animator = ValueAnimator.ofFloat(0f, 1.0f);
                animator.setDuration(200);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float f = (float) animation.getAnimatedValue();
                        mContentView.setAlpha(f);
                    }
                });
                animator.start();
            } catch (Exception ex) {
                Log.w("MediaController", "hide Exception = " + ex.getMessage());
            }
        }

        post(mUpdateProgressRunnable);//更新进度条
        removeCallbacks(mHideRunnable);
        if (timeout != 0) {
            postDelayed(mHideRunnable, timeout);
        }
    }

    public void removeSetProgressRun() {
        removeCallbacks(mUpdateProgressRunnable);
    }

    public void hide() {
        if (getVisibility() == VISIBLE) {
            try {
                ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0);
                animator.setDuration(200);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float f = (float) animation.getAnimatedValue();
                        mContentView.setAlpha(f);
                    }
                });

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        removeCallbacks(mUpdateProgressRunnable);
                        mContentView.setVisibility(GONE);
                        mContentView.setAlpha(1.0f);
                    }
                });

                animator.start();
            } catch (Exception ex) {
                Log.w("MediaController", "hide Exception = " + ex.getMessage());
            }
        }
    }

    protected final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * 移除自动隐藏的runabble
     */
    public void retainShow() {
        removeCallbacks(mHideRunnable);
        mContentView.setVisibility(VISIBLE);
    }

    protected final Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && isShowing() && isPlaying()) {
                postDelayed(mUpdateProgressRunnable, 1000 - (pos % 1000));
            }
        }
    };

    protected int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = getCurrentPosition();
        int duration = getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = getBufferPercentage();
            mProgress.setSecondaryProgress(percent);
        }

        if (mEndTime != null) {
            mEndTime.setText(stringForTime(duration));
        }
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringForTime(position));
        }
        return position;
    }

    protected String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public boolean isShowing() {
        return mContentView.getVisibility() == VISIBLE;
    }

    public void setMediaPlayer(MyVideoView player) {
        mPlayer = player;
        Log.d("wangzixu", "setVideoPath setMediaPlayer mPlayer = " +mPlayer);
        updatePausePlay();
    }

    public View getPauseBtn() {
        return mPauseButton;
    }

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null) {
            return;
        }

        mPauseButton.requestFocus();
        if (isPlaying()) {
            mPauseButton.setImageResource(R.drawable.ic_media_pause);
            mPauseButton.setContentDescription(mPauseDescription);
        } else {
            mPauseButton.setImageResource(R.drawable.ic_media_play);
            mPauseButton.setContentDescription(mPlayDescription);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    public void reset() {
        mProgress.setProgress(0);
        mProgress.setSecondaryProgress(0);
        mPauseButton.setImageResource(R.drawable.ic_media_play);
        mPauseButton.setContentDescription(mPlayDescription);

        if (mEndTime != null)
            mEndTime.setText("");
        if (mCurrentTime != null)
            mCurrentTime.setText("");
    }

    @Override
    public void setVideoPath(String path) {
        if (mPlayer != null) {
            mPlayer.setVideoPath(path);
        }
    }

    @Override
    public void start() {
        if (mPlayer != null) {
            mPlayer.start();
            updatePausePlay();
        }
    }

    public void showLoadingView() {
        mLoadingView.setVisibility(VISIBLE);
        mPauseButton.setVisibility(GONE);
    }

    public void hideLoadingView() {
        mLoadingView.setVisibility(GONE);
        mPauseButton.setVisibility(VISIBLE);
    }

    @Override
    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
            updatePausePlay();
        }
    }

    @Override
    public int getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (mPlayer != null) {
            mPlayer.seekTo(pos);
        }
    }

    @Override
    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        if (mPlayer != null) {
            return mPlayer.getBufferPercentage();
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        if (mPlayer != null) {
            return mPlayer.canPause();
        }
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        if (mPlayer != null) {
            return mPlayer.canSeekBackward();
        }
        return false;
    }

    @Override
    public boolean canSeekForward() {
        if (mPlayer != null) {
            return mPlayer.canSeekForward();
        }
        return false;
    }

    @Override
    public int getAudioSessionId() {
        if (mPlayer != null) {
            return mPlayer.getAudioSessionId();
        }
        return 0;
    }

    public void setOnPauseBtnClickListener(OnClickListener onPauseBtnClickListener) {
        mOnPauseBtnClickListener = onPauseBtnClickListener;
    }

    public void setOnFullScreenListener(OnClickListener onFullScreenListener) {
        mOnFullScreenListener = onFullScreenListener;
    }
}
