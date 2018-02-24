package com.wzx.library.reflectdemo.control;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.wzx.library.reflectdemo.viewinterface.ILockScreenViewController;
import com.wzx.library.reflectdemo.viewinterface.ILockScreenViewProxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wangzixu on 2018/2/5.
 */
public class LockScreenViewProxy implements ILockScreenViewProxy {
    public static final String REMOTE_PACKAGE = "com.haokanhaokan.lockscreen";
    public static final String REMOTE_CLASSNAME = "com.haokan.hklockscreen.lockscreen.view.LockScreenViewProxy";
    Activity mContext;
    private Object mRemoteViewProxy;
    private Class mRemoteViewClazz;

    public LockScreenViewProxy(Activity context) {
        mContext = context;
        getRemoteView();
    }

    private void getRemoteView() {
        //反射获取view
        try{
            long start =  SystemClock.currentThreadTimeMillis();
            Context remoteContext = mContext.createPackageContext(REMOTE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);

            //验证是否会把assets资源加载过来
//            try {
//                InputStream open = remoteContext.getAssets().open("default_offline_china.txt");
//                LogHelper.d("wangzixu", "getRemoteView 取默认图 list = " + open);
//            } catch (Exception e) {
//                LogHelper.d("wangzixu", "getRemoteView 取默认图 Exception");
//                e.printStackTrace();
//            }

            ClassLoader classLoader = remoteContext.getClassLoader();
            //替换类加载器的parent
//            Field parent = ClassLoader.class.getDeclaredField("parent");
//            parent.setAccessible(true);
//            Object o = parent.get(classLoader);
//            Log.i("reflectdemo", "ClassLoader parent = " + o);
//            parent.set(classLoader, ISystemUiView.class.getClassLoader());

            //通过createPackageContext创建的context, 获取的getApplicitonContext为null
            //获取    final @NonNull LoadedApk mPackageInfo;
            Field mPackageInfo = remoteContext.getClass().getDeclaredField("mPackageInfo");
            mPackageInfo.setAccessible(true);
            Object o = mPackageInfo.get(remoteContext);
//            Log.i("wangzixu", "mRemoteViewProxy o = " + o.getClass());
            Field mApplication = o.getClass().getDeclaredField("mApplication");
            mApplication.setAccessible(true);
            mApplication.set(o, mContext.getApplication());
//            Application application = new Application();
//            Method attach = Application.class.getDeclaredMethod("attach", Context.class);
//            attach.setAccessible(true);
//            attach.invoke(application, remoteContext);
            Log.i("wangzixu", "mRemoteViewProxy remoteContext.getApplicationContext = " + remoteContext.getApplicationContext());

            mRemoteViewClazz = classLoader.loadClass(REMOTE_CLASSNAME);
            Constructor constructor = mRemoteViewClazz.getConstructor(Context.class);
            mRemoteViewProxy = constructor.newInstance(remoteContext);
            Log.i("reflectdemo", "mRemoteViewProxy, time = " + mRemoteViewProxy + ", " + String.valueOf(SystemClock.currentThreadTimeMillis() - start));
        } catch (Exception e) {
            Log.i("reflectdemo", "exception = " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void delayAutoUpdate() {

    }

    @Override
    public void loadLocalImgDate(boolean b) {
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

    @Override
    public void loadOfflineNetData() {
        if (mRemoteViewProxy != null) {
            try {
                Method method = mRemoteViewClazz.getDeclaredMethod("loadOfflineNetData");
                if (method != null) {
                    method.invoke(mRemoteViewProxy);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
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


    @Override
    public void updateDateTime() {

    }

    @Override
    public void screenOff() {

    }

    @Override
    public void screenOn() {

    }

    @Override
    public void backLockscreen(boolean isLock) {

    }

    @Override
    public void beginUpdateImage(String progress) {

    }

    @Override
    public void endUpdateImage() {

    }

    //    @Override
//    public void setLockViewData(ArrayList<BigImageBean> list, int initPos) {
//
//    }

    @Override
    public void setRecommendViewData() {

    }

    @Override
    public void setController(ILockScreenViewController controller) {

    }

    @Override
    public void onDestroy() {

    }
}
