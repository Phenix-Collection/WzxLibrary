package com.wzx.library;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.amap.api.maps2d.MapView;

/**
 * Created by wangzixu on 2017/9/1.
 */
public class ActivityMain extends Activity implements View.OnClickListener {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.gomap).setOnClickListener(this);
        findViewById(R.id.govideo).setOnClickListener(this);
        findViewById(R.id.gosurface).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gomap:
                Intent imap = new Intent(this, ActivityMap.class);
                startActivity(imap);
                break;
            case R.id.govideo:
                Intent ivideo = new Intent(this, ActivityMap.class);
                startActivity(ivideo);
                break;
            case R.id.gosurface:
                Intent isurface = new Intent(this, ActivitySurfaceView.class);
                startActivity(isurface);
                break;
            default:
                break;
        }
    }
}
