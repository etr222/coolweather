package com.example.coolweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.FontsContract;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;

    //    设置成public 可以让其他类直接使用。
    public SwipeRefreshLayout swipeRefreshLayout;
    private String mWeatherId;
    public DrawerLayout drawerLayout;
    private Button navButton;

    private static final int FALSECODE =1001 ;
    private static final int TRUECODE =1002 ;
    private static final int PICTRUECODE =1003 ;
    private Handler handler;
    private String responseText="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

//        简单实现背景图和状态栏融合的效果。
//        判断系统版本号。
        if (Build.VERSION.SDK_INT>=21){

            View decorView=getWindow().getDecorView();

            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }



        weatherLayout=findViewById(R.id.weather_layout);
        titleCity=findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        forecastLayout=findViewById(R.id.forecast_layout);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);
        bingPicImg=findViewById(R.id.bing_pic_img);

//        下拉刷新控件绑定。
        swipeRefreshLayout=findViewById(R.id.swipe_refresh);
//        设置下拉刷新的主题样式。
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout=findViewById(R.id.drawer_layout);
        navButton=findViewById(R.id.nav_button);

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


//        获得SharedPreferences对象。通过PreferenceManager.getDefaultSharedPreferences(Context类);
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
//        获取数据通过SharedPreferences对象。通过SharedPreferences对象.getXXX(key,取不到时的默认值);
        String weatherString=sp.getString("weather",null);

        if (weatherString!=null){
//            有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
//            无缓存区服务器查询天气
            mWeatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
//        设置下拉刷新的事件监听。通过SwipeRefreshLayout对象.setOnRefreshListener();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
                Toast.makeText(WeatherActivity.this, "重新获取数据成功", Toast.LENGTH_SHORT).show();
            }
        });


        String bingPic=sp.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }


        handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what==FALSECODE){
                    Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    return true;
                }else if (msg.what==TRUECODE){
//                    SharedPreferences的使用：
//                    第一步：先得到SharedPreferences的对象。有三种方法。第一种是Context类.getSharedPreferences(指定文件名，指定操作模式);
//                            第二种是Activity类.getPreferences(指定操作模式);
//                            第三种是PreferenceManager.getDefaultSharedPreferences(Context对象);
//                    第二步：获取SharedPreferences.Editor对象。通过SharedPreferences对象.edit();
//                    第三步：添加数据。SharedPreferences.Editor对象.putXXX(key,value);
//                    第四步：提交并存储数据。SharedPreferences.Editor对象.apply();
//                    第五步：获取数据。创建SharedPreferences的对象。SharedPreferences对象.getXXX(key，取不到的选择的默认值);
                    SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("weather",responseText);
                    editor.apply();
                    showWeatherInfo((Weather) msg.obj);
                    swipeRefreshLayout.setRefreshing(false);
                    return true;
                }else if (msg.what==PICTRUECODE){
                    String bingPic= (String) msg.obj;
                    Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    return true;
                }
                return false;
            }
        });

    }

    private void loadBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                Message message=new Message();
                message.what=PICTRUECODE;
                message.obj=bingPic;
                handler.sendMessage(message);
            }
        });
    }


    /*
    * 根据天气id请求城市天气信息。
    * */
    public void requestWeather(String weatherId) {
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=3ab94192e240431d9fae34675bb4a503";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message=new Message();
                message.what=FALSECODE;
                message.obj=e.toString();
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseText=response.body().string();
                Weather weather=Utility.handleWeatherResponse(responseText);
                Message message=new Message();
                message.what=TRUECODE;
                message.obj=weather;
                handler.sendMessage(message);
            }
        });
        loadBingPic();
    }

    /*
    * 处理并展示Weather实体类中的数据。
    * */
    private void showWeatherInfo(Weather weather) {
        String cityName=weather.basic.cityName;
        String updataTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updataTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast:weather.forecastList){
//            动态加载布局文件。
            View view= LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item,forecastLayout,false);

            TextView dateText=view.findViewById(R.id.date_text);
            TextView infoText=view.findViewById(R.id.info_text);
            TextView maxText=view.findViewById(R.id.max_text);
            TextView minText=view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            forecastLayout.addView(view);
        }

        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);


        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }



}
