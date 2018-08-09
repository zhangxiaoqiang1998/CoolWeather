package com.coolweather.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.BaseFragment;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/8/9.
 */

public class ChooseAreaFragment extends BaseFragment {
    public static final int LEVER_PROVINCE = 0;
    public static final int LEVER_CITY = 1;
    public static final int LEVER_COUNTY = 2;
    private ProgressDialog ProgressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listview;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<County> countyList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City seletedCity;
    /**
     * 当前选中的级别
     */
    private int currentLever;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listview = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listview.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                showProgressDialog();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (currentLever == LEVER_PROVINCE){
                            selectedProvince = provinceList.get(position);
                            queryCities();
                        }else if (currentLever == LEVER_CITY){
                            seletedCity = cityList.get(position);
                            queryCounties();
                        }

                    }
                },1000);

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLever == LEVER_COUNTY){
                    queryCities();
                }else if (currentLever == LEVER_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }



    /**
     * 查询全国所有的省,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    private void queryProvinces(){
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            titleText.setText("中国");
            backButton.setVisibility(View.GONE);
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProincecName());
            }
            closeProgressDialog();
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLever = LEVER_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     *查询省内所有的市,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            titleText.setText(selectedProvince.getProincecName());
            backButton.setVisibility(View.VISIBLE);
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            closeProgressDialog();
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLever = LEVER_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     *查询选中市内所有的县,优先从数据库查询,如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        countyList = DataSupport.where("cityid = ?",String.valueOf(seletedCity.getId())).find(County.class);
        if (countyList.size()>0){
            titleText.setText(seletedCity.getCityName());
            backButton.setVisibility(View.VISIBLE);
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            closeProgressDialog();
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLever = LEVER_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = seletedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }

    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */

    private void queryFromServer(String address, final String type){
        //showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,seletedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if (ProgressDialog == null){
            ProgressDialog = new ProgressDialog(getActivity());
            ProgressDialog.setMessage("正在加载...");
            ProgressDialog.setCanceledOnTouchOutside(false);
        }
        ProgressDialog.show();
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (ProgressDialog!=null){
            ProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onBackPressed() {
        //return super.onBackPressed();
        if (currentLever == LEVER_COUNTY){
            queryCities();
        }else if (currentLever == LEVER_CITY){
            queryProvinces();
        }else if (currentLever == LEVER_PROVINCE){
            return super.onBackPressed();
        }
        return true;
    }
}
