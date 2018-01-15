package com.wzx.library.video.mediacontrol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.wzx.library.R;


/**
 * Created by wangzixu on 2018/1/14.
 */
public class MyMdeiaContorlerList extends MyMediaControler {
    public MyMdeiaContorlerList(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public MyMdeiaContorlerList(@NonNull Context context) {
        super(context);
    }

    public MyMdeiaContorlerList(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View makeControllerView() {
        mRoot = LayoutInflater.from(mContext).inflate(R.layout.mymedia_contorl_list, this, true);
        initControllerView(mRoot);
        return mRoot;
    }

    private String mTempPath;
    public void setTempVideoPath(String path) {
        mTempPath = path;
        setEnabled(true);
    }

    public void setOnPlayPauseListener(final OnClickListener listener) {
        if (mPauseButton != null) {
            mPauseButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPlayer.mUri == null) {
                        if (listener != null) {
                            listener.onClick(v);
                        }
                    } else {
                        mPauseListener.onClick(v);
                    }
                }
            });
        }
    }

//    @Override
//    public void start() {
//        if (mPlayer != null) {
//            if (mPlayer.mUri == null) {
//                mPlayer.setVideoPath(mTempPath);
//                showLondingView();
//            }
//            mPlayer.start();
//
//            if (isShowing()) {
//                show(sDefaultTimeout);
//            }
//        }
//    }
}
