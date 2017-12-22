package com.wzx.library.video;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.wzx.library.R;
import com.wzx.library.media.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by wangzixu on 2017/9/1.
 */
public class ActivityVideo extends Activity implements View.OnClickListener {
    private MyVideoView mMyVideoView;
    private IjkVideoView mIjkVideoView;
    private View mPlayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStatusBarTransparnet(this);
        setContentView(R.layout.activity_video);

        mMyVideoView = (MyVideoView) findViewById(R.id.myvideoview);
//        mIjkVideoView = (IjkVideoView) findViewById(R.id.myvideoview);
        mPlayBtn = findViewById(R.id.play);
        mPlayBtn.setOnClickListener(this);

        //加载native库
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            this.finish();
        }

        mMyVideoView.setMyVideoPlayerListener(new MyVideoPlayerListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
                Log.i("wangzixu", "mMyVideoView onBufferingUpdate i = " + i);
            }

            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                Log.i("wangzixu", "mMyVideoView onCompletion");
            }

            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                Log.i("wangzixu", "mMyVideoView onError i = " + i + ", i1 = " + i1);
                return false;
            }

            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                Log.i("wangzixu", "mMyVideoView onInfo i = " + i + ", i1 = " + i1);
                if (i == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED && i1 != 0) {
                    Log.i("wangzixu", "mMyVideoView onInfo 切换竖屏");

                }
                return false;
            }

            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                Log.i("wangzixu", "mMyVideoView ***onPrepared***");
//                mPlayBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                Log.i("wangzixu", "mMyVideoView onSeekComplete");
            }

            @Override
            public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
                Log.i("wangzixu", "mMyVideoView onVideoSizeChanged i = " + i + ", i1 = " + i1 + ", i2 = " + i2 + ", i3 = " + i3);
                Log.i("wangzixu", "mMyVideoView onVideoSizeChanged w = " + iMediaPlayer.getVideoWidth() + ", h = " + iMediaPlayer.getVideoHeight());

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                mMyVideoView.setVideoPath("http://qkk.oss-cn-beijing.aliyuncs.com/qkk/travel/cc2f7669b62c4759a3ea0f2ecb0582e7/1510902981.mp4");
                mMyVideoView.start();

//                mIjkVideoView.setVideoPath("http://qkk.oss-cn-beijing.aliyuncs.com/1468918230/d83394bf241243619b8dc7fe2ccc8581.mov");
//                mIjkVideoView.start();
                break;
            default:
                break;
        }
    }

    /**
     * 设置状态栏透明
     */
    public static void setStatusBarTransparnet(Activity activity) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE //让应用的主体内容占用系统状态栏和导航栏的空间
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); //内容填充进statusbar下面（android:fitsSystemWindows="false"）
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window =activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMyVideoView.stopPlayback();

//        mIjkVideoView.stopPlayback();
    }

    @Override
    protected void onDestroy() {
        IjkMediaPlayer.native_profileEnd();
        super.onDestroy();
    }
}
