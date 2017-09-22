package com.wzx.library;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.MapsInitializer;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

/**
 * Created by wangzixu on 2017/9/1.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_goskip).setOnClickListener(this);
        MapsInitializer.loadWorldGridMap(true);

        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);

        AMap aMap = mMapView.getMap();

//        double loi = 43.828;
//        double lai = 87.621;
//        double loi1 = 45.808;
//        double lai1 = 126.55;

        double loi = 55.454;
        double lai = 11.788;

        double loi1 = 58.454;
        double lai1 = 13.788;

        LatLng latLng = new LatLng(loi, lai);
        LatLng latLng1 = new LatLng(loi1, lai1);
        LatLng latLng2 = new LatLng((loi + loi1)/2f, (lai + lai1)/2f);

        aMap.moveCamera(CameraUpdateFactory.zoomTo(4));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng2));

        //画点
        aMap.addMarker(new MarkerOptions().position(latLng));
        aMap.addMarker(new MarkerOptions().position(latLng1));

        //划线
        aMap.addPolyline((new PolylineOptions()).add(latLng, latLng1).color(Color.BLUE).width(3.0f));


        GeocodeSearch geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                String getFormatAddress = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                Log.d("geocoderSearch", "i = " + i + ", getFormatAddress = " + getFormatAddress);
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });

        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(loi, lai), 200f, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_goskip:
                Intent intent = new Intent(this, ActivitySkipApp.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
