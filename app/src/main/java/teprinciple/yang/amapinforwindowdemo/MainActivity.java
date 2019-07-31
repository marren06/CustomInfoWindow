package teprinciple.yang.amapinforwindowdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import teprinciple.yang.amapinforwindowdemo.adapter.InfoWinAdapter;
import teprinciple.yang.amapinforwindowdemo.entity.Constant;
import teprinciple.yang.amapinforwindowdemo.utils.CheckPermissionsActivity;


//test  git  tag
public class MainActivity extends CheckPermissionsActivity implements AMap.OnMapTouchListener,
        AMap.OnMarkerClickListener, AMapLocationListener, LocationSource {

    private MapView mapView;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient;
    //声明AMapLocationClientOption对象
    private AMapLocationClientOption mLocationOption;
    private AMap aMap;
    private UiSettings uiSettings;
    private InfoWinAdapter adapter;
    private LocationSource.OnLocationChangedListener mListener;
    private LatLng myLatLng;
    private Marker oldMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //在执行onCreateView时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mapView.onCreate(savedInstanceState);
        initOperation();
    }

    private void initView() {
        mapView = (MapView) initV(R.id.mapView);
    }

    private void initOperation() {
        initMap();
        initLocation();
    }

    /**
     * 初始化地图
     */
    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            uiSettings = aMap.getUiSettings();
            aMap.setOnMapTouchListener(this);
        }
        uiSettings.setZoomControlsEnabled(false); //隐藏缩放控件
        //自定义InfoWindow
        aMap.setOnMarkerClickListener(this);
        adapter = new InfoWinAdapter();
        aMap.setInfoWindowAdapter(adapter);
        addMarkerToMap(Constant.SHANGHAI,"上海","上海市");
    }


    /**
     * 初始化定位
     */
    private void initLocation(){
        // 自定义系统定位小蓝点--我的位置
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.mylocation));// 设置小蓝点的图标
        myLocationStyle.strokeColor(getResources().getColor(R.color.blue));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 29, 161, 242));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
//        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
//        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位资源。如果不设置此定位资源则定位按钮不可点击。并且实现activate激活定位,停止定位的回调方法
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }



    //激活定位
    //记得注册定位
    //<service android:name="com.amap.api.location.APSService"/>
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setOnceLocation(true);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        }
    }

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume(); //管理地图的生命周期
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause(); //管理地图的生命周期
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy(); //管理地图的生命周期
    }



    //maker的点击事件
    @Override
    public boolean onMarkerClick(Marker marker) {
        oldMarker = marker;
        return false;
    }

    //定位成功的监听
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if (aMapLocation != null) {
            if(mListener != null){
//                aMap.clear();  清除之前的marker
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点-我的位置
            }

            if (aMapLocation.getErrorCode() == 0) {
                myLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 14));
                String city = aMapLocation.getCity();
                String address = aMapLocation.getAddress();
//                addMarkerToMap(latLng,city,address);
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    private void addMarkerToMap(LatLng latLng, String title, String snippet) {
        aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_normal))
        );
    }

    //用OnMapClick()方法不起作用
    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (aMap != null && oldMarker != null) {
            if (oldMarker.isInfoWindowShown()){
                oldMarker.hideInfoWindow();
            }
        }
    }
}
