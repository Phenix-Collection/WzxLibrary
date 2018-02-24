package com.wzx.library.reflectdemo.viewinterface;

/**
 * Created by wangzixu on 2018/2/5.
 */
public interface ILockScreenViewProxy extends ILockScreenView {
    void delayAutoUpdate();
    void loadLocalImgDate(boolean b);
    void loadOfflineNetData();
}
