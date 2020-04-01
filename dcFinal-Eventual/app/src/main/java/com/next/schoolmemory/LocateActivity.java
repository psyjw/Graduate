package com.next.schoolmemory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

//import android.support.v7.widget.Toolbar;//注意所有类中的toolbar都是用V7的toolbar!!!
//由于MainActivity已经添加过定位请求，所以这里不用再提醒用户了 No need to remind users
//这个Acitivity的目的在于帮助用户添加定位，实现方法和MapFragment中的方法几乎一样。因此不再注释。MapFragment有详细注释
public class LocateActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private LocationClient mLocationClient=null;
    private BDLocation latestUserLocation;
    private Button backToLocationButton;
    private boolean FirstTimeLoc = true;
    private LatLng latestMiddlePoint;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//此行用于去除默认的顶部栏
        SDKInitializer.initialize(this.getApplicationContext());//百度地图的初始化 // Initialization of baidu map
        setContentView(R.layout.activity_locate);

        initViewAndData();//初始化界面 Initialize interface
    }

    public void initViewAndData(){
        initToolbar();
        initButton();
        initMap();
        initLocationService();
    }
    public void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.loc_toolbar);
        toolbar.setTitle("Add location information");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.back);//此行必须放在setSupportActionBar后执行 //executes after setSupportActionBar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {//对后退键的监听。
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED, new Intent().putExtras(new Bundle()));//不返回任何信息
                LocateActivity.this.finish();
            }
        });
        TextView saveview=this.findViewById(R.id.save_location);
        saveview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBackLocation();//将定位信息发送给调用自己的activity send location info to activity
            }
        });
    }
    public void initButton(){
        backToLocationButton = (Button) findViewById(R.id.loc_locate_button);
        backToLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserLocation();
                moveToCenter(latestUserLocation);
            }
        });
    }
    public void initMap(){
        mMapView = (MapView) findViewById(R.id.loc_mapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(10.0f));// 设置地图显示比例 setup map rate

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) { }
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) { }
            @Override
            public void onMapStatusChange(MapStatus mapStatus) { }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                //记录当前屏幕中心对应的经纬度坐标点 // setup location
                latestMiddlePoint = mapStatus.target;
            }
        });
    }
    public void initLocationService(){
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(this);

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps // open GPS
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);

        //注册LocationListener监听器 regester listener
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        mLocationClient.start();
    }

    //建立位置监听，此函数持续触发，即每秒定位一次，定位时间间隔请看initLocation()函数 get monitor method, executes per second
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location != null && mMapView != null) {
                //LAT and LONG
                //double lat = location.getLatitude();
                //double lon = location.getLongitude();

                latestUserLocation = location;
                showUserLocation();
                if(FirstTimeLoc){
                    FirstTimeLoc = false;
                    moveToCenter(latestUserLocation);
                }
            }
        }
    }
    public void showUserLocation(){
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(latestUserLocation.getRadius())
                .direction(latestUserLocation.getRadius()).latitude(latestUserLocation.getLatitude())
                .longitude(latestUserLocation.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);
    }

    public void moveToCenter(BDLocation bdLocation){
        LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    public void sendBackLocation(){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("location", latestMiddlePoint);
        intent.putExtra("bundle", bundle);
        setResult(RESULT_OK, intent);
        LocateActivity.this.finish();

        //to get it from activity
        //Bundle bundle = getIntent().getParcelableExtra("bundle");
        //LatLng fromPosition = bundle.getParcelable("from_position");
        //LatLng toPosition = bundle.getParcelable("to_position");
    }
}
