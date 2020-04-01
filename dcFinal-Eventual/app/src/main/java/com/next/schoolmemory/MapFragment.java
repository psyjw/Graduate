package com.next.schoolmemory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.next.schoolmemory.Bean.Means;
import com.next.schoolmemory.Bean.NoteBean;
import com.next.schoolmemory.Presenter.Presenter_map;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions // 标记，表明这个Fragment需要申请定位权限
public class MapFragment extends Fragment {
    private Presenter_map presenter_map;
    private Toolbar toolbar;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private LocationClient mLocationClient=null;//初始化LocationClient定位类

    //防止每次定位都重新设置中心点和marker
    private boolean FirstTimeLoc = true;
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口，原有BDLocationListener接口
    private BDLocationListener myListener = new MyLocationListener();

    private Button backToLocationButton;//定位按钮，用户点击后回到以用户当前定位为中心的画面
    private BDLocation latestUserLocation;//用于实时保存用户的定位

    private LatLng latestMiddlePoint;//用于记录每次用户切换fragment前地图最后的位置状态，以便下次进入Fragment时仍回到这个位置

    List<Marker> markerlist;
    List<NoteBean> loc_noteBean_list;

    InfoWindow mInfoWindow;

    private ImageView popImageView;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext。注意该方法要在setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext());
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        // 定位SDK默认输出GCJ02坐标，地图SDK默认输出BD09ll坐标。
        //SDKInitializer.setCoordType(CoordType.BD09LL);

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        //把变量和xml文件中的部件对应起来
        toolbar = (Toolbar) view.findViewById (R.id.toolbar);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        backToLocationButton = (Button) view.findViewById(R.id.locate_button);
        return view;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        presenter_map = new Presenter_map(this);
        initToolBar();//初始化toolbar
        initMap(); //初始化地图
        initLocationService(); //定位服务的初始化，包含gps监听
        initListener();//建立监听，包含定位按钮的监听，地图状态变化的监听等。
        //setPoint_test();
        showPoints();//显示用户记录中标记的所有位置
    }

    public void initToolBar(){
        toolbar.setTitle("Location");
        AppCompatActivity myActivity = (AppCompatActivity) getActivity();
        myActivity.setSupportActionBar(toolbar);
    }

    public void initMap(){
        // 获得到地图
        mBaiduMap = mMapView.getMap();
        // 设置地图显示比例
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(10.0f));
    }

    public void initListener(){
        backToLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //将画面移动到以用户定位为中心
                showUserLocation(); //显示用户定位
                moveToCenter(latestUserLocation); //移动画面，使用户定位到画面中心
            }
        });
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) { }
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) { }
            @Override
            public void onMapStatusChange(MapStatus mapStatus) { }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                //记录用户移动地图最新的位置，使得下次回到这个fragment能从上次的位置开始
                latestMiddlePoint = mapStatus.target;
            }
        });
    }

    public void initLocationService(){
        //开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //定位初始化
        mLocationClient = new LocationClient(getActivity());
        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型                                                                                                        //确定是这个类型吗？两个类型什么区别？？？？？？？？？？？？？？
        option.setScanSpan(1000);
        //设置locationClientOption
        mLocationClient.setLocOption(option);
        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //initLocation();//进行相关参数的设定
        //开启地图定位图层
        mLocationClient.start();
    }

    /****/
    /**
     * 申请权限成功时
     */
    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    void ApplySuccess() {
        initMap();
    }
    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        //载入用户上次的地图位置
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latestMiddlePoint).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }
    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {//这个只有在activity销毁才会调用，因为我没有设置销毁fragment的函数
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        // 退出时销毁定位
        //mLocationClient.unRegisterLocationListener(myListener);
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;

    }

    public Context getListActivityConent() {
        return getActivity();
    }

    //建立位置监听，此函数持续触发，即每一小段时间定位一次，定位时间间隔请看initLocation()函数
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location != null && mMapView != null) {
                //相关信息的收集
                /*********
                location.getTime();    //获取定位时间
                location.getLocationID();    //获取定位唯一ID，v7.2版本新增，用于排查定位问题
                location.getLocType();    //获取定位类型
                location.getLatitude();    //获取纬度信息
                location.getLongitude();    //获取经度信息
                location.getRadius();    //获取定位精准度
                location.getAddrStr();    //获取地址信息
                location.getCountry();    //获取国家信息
                location.getCountryCode();    //获取国家码
                location.getCity();    //获取城市信息
                location.getCityCode();    //获取城市码
                location.getDistrict();    //获取区县信息
                location.getStreet();    //获取街道信息
                location.getStreetNumber();    //获取街道码
                location.getLocationDescribe();    //获取当前位置描述信息
                location.getPoiList();    //获取当前位置周边POI信息
                location.getBuildingID();    //室内精准定位下，获取楼宇ID
                location.getBuildingName();    //室内精准定位下，获取楼宇名称
                location.getFloor();    //室内精准定位下，获取当前位置所处的楼层信息
                 *************/
                //经纬度
                //double lat = location.getLatitude();
                //double lon = location.getLongitude();

                latestUserLocation = location;//每次接收用户的最新位置都保存到私有变量中，供定位按钮使用

                //判断是否是第一次定位（也就是第一次打开MapFragment），如果是，则显示用户位置，并把画面移动到以用户为中心。如果不是，则仅显示用户位置。
                showUserLocation();
                if(FirstTimeLoc){
                    FirstTimeLoc = false;
                    moveToCenter(latestUserLocation);
                }
            }
        }
    }

    /**
     * 配置定位参数
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(5000);
        //可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        //可选，默认false,设置是否使用gps
        option.setOpenGps(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setLocationNotify(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
    }


    //显示用户当前定位到画面上，参数即为定位
    public void showUserLocation(){
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(latestUserLocation.getRadius())
                .direction(latestUserLocation.getRadius()).latitude(latestUserLocation.getLatitude())
                .longitude(latestUserLocation.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);
    }

    public void moveToCenter(BDLocation bdLocation){ //将地图画面移动到，以BDlocation为中心，固定高度（比例）
        LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    public void setPoint_test(){//在地图上显示一个标记，没有触发（本方法用于测试）
        LatLng point = new LatLng(22.536 , 113.949);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        OverlayOptions overlayOptions = new MarkerOptions().position(point).icon(bitmapDescriptor);
        Marker marker = (Marker)mBaiduMap.addOverlay(overlayOptions);
    }

    //从数据库中读取所有的定位信息并显示
    public void showPoints(){
        loc_noteBean_list = presenter_map.getLocPoints();
        markerlist = new ArrayList<>();
        for(int i=0;i<loc_noteBean_list.size();i++){
            NoteBean bb = loc_noteBean_list.get(i);
            LatLng point = new LatLng(bb.getLocation_latitude() , bb.getLocation_longtitude());//获取经纬度，放在定位中
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);//图标
            OverlayOptions overlayOptions = new MarkerOptions().position(point).icon(bitmapDescriptor);
            Marker marker = (Marker)mBaiduMap.addOverlay(overlayOptions);//添加覆盖物
            markerlist.add(marker);
        }
        markerAddListener();//添加监听
    }

    public void markerAddListener(){
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int i;
                //寻找用户点击的marker对应在list中的位置
                for(i=0;i<markerlist.size();i++){
                    if(marker == markerlist.get(i))
                        break;
                }
                if(i == markerlist.size())
                    return false;
                else{
                    mapStartNoteInfoActivity(loc_noteBean_list.get(i));
                    return true;
                }
            }
        });
    }
    //跳转到详情页
    public void mapStartNoteInfoActivity(NoteBean noteBean){
        Intent intent=new Intent(getActivity(), NoteinfoActivity.class);
        Bundle bundle=new Bundle();
        bundle.putSerializable("noteinfo", Means.changefromNotebean(noteBean));
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
