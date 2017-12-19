package com.wzx.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.amap.api.maps2d.MapView;

/**
 * Created by wangzixu on 2017/9/1.
 */
public class ActivitySurfaceView extends Activity implements View.OnClickListener {

    private MapView mMapView;
    private MySurfaceView mMySurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surfaceview);

        mMySurfaceView = (MySurfaceView) findViewById(R.id.mysurfaceview);
        findViewById(R.id.clear).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                mMySurfaceView.clear();
                break;
            default:
                break;
        }
    }
}
