package com.wzx.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.amap.api.maps2d.MapView;

/**
 * Created by wangzixu on 2017/9/1.
 */
public class ActivityVideo extends Activity implements View.OnClickListener {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.gomap:
//                Intent imap = new Intent(this, ActivityMap.class);
//                startActivity(imap);
//                break;
            default:
                break;
        }
    }
}
