package com.wzx.library;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.amap.api.maps2d.MapView;
import com.wzx.library.map.ActivityMap;
import com.wzx.library.scheme.ActivitySchemePull;
import com.wzx.library.selectimages.ActivitySelectImages;
import com.wzx.library.surfaceview.ActivitySurfaceView;
import com.wzx.library.video.ActivityVideo;

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
        findViewById(R.id.goscheme).setOnClickListener(this);
        findViewById(R.id.selectimages).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gomap:
                Intent imap = new Intent(this, ActivityMap.class);
                startActivity(imap);
                break;
            case R.id.govideo:
                Intent ivideo = new Intent(this, ActivityVideo.class);
                startActivity(ivideo);
                break;
            case R.id.gosurface:
                Intent isurface = new Intent(this, ActivitySurfaceView.class);
                startActivity(isurface);
                break;
            case R.id.goscheme:
                Intent ischeme = new Intent(this, ActivitySchemePull.class);
                startActivity(ischeme);
                break;
            case R.id.selectimages:
                Intent iselect = new Intent(this, ActivitySelectImages.class);
                startActivity(iselect);
                break;
            default:
                break;
        }
    }
}
