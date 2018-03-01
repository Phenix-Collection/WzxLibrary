package com.wzx.library.reflectdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wangzixu on 2018/2/5.
 */
public class LockScreenClient implements ILockScreenClient {
    public static final String TAG = "LockScreenClient ";
    public static final String REMOTE_PACKAGE = "com.haokanhaokan.lockscreen";
    public static final String REMOTE_CLASSNAME = "com.haokan.hklockscreen.lockscreen.control.LockScreenViewController";
    Activity mContext;
    private Object mRemoteViewProxy;
    private Class mRemoteViewClazz;

    public LockScreenClient(Activity context) {
        mContext = context;
        getRemoteView();
    }

    private void getRemoteView() {
        //反射获取view
        try{
            Context remoteContext = mContext.createPackageContext(REMOTE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);

            ClassLoader classLoader = remoteContext.getClassLoader();
            //替换类加载器的parent
//            Field parent = ClassLoader.class.getDeclaredField("parent");
//            parent.setAccessible(true);
//            Object o = parent.get(classLoader);
//            Log.i("reflectdemo", "ClassLoader parent = " + o);
//            parent.set(classLoader, ISystemUiView.class.getClassLoader());

            //通过createPackageContext创建的context, 获取的getApplicitonContext为null, 通过一系列反射, 给这个app赋值
            //获取    final @NonNull LoadedApk mPackageInfo;
            Field mPackageInfo = remoteContext.getClass().getDeclaredField("mPackageInfo");
            mPackageInfo.setAccessible(true);
            Object o = mPackageInfo.get(remoteContext);
            Field mApplication = o.getClass().getDeclaredField("mApplication");
            mApplication.setAccessible(true);
            mApplication.set(o, mContext.getApplication());
//            Application application = new Application();
//            Method attach = Application.class.getDeclaredMethod("attach", Context.class);
//            attach.setAccessible(true);
//            attach.invoke(application, remoteContext);
//            Log.i("wangzixu", TAG + " mRemoteViewProxy remoteContext.getApplicationContext = " + remoteContext.getApplicationContext());

            mRemoteViewClazz = classLoader.loadClass(REMOTE_CLASSNAME);
            Constructor constructor = mRemoteViewClazz.getConstructor(Context.class);
            mRemoteViewProxy = constructor.newInstance(remoteContext);
            setClient();
        } catch (Exception e) {
            Log.i("wangzixu", TAG + " getRemoteView exception = " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setClient() {
        if (mRemoteViewProxy != null) {
            try {
                Method method = mRemoteViewClazz.getDeclaredMethod("setRemoteClient", Object.class);
                if (method != null) {
                    method.invoke(mRemoteViewProxy, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadDate() {
        if (mRemoteViewProxy != null) {
            try {
                Method method = mRemoteViewClazz.getDeclaredMethod("loadLocalImgDate", boolean.class);
                if (method != null) {
                    method.invoke(mRemoteViewProxy, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public View getLockScreenView() {
        if (mRemoteViewProxy != null) {
            try {
                Method method = mRemoteViewClazz.getDeclaredMethod("getLockScreenView");
                if (method != null) {
                    return (View) method.invoke(mRemoteViewProxy);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("HaokanLockView", "init remote == null");
        }
        return null;
    }

    public void onDestroy() {

    }

    //作为锁屏client必须具备的方法, 锁屏view中会调用到begin
    @Override
    public void onStartActivity(Intent intent) {
        //打开activity了, 客户端是否要采取一些动作, 如解除锁屏, 弹出密码界面等
    }
    //作为锁屏client必须具备的方法, 锁屏view中会调用到end
}
