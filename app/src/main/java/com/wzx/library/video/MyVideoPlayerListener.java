package com.wzx.library.video;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by wangzixu on 2017/12/20.
 */
public abstract class MyVideoPlayerListener implements IMediaPlayer.OnBufferingUpdateListener
        , IMediaPlayer.OnCompletionListener
        , IMediaPlayer.OnPreparedListener
        , IMediaPlayer.OnInfoListener
        , IMediaPlayer.OnErrorListener
        , IMediaPlayer.OnVideoSizeChangedListener
        , IMediaPlayer.OnSeekCompleteListener {
}
