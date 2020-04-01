package com.next.schoolmemory;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;//BottomNavigationView是support library里面的类
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.next.schoolmemory.Adapter.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import pub.devrel.easypermissions.EasyPermissions;

@RuntimePermissions//表示该活动需要权限申请
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private BottomNavigationView bottomNavigationView;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;//用户左右滑动界面的视图切换工具 //swipe tool
    private MenuItem selectedItem;

    private FileFragment fileFragment = new FileFragment();
    private SearchFragment searchFragment = new SearchFragment();
    private MapFragment mapFragment = new MapFragment();
    private SquareFragment squareFragment = new SquareFragment();

    private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//此行用于去除默认的顶部栏 remove top bar
        setContentView(R.layout.activity_main);

        initViewAndData();//初始化界面 // Initialize
        getPermission();
        MainActivityPermissionsDispatcher.ApplySuccessWithCheck(this);//检查权限 // check out permission
    }

    public void initViewAndData(){
        initBottomNavigationView();//初始化底部导航栏
        initViewPager();//初始化ViewPager，包含ViewPagerAdaptor
    }

    public void initBottomNavigationView(){
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);//底部导航栏，通过布局xml文件找到对应物
        //设置底部导航栏的监听 // setup listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectedItem = item;
                switch (item.getItemId()) {
                    case R.id.navigation_files:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.navigation_square:
                        viewPager.setCurrentItem(1);
                        return true;
                    case R.id.navigation_search:
                        viewPager.setCurrentItem(2);
                        return true;
                    case R.id.navigation_map:
                        viewPager.setCurrentItem(3);
                        return true;
                }
                return false;
            }
        });
    }
    public void initViewPager(){
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override //用户手指滑动过程中持续触发 // finger gesture trigger
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override //根据用户手指动作（接触，滑动，抬起）触发 // finger gesture trigger2
            public void onPageScrollStateChanged(int state) { }

            @Override//左右滑动时底部导航栏的变化 // tab bar changes
            public void onPageSelected(int position) {
                selectedItem = bottomNavigationView.getMenu().getItem(position);
                selectedItem.setChecked(true);
            }
        });

        //将fragment加入到 viewPagerAdaptor中
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(fileFragment);
        fragmentList.add(squareFragment);
        fragmentList.add(searchFragment);
        fragmentList.add(mapFragment);
        viewPagerAdapter.setList(fragmentList);
    }

    //获取权限//get permission
    private void getPermission() {
        if (EasyPermissions.hasPermissions(this, permissions)) {
            //已经打开权限 got permission
            //Toast.makeText(this, "已经申请相关权限", Toast.LENGTH_SHORT).show();
        } else {
            //没有打开相关权限、申请权限 // ask for permission
            EasyPermissions.requestPermissions(this, "Please turn on the Photo permission", 1, permissions);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Get permission", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Please turn on the permission", Toast.LENGTH_SHORT).show();
    }
    /**********
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {//和权限相关，但是不知道哪里有用到，还是必须要写？？
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //框架要求必须这么写
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
     ***********/

    //以下是获取定位权限相关 get location info permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        //上面这个类只有在Build-Make Project后才会出现
    }

    /**
     * 申请权限成功时 If successfully get permission
     */
    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    void ApplySuccess() {
    }

    /**
     * 申请权限告诉用户原因时 Do not remind users if denied
     * @param request
     */
    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    void showRationaleForMap(PermissionRequest request) {
        showRationaleDialog("Please allow us to access your location to use this function ", request);
    }

    /**
     * 申请权限被拒绝时 if denied
     *
     */
    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
    void onMapDenied() {
        Toast.makeText(this,"This function is unavailable",Toast.LENGTH_LONG).show();
    }

    /**
     * 申请权限被拒绝并勾选不再提醒时 Do not remind users if denied
     */
    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION)
    void onMapNeverAskAgain() {
        AskForPermission();
    }

    //告知用户具体需要权限的原因
    private void showRationaleDialog(String messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();//请求权限
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    /**
     * 被拒绝并且不再提醒,提示用户去设置界面重新打开权限 Do not remind users if denied, send permission reminder
     */
    private void AskForPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please turn on location permission in settings\n");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        builder.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName())); // 根据包名打开对应的设置界面
                startActivity(intent);
            }
        });
        builder.create().show();
    }
}
