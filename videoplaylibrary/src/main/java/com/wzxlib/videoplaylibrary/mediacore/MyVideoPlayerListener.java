package com.wzxlib.videoplaylibrary.mediacore;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by wangzixu on 2017/12/20.
 */
public interface MyVideoPlayerListener extends IMediaPlayer.OnBufferingUpdateListener
        , IMediaPlayer.OnCompletionListener
        , IMediaPlayer.OnPreparedListener
        , IMediaPlayer.OnInfoListener
        , IMediaPlayer.OnErrorListener
        , IMediaPlayer.OnVideoSizeChangedListener
        , IMediaPlayer.OnSeekCompleteListener {

    void onRenderStart(IMediaPlayer var1);
    void onBufferStart();
    void onBufferEnd();
}
