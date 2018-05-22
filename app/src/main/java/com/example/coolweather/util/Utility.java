package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//JSONObject解析JSON数据：
//    第一步：创建JSONArray对象。通过new JSONArray(需解析的数据)。
//    第二步：利用循环遍历出每个JSONObject对象，创建JSONObject对象。通过JSONArray对象.getJSONObject();
//    第三步：获得JSON文件的字段的值。通过JSONObject对象.getString("字段名");


//litepal的使用：
//    第一步：添加依赖。
//    第二步：/main目录下创建assets目录新建一个litepal.xml文件。并配置litepal.xml。<litepal>标签。
//    第三步：AndroidManifest.xml文件的<application>标签添加        android:name="org.litepal.LitePalApplication"属性。
//    第四步：进行增删查改操作，要使Bean类继承DataSupport。
public class Utility {

    /*
    * 解析和处理服务器返回的省级数据。
    * */
    public static boolean handleProvinceResponse(String response){
//        TextUtils。Android自带的工具类。是否为空字符 boolean android.text.TextUtils.isEmpty(CharSequence str)
        if (!TextUtils.isEmpty(response)){
            try {
//                创建JSONArray对象。通过new JSONArray(需解析的数据)。
                JSONArray allProvinces=new JSONArray(response);
//                利用循环遍历出每个JSONObject对象，创建JSONObject对象。通过JSONArray对象.getJSONObject();
                for (int i=0;i<allProvinces.length();i++) {
                    JSONObject provinceObject=allProvinces.getJSONObject(i);

                    Province province = new Province();
//                    litepal添加数据。通过Bean类对象.save();
//                    获得JSON文件的字段的值。通过JSONObject对象.getString("字段名");
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
     * 解析和处理服务器返回的市级数据。
     * */
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
//                创建JSONArray对象。通过new JSONArray(需解析的数据)。
                JSONArray allCities=new JSONArray(response);
//                利用循环遍历出每个JSONObject对象，创建JSONObject对象。通过JSONArray对象.getJSONObject();
                for (int i=0;i<allCities.length();i++) {
                    JSONObject cityObject=allCities.getJSONObject(i);
                    City city = new City();
//                    litepal添加数据。通过Bean类对象.save();
//                    获得JSON文件的字段的值。通过JSONObject对象.getString("字段名");
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
     * 解析和处理服务器返回的县级数据。
     * */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
//                创建JSONArray对象。通过new JSONArray(需解析的数据)。
                JSONArray allCounties=new JSONArray(response);
//                利用循环遍历出每个JSONObject对象，创建JSONObject对象。通过JSONArray对象.getJSONObject();
                for (int i=0;i<allCounties.length();i++) {
                    JSONObject countiesObject=allCounties.getJSONObject(i);
                    County county = new County();
//                    litepal添加数据。通过Bean类对象.save();
//                    获得JSON文件的字段的值。通过JSONObject对象.getString("字段名");
                    county.setCountyName(countiesObject.getString("name"));
                    county.setWeatherId(countiesObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }



}
