package com.wzx.library.video.mediacontrol;

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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.wzx.library.R;
import com.wzx.library.video.mediacore.MyVideoView;
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
    protected static final int sDefaultTimeout = 3000;
    protected boolean mUseFastForward = false;
    protected boolean mListenersSet;
    protected OnClickListener mNextListener, mPrevListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    protected ImageButton mPauseButton;
    protected ImageButton mFfwdButton;
    protected ImageButton mFullScreenButton;
    protected ImageButton mRewButton;
    protected ImageButton mNextButton;
    protected ImageButton mPrevButton;
    protected CharSequence mPlayDescription;
    protected CharSequence mPauseDescription;
    protected View mContentView;
    protected View mLoadingView;

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
        return mRoot;
    }

    protected void initControllerView(View v) {
        mPlayDescription = "播放";
        mPauseDescription = "暂停";

        mContentView = v.findViewById(R.id.controlcontent);
        mLoadingView = v.findViewById(R.id.bufferloadingview);

        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFullScreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
        if (mFullScreenButton != null) {
            mFullScreenButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnFullScreenListener != null) {
                        mOnFullScreenListener.onClick(v);
                    }
//                    MyVideoView myVideoView = (MyVideoView) mPlayer;
//                    float rotation = myVideoView.getRotation();
//                    if (rotation != 90) {
//                        myVideoView.setRotation(90);
////                        setRotation(90);
//                    } else {
//                        myVideoView.setRotation(0);
////                        setRotation(0);
//                    }
                }
            });
        }

//        mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
//        if (mFfwdButton != null) {
//            mFfwdButton.setOnClickListener(mFfwdListener);
//            mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
//        }
//
//        mRewButton = (ImageButton) v.findViewById(R.id.rew);
//        if (mRewButton != null) {
//            mRewButton.setOnClickListener(mRewListener);
//            mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
//        }
//
//        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
//        mNextButton = (ImageButton) v.findViewById(R.id.next);
//        if (mNextButton != null && !mListenersSet) {
//            mNextButton.setVisibility(View.GONE);
//        }
//        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
//        if (mPrevButton != null && !mListenersSet) {
//            mPrevButton.setVisibility(View.GONE);
//        }

        mProgress = (ProgressBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(100);
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        installPrevNextListeners();
    }

    public void setAnchorView(ViewGroup group) {
        if (getParent() != null) {
            return;
        }

        ViewGroup.LayoutParams frameParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        group.addView(this, frameParams);
    }

    protected void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mNextListener != null);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mPrevListener != null);
        }
    }

    public void setPrevNextListeners(OnClickListener next, OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;
        mListenersSet = true;

        if (mRoot != null) {
            installPrevNextListeners();

            if (mNextButton != null) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            if (mPrevButton != null) {
                mPrevButton.setVisibility(View.VISIBLE);
            }
        }
    }

    OnClickListener mOnFullScreenListener;
    public void setOnFullScreenListener(OnClickListener onFullScreenListener) {
        mOnFullScreenListener = onFullScreenListener;
    }

    protected final OnClickListener mRewListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = getCurrentPosition();
            pos -= 5000; // milliseconds
            seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    protected final OnClickListener mFfwdListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = getCurrentPosition();
            pos += 15000; // milliseconds
            seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };

    protected final OnClickListener mPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isPlaying()) {
                pause();
            } else {
                start();
            }
        }
    };

    protected final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(0);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            removeCallbacks(mShowProgress);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = getDuration();
            long newposition = (duration * progress) / 100L;
            seekTo( (int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime( (int) newposition));
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            post(mShowProgress);
        }
    };

    public void show() {
        show(sDefaultTimeout);
    }

    public void show(int timeout) {
        updatePausePlay();
        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        post(mShowProgress);

        if (timeout != 0) {
            removeCallbacks(mFadeOut);
            postDelayed(mFadeOut, timeout);
        }
        mContentView.setVisibility(VISIBLE);
    }

    protected int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = getCurrentPosition();
        int duration = getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 100L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = getBufferPercentage();
            mProgress.setSecondaryProgress(percent);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

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
                        removeCallbacks(mShowProgress);
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

    protected final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    protected final Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && isShowing() && isPlaying()) {
//                postDelayed(mShowProgress, 1000 - (pos % 1000));
                postDelayed(mShowProgress, 1000);
            }
        }
    };


    public void setMediaPlayer(MyVideoView player) {
        mPlayer = player;
        Log.d("wangzixu", "setVideoPath setMediaPlayer mPlayer = " +mPlayer);
        updatePausePlay();
    }

    public void setPauseButtonVisiblity(int visiblity) {
        mPauseButton.setVisibility(visiblity);
    }


    protected void updatePausePlay() {
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
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    public void onLoadingBuffer(boolean startBuffer) {
        Log.d("wangzixu", "mymediacontroler onLoadingBuffer startBuffer  = " + startBuffer) ;
        if (startBuffer) {
            mLoadingView.setVisibility(VISIBLE);
            mPauseButton.setVisibility(GONE);
        } else {
            mLoadingView.setVisibility(GONE);
            mPauseButton.setVisibility(VISIBLE);
        }
    }

    public void onRenderStart() {
        Log.d("wangzixu", "mymediacontroler onRenderStart") ;
        if (isShowing()) {
            mLoadingView.setVisibility(GONE);
            mPauseButton.setVisibility(VISIBLE);
            show(50);
        } else {
            mLoadingView.setVisibility(GONE);
            mPauseButton.setVisibility(VISIBLE);
        }
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

            if (isShowing()) {
                show(sDefaultTimeout);
            }
        }
    }

    public void showLondingView() {
        mLoadingView.setVisibility(VISIBLE);
        mPauseButton.setVisibility(GONE);
    }


    @Override
    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
            updatePausePlay();
            show(sDefaultTimeout);
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
}
