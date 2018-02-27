package com.wzx.library.reflectdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.wzx.library.R;
import com.wzx.library.util.StatusBarUtil;

/**
 * Created by wangzixu on 2018/2/5.
 */
public class ActivityLockDemo extends Activity {
    private ViewGroup mContentView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockdemo);
        StatusBarUtil.setStatusBarTransparnet(this);

        mContentView = findViewById(R.id.content);

        LockScreenClient lockScreenViewProxy = new LockScreenClient(this);
        View screenView = lockScreenViewProxy.getLockScreenView();

        Log.d("wangzixu", "ActivityLockDemo screenView = " + screenView);
        if (screenView != null) {
            mContentView.addView(screenView);
            lockScreenViewProxy.loadDate();
        }
    }
}
