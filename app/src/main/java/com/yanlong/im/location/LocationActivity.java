package com.yanlong.im.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.yanlong.im.MyAppLication;
import com.yanlong.im.R;
import com.yanlong.im.chat.bean.LocationMessage;
import com.yanlong.im.listener.BaseListener;
import com.yanlong.im.view.MaxHeightRecyclerView;
import net.cb.cb.library.utils.GsonUtils;
import net.cb.cb.library.utils.InputUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.view.ClearEditText;
import net.cb.cb.library.view.HeadView;
import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;

/**
 * zgd 位置 地图搜索
 * 20191214
 */

public class LocationActivity extends AppActivity {
    private HeadView headView;
    private ActionbarView actionbar;
    private MapView mapview;
    private ClearEditText edtSearch;
    private MaxHeightRecyclerView recyclerview;
    private RelativeLayout search_ll;

    private BaiduMap mBaiduMap;
    private LocationService locService;
    private BDAbstractLocationListener listener;
    private List<LocationMessage> locationList;
    private LocationPoiAdapter locationPoiAdapter;

    private Boolean isShow=true;
    private String city="长沙市";//默认城市
    private int latitude=28136296;//默认定位
    private int longitude=112953042;//默认定位



    public static void openActivity(Activity activity,Boolean isShow ,int latitude,int longitude) {
        if (!LocationPersimmions.checkPermissions(activity)) {
            return;
        }
        if(!LocationUtils.isLocationEnabled(activity)){
            ToastUtil.show(activity,"请打开定位服务");
            return;
        }
        Intent intent=new Intent(activity, LocationActivity.class);
        intent.putExtra("isShow",isShow);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        findViews();
        initEvent();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        locService.unregisterListener(listener);
        locService.stop();
        mapview.onDestroy();
    }


    //自动寻找控件
    private void findViews() {
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        mapview = findViewById(R.id.mapview);
        edtSearch = findViewById(R.id.edt_search);
        search_ll = findViewById(R.id.search_ll);

        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setVisibility(View.GONE);

        locationList = new ArrayList<>();
        locationPoiAdapter = new LocationPoiAdapter(context, locationList);
        recyclerview.setAdapter(locationPoiAdapter);
        locationPoiAdapter.setListener(new BaseListener() {
            @Override
            public void onSuccess(Object object) {
                super.onSuccess(object);
                if(object!=null){
                    LocationMessage locationMessage=(LocationMessage) object;
                    if(locationMessage.getLatitude()==-1||locationMessage.getLongitude()==-1){
                        getPoi(true,city,locationMessage.getAddress());
                    }else {
                        setLocationBitmap(locationMessage.getLatitude()/LocationUtils.beishu, locationMessage.getLongitude()/LocationUtils.beishu);
                    }
                }
            }
        });
    }


