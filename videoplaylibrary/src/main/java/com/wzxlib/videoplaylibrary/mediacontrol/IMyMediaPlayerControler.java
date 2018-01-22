package com.wzxlib.videoplaylibrary.mediacontrol;

import android.widget.MediaController;

/**
 * Created by wangzixu on 2018/1/14.
 */
public interface IMyMediaPlayerControler extends MediaController.MediaPlayerControl {
    void setVideoPath(String path);
    void reset();
}
