package com.wzx.library.reflectdemo;

import android.content.Intent;

/**
 * Created by wangzixu on 2018/1/24.
 * 锁屏的客户端进程, 应该具备的一些方法, 以便服务端调用
 */
public interface ILockScreenClient {
    void onStartActivity(Intent intent);
}
