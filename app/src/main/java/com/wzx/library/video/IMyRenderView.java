/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wzx.library.video;

import android.support.annotation.NonNull;
import android.view.View;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 承载video的view接口, 可以是surfaceview或者textureview, 以surfaceview为例
 * surfaceview和videoview之间需要互相配合和调用, 为了解耦, 有此接口
 */
public interface IMyRenderView {
    View getView();

    void setVideoSize(int videoWidth, int videoHeight);

    void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen);

    /**
     * 设置mediaplayer的显示视图, 因为不同的renderview设置方式不一样, 所以需要抽取成一个方法
     * @param mp
     */
    void bindToMediaPlayer(IMediaPlayer mp);

    /**
     * 设置角度
     * @param degree
     */
    void setVideoRotation(int degree);

    /**
     * 设置宽高比
     * @param aspectRatio
     */
    void setAspectRatio(int aspectRatio);

    void addRenderCallback(@NonNull IMyRenderCallback callback);

    void removeRenderCallback(@NonNull IMyRenderCallback callback);

    interface IMyRenderCallback {
        void onSurfaceCreated(int width, int height);

        void onSurfaceChanged(int width, int height);

        void onSurfaceDestroyed();
    }
}
