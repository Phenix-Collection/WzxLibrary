package com.wzx.library.reflectdemo.viewinterface;

import android.view.View;

/**
 * Created by wangzixu on 2018/1/24.
 * 锁屏View的抽象接口, 封装了锁屏view上的一些动作
 */
public interface ILockScreenView {
    View getLockScreenView();

    /**
     * 更新时间显示
     */
    void updateDateTime();

    /**
     * screen off
     */
    void screenOff();

    void screenOn();

    /**
     * 回锁屏界面, 是否锁屏状态
     */
    void backLockscreen(boolean isLock);

    /**
     * 开始更新锁屏图片
     */
    void beginUpdateImage(String progress);

    /**
     * 结束更新锁屏图片
     */
    void endUpdateImage();

    /**
     * 刷新锁屏页面数据
     */
//    void setLockViewData(ArrayList<BigImageBean> list, int initPos);

    /**
     * 刷新推荐页面数据
     */
    void setRecommendViewData();

    void setController(ILockScreenViewController controller);

    /**
     * 做一些释放资源的动作
     */
    void onDestroy();
}