    //自动生成的控件事件
    private void initEvent() {
        actionbar.getBtnLeft().setVisibility(View.GONE);
        actionbar.setTxtLeft("取消");
        isShow=getIntent().getBooleanExtra("isShow",true);
        latitude=getIntent().getIntExtra("latitude",28136296);
        longitude=getIntent().getIntExtra("longitude",112953042);

        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                if (locationList.size() > locationPoiAdapter.position) {
                    LocationMessage message = locationList.get(locationPoiAdapter.position);
                    if(message.getLatitude()==-1||message.getLongitude()==-1){
                        getPoi(false,city,locationList.get(locationPoiAdapter.position).getAddress());
                    }else {
                        EventBus.getDefault().post(new LocationSendEvent(message));

                        finish();
                    }
                } else {
                    ToastUtil.show(context, "请选择定位的地址");
                }
            }
        });




        //百度地图参数
        mBaiduMap = mapview.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(18));

        locService = ((MyAppLication) getApplication()).locationService;
        LocationClientOption mOption = locService.getDefaultLocationClientOption();
        mOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        mOption.setCoorType("bd09ll");
        locService.setLocationOption(mOption);

        setLocationBitmap(latitude/LocationUtils.beishu, longitude/LocationUtils.beishu);//设置默认定位

        listener = new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                LogUtil.getLog().e("=location====" + GsonUtils.optObject(bdLocation));

                try {
                    if (bdLocation != null&&bdLocation.getPoiList()!=null) {
                        city = bdLocation.getCity();

                        locationList.clear();
                        locationPoiAdapter.position=0;
                        for (int i = 0; i < bdLocation.getPoiList().size(); i++) {
                            LocationMessage locationMessage=new LocationMessage();
                            if(i==0){
                                locationMessage.setLatitude((int)(bdLocation.getLatitude()*LocationUtils.beishu));
                                locationMessage.setLongitude((int)(bdLocation.getLongitude()*LocationUtils.beishu));
                                locationMessage.setImg(LocationUtils.getLocationUrl2(bdLocation.getLatitude(), bdLocation.getLongitude()));
                            }
                            locationMessage.setAddress(bdLocation.getPoiList().get(i).getName());
                            locationMessage.setAddressDescribe(bdLocation.getPoiList().get(i).getAddr());
                            locationList.add(locationMessage);

                            getPoi(false,city,bdLocation.getPoiList().get(i).getName());
                        }
                        recyclerview.getAdapter().notifyDataSetChanged();
                        recyclerview.setVisibility(View.VISIBLE);

                        setLocationBitmap(bdLocation.getLatitude(), bdLocation.getLongitude());
                        locService.stop();//定位成功后停止点位
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        locService.registerListener(listener);

        if(isShow){
            search_ll.setVisibility(View.GONE);
        }else {
            actionbar.setTxtRight("发送");
            locService.start();
        }


        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    search();
                }
                return false;
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtSearch.getText().toString().length() == 0) {
                    //搜索关键字为0的时候，重新显示全部消息
//                    locService.start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void setLocationBitmap(double latitude, double longitude) {
        LogUtil.getLog().e("===location====" + latitude + "====" + longitude);
        mBaiduMap.clear();
        LatLng point = new LatLng(latitude, longitude);
        // 构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.location_two); // 非推算结果

        // 构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        // 在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
    }


    //搜索
    private void search() {
        String key = edtSearch.getText().toString();
        LogUtil.getLog().e("=location===key=" + key);
        if (!StringUtil.isNotNull(key)) {
            return;
        }

        InputUtil.hideKeyboard(edtSearch);

        locationList.clear();
        locationPoiAdapter.position=0;
        recyclerview.getAdapter().notifyDataSetChanged();

        //建议搜索
        SuggestionSearch mSuggestionSearch = SuggestionSearch.newInstance();
        OnGetSuggestionResultListener listener2 = new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                LogUtil.getLog().e("=location===建议搜索==suggestionResult=" + GsonUtils.optObject(suggestionResult));
                //处理sug检索结果
                if (suggestionResult != null && "NO_ERROR".equals(suggestionResult.error.name())
                        && suggestionResult.getAllSuggestions() != null&&suggestionResult.getAllSuggestions().size()>0) {
                    List<SuggestionResult.SuggestionInfo> list=suggestionResult.getAllSuggestions();
                    boolean hasSetBitmap=false;
                    for (int i = 0; i < list.size(); i++) {
                        SuggestionResult.SuggestionInfo sug=list.get(i);
                        if(sug!=null&&sug.pt!=null){
                            LocationMessage locationMessage=new LocationMessage();
                            locationMessage.setLatitude((int)(sug.pt.latitude*LocationUtils.beishu));
                            locationMessage.setLongitude((int)(sug.pt.longitude*LocationUtils.beishu));
                            locationMessage.setImg(LocationUtils.getLocationUrl2(sug.pt.latitude,sug.pt.longitude));
                            locationMessage.setAddress(sug.getKey());
                            locationMessage.setAddressDescribe(sug.getCity()+sug.getDistrict()+sug.getAddress());
                            locationList.add(locationMessage);

                            if(!hasSetBitmap){
                                setLocationBitmap(sug.pt.latitude,sug.pt.longitude);
                                hasSetBitmap=true;
                            }
                        }
                    }

                    recyclerview.getAdapter().notifyDataSetChanged();
                    recyclerview.setVisibility(View.VISIBLE);

                }
            }
        };

        mSuggestionSearch.setOnGetSuggestionResultListener(listener2);
        mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                .city(city)
                .keyword(key));

    }


    //获取经纬度
    private void getPoi(boolean isAddBitmap,String city, String address) {
        if (!StringUtil.isNotNull(city) || !StringUtil.isNotNull(city)) {
            return;
        }

        // 通过GeoCoder的实例方法得到GerCoder对象
        GeoCoder mGeoCoder = GeoCoder.newInstance();
        // 为GeoCoder设置监听事件
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            // 这个方法是将坐标转化为具体地址
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
                LogUtil.getLog().e("=location===地理编码搜索=arg0=" + GsonUtils.optObject(arg0));
            }

            // 将具体的地址转化为坐标
            @Override
            public void onGetGeoCodeResult(GeoCodeResult arg1) {
                LogUtil.getLog().e("=location===地理编码搜索=arg1=" + GsonUtils.optObject(arg1));
                if (arg1 != null && "NO_ERROR".equals(arg1.error.name()) && arg1.getLocation() != null) {
                    for (int i = 0; i < locationList.size(); i++) {
                        if(address.equals(locationList.get(i).getAddress())){
                            locationList.get(i).setLatitude((int)(arg1.getLocation().latitude*LocationUtils.beishu));
                            locationList.get(i).setLongitude((int)(arg1.getLocation().longitude*LocationUtils.beishu));
                            locationList.get(i).setImg(LocationUtils.getLocationUrl2(arg1.getLocation().latitude, arg1.getLocation().longitude));
                            break;
                        }
                    }

                    if(isAddBitmap){
                        setLocationBitmap(arg1.getLocation().latitude, arg1.getLocation().longitude);
                    }
                }
            }
        });
        //地理编码搜索   必须设置在监听后面，否则监听无法回调。  得到GenCodeOption对象
        mGeoCoder.geocode(new GeoCodeOption()
                .city(city)
                .address(address));
    }

}
