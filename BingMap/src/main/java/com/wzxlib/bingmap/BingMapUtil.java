package com.wzxlib.bingmap;

/**
 * Created by wangzixu on 2017/9/20.
 */
public class BingMapUtil {

    /**
     * 返回逆地理编码的必应链接, 通过get请求此链接, 即可返回相关的地址信息
     * Adress: landmark + locality + TextUtil.isEmpty(adminDistrict)?adminDistrict2:adminDistrict + countryRegion
     * @return
     */
    public static String getReGeoCodeUrl(String latitude, String longitude) {
        String url = "http://dev.virtualearth.net/REST/v1/Locations/" + latitude + "," + longitude + "?key=AvgkhHwzQCBeVQIfq6Wk7PbwLNAI6VD_ab5DCBBPLAQxn656muDx6Ow4Fj-WTBLy";
        return url;
    }

    public static String getReGeoCodeUrl(double latitude, double longitude) {
        String url = "http://dev.virtualearth.net/REST/v1/Locations/" + latitude + "," + longitude + "?key=AvgkhHwzQCBeVQIfq6Wk7PbwLNAI6VD_ab5DCBBPLAQxn656muDx6Ow4Fj-WTBLy";
        return url;
    }
}
