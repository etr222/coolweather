package com.example.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//碎片化的使用：
//    第一步：自定义一个类集成Fragment。
//    第二步：重写onCreateView()方法。并在该方法中，动态加载布局。通过LayoutInflater对象.inflate(资源文件路径，ViewGroup对象，false);
//    第三步：绑定控件。
public class ChooseAreaFragment extends Fragment{

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;

    private static final int TRUECODE = 1001;
    private static final int FALSECODE =1002 ;

    private ProgressDialog progressDialog;

    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private List<String> dataList=new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;


    private int currentLevel;

    private Handler handler;

    private View view;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

//        动态加载布局。通过LayoutInflater对象.inflate(资源文件路径，ViewGroup对象，false);
        view=inflater.inflate(R.layout.choose_area,container,false);
        Log.e("check","1");
//        绑定控件。
        titleText=view.findViewById(R.id.title_text);
        backButton=view.findViewById(R.id.back_button);
        listView=view.findViewById(R.id.list_view);

        adapter=new ArrayAdapter<>(view.getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){

                    selectedProvince= provinceList.get(position);

                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity= cityList.get(position);
                    queryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
//        调用查询省份数据的方法。
        queryProvinces();

        handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                closeProgressDialog();
                if(msg.what==TRUECODE){
                    String type= (String) msg.obj;
                    if ("province".equals(type)){
                        queryProvinces();
                    }else if ("city".equals(type)){
                        queryCities();
                    }else if ("county".equals(type)){
                        queryCounties();
                    }
                    return true;
                }
                if (msg.what==FALSECODE){
                    String e= (String) msg.obj;
                    Toast.makeText(view.getContext(), "加载失败"+e, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    /*
    * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
    * */
    private void queryProvinces() {
        titleText.setText("中国");
//        设置button按钮的可见性。
        backButton.setVisibility(View.GONE);
//        Litepal的查询所有数据。通过DataSupport.findAll(Bean类);
        provinceList= DataSupport.findAll(Province.class);
//        判断litepal查询到的数据。
        if (provinceList.size()>0){
//            清除ArrayList的数据。
            dataList.clear();
            for (Province province:provinceList){
//                向ArrayList添加数据。
                dataList.add(province.getProvinceName());
            }
//            更新数据。Adapter对象.notifyDataSetChanged();
            adapter.notifyDataSetChanged();
//            ListView设置界面位置。
            listView.setSelection(0);

            currentLevel=LEVEL_PROVINCE;
        }else {

            String address="http://guolin.tech/api/china";
//            当litepal数据库无数据。则使用okhttp3访问API接口查询数据。
            queryFromServer(address,"province");
        }
    }

    /*
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     * */
    private void queryCities() {

        titleText.setText(selectedProvince.getProvinceName());
//        设置Button按钮的可见性。
        backButton.setVisibility(View.VISIBLE);
//        litepal查询where条件数据。通过DataSupport.where(key,value).find(Bean类);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
//        判断litepal查询到的数据。
        if (cityList.size()>0){
//            清除ArrayList的数据。
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
//            更新数据。Adapter对象.notifyDataSetChanged();
            adapter.notifyDataSetChanged();
//            ListView设置界面位置。
            listView.setSelection(0);

            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
//            当litepal数据库无数据。则使用okhttp3访问API接口查询数据。
            queryFromServer(address,"city");
        }

    }

    /*
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     * */
    private void queryCounties() {

        titleText.setText(selectedCity.getCityName());
//        设置Button按钮的可见性。
        backButton.setVisibility(View.VISIBLE);
//        litepal查询where条件数据。通过DataSupport.where(key,value).find(Bean类);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
//        判断litepal查询到的数据。
        if (countyList.size()>0){
//            清除ArrayList的数据。
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
//            更新数据。Adapter对象.notifyDataSetChanged();
            adapter.notifyDataSetChanged();
//            ListView设置界面位置。
            listView.setSelection(0);

            currentLevel=LEVEL_COUNTY;

        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    /*
     * 根据传入的地址和类型从服务器上查询省市县数据。
     * */
    private void queryFromServer(String address, final String type) {
//        显示进度条ProgressDialog。
        showProgressDialog();
//        使用Okhttp:
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message=new Message();
                message.what=FALSECODE;
                message.obj=e.toString();
                handler.sendMessage(message);
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        closeProgressDialog();
//                        Toast.makeText(view.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                获取服务器返回的数据。通过Response对象.body().string();
                String responseText=response.body().string();

                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    Message message=new Message();
                    message.what=TRUECODE;
                    message.obj=type;
                    handler.sendMessage(message);
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            closeProgressDialog();
//                            if ("province".equals(type)){
//                                queryProvinces();
//                            }else if ("city".equals(type)){
//                                queryCities();
//                            }else if ("county".equals(type)){
//                                queryCounties();
//                            }
//                        }
//                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

}
