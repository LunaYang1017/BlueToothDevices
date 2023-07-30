package com.example.frombuttontoconnect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import BlueToothPackage.BluetoothController;
import DeviceListView.DeviceAdapter;
import DeviceListView.DeviceClass;

public class DeviceListActivity extends AppCompatActivity {


    //private Handler handler = new Handler();
    private ActivityResultLauncher<Intent> enableBluetooth;//打开蓝牙意图
    private ActivityResultLauncher<String> requestBluetoothConnect; //请求蓝牙连接权限意图
    private ActivityResultLauncher<String> requestBluetoothScan;    //请求蓝牙扫描权限意图

    private BluetoothLeScanner scanner;//扫描者
    boolean isScanning;//是否正在扫描
    private Button SearchDevicebtn;


   // private BluetoothController mbluetoothController=new BluetoothController();
    private Toast mToast;
    private BluetoothAdapter mBluetoothAdapter;//系统蓝牙适配器
    private BluetoothController mbluetoothController;
    private DeviceAdapter mfindDeviceAdapter,mbondDeviceAdapter;//接受找到的和已连接的设备适配器
    /*private List<BluetoothDevice> mfindDeviceList= new ArrayList<>();//所有未绑定
    private List<BluetoothDevice> mbondedDeviceList= new ArrayList<>();//所有已绑定*/

    private List<DeviceClass> mfindDeviceList= new ArrayList<>();//所有未绑定
    private List<DeviceClass> mbondedDeviceList= new ArrayList<>();//所有已绑定

    @Override
    protected void onCreate(Bundle savedInstanceState) {//该页面里面执行的程序
        registerIntent();//蓝牙连接权限，位置权限，版本兼容问题
        super.onCreate(savedInstanceState);//自动生成的
        setContentView(R.layout.activity_devicelist);//当前页面使用的layout
        SearchDevicebtn=findViewById(R.id.btn_SearchDevice);

        //registerBluetoothReceiver();//软件运行时弹窗申请打开蓝牙

        Init_listView();//初始化展示列表
        init_Filter();//初始化广播
        //show_bondDeviceList();
        //doDiscovery();

        SearchDevicebtn.setOnClickListener(view -> {
            //doDiscovery();
            //findDevice(view);
            assert (mBluetoothAdapter != null);
            mBluetoothAdapter.startDiscovery();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ((hasPermission(Manifest.permission.BLUETOOTH_SCAN))&&(mBluetoothAdapter != null)){
                    //扫描或者停止扫描
                   /* if (isScanning) stopScan();
                    else*/
                        startScan();
                }
            }

        });

    }

    private void Init_listView() {
        mbondDeviceAdapter=new DeviceAdapter(DeviceListActivity.this,R.layout.device_item,mbondedDeviceList);
        ListView listViewBonded=findViewById(R.id.listview1);
        listViewBonded.setAdapter(mbondDeviceAdapter);
        mbondDeviceAdapter.notifyDataSetChanged();

        mfindDeviceAdapter=new DeviceAdapter(DeviceListActivity.this,R.layout.device_item,mfindDeviceList);
        ListView listViewfind=findViewById(R.id.listview2);
        listViewfind.setAdapter(mbondDeviceAdapter);
       // mfindDeviceList.clear();
        mfindDeviceAdapter.notifyDataSetChanged();
    }

    private void registerIntent(){//oncreat以前的函数
        //开启位置共享，如果版本在安卓12以下，需要开启位置权限才能打开蓝牙搜索
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }
        }

        //打开蓝牙意图
        enableBluetooth=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
            if (result.getResultCode()==Activity.RESULT_OK){
                if(isOpenBluetooth()){
                    BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
                    mBluetoothAdapter = manager.getAdapter();

                    showToast("蓝牙已打开");
                }
                else {
                    showToast("蓝牙未打开");
                }
            }
        });
        //请求BLUETOOTH_CONNECT权限意图
        requestBluetoothConnect = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                enableBluetooth.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            } else {
                showToast("Android12中未获取此权限，无法打开蓝牙。");
            }
        });
       // mfindDeviceList.clear();//清空当前列表


                    //扫描者获取系统蓝牙适配器
        if (isOpenBluetooth()) {
                        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
                        mBluetoothAdapter = manager.getAdapter();
                        scanner = mBluetoothAdapter.getBluetoothLeScanner();//以上三行获取系统蓝牙适配器，扫描者
                        showToast("蓝牙已打开");
                        return;
                    }

                    //是Android12
        if (isAndroid12()) {
                        //检查是否有BLUETOOTH_CONNECT权限
                        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                            enableBluetooth.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));

                        }
                        else {
                            //请求权限
                            requestBluetoothConnect.launch(Manifest.permission.BLUETOOTH_CONNECT);
                        }
                    }
        else enableBluetooth.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        //请求BLUETOOTH_SCAN权限意图
      requestBluetoothScan = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                //进行扫描
                startScan();
            } else {
                showToast("Android12中未获取此权限，则无法扫描蓝牙。");
            }
        });

    }

    //扫描结果回调
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
           showToast(device.getAddress());
           mfindDeviceAdapter.notifyDataSetChanged();
        }
    };
    private void startScan() {
        if (!isScanning) {
            scanner.startScan(scanCallback);
            isScanning = true;
            showToast("start扫描");
        }
    }

    private void stopScan() {
        if (isScanning) {
            scanner.stopScan(scanCallback);
            isScanning = false;
            showToast("stop扫描");
        }
    }








    /*
* 广播版扫描方式*/
//开启广播
private void init_Filter(){
    IntentFilter filter = new IntentFilter();//创建了一个广播频道，要收听的是下面这些

    //频道1.开始查找
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
    //结束查找
   filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    //查找设备
    filter.addAction(BluetoothDevice.ACTION_FOUND);
    //设备扫描模式改变
    filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
    //绑定状态
    filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);


    registerReceiver(receiver, filter);
    this.registerReceiver(receiver,filter);//注册广播

    showToast("开启广播完毕");
}

    //广播内容
    BroadcastReceiver receiver = new BroadcastReceiver() {//1.创建了一个广播接受者，用来接受状态
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                //setSupportProgressBarIndeterminateVisibility(true);
                change_Button_Text("搜索中...","DISABLE");
                //mfindDeviceList.clear();
                mfindDeviceAdapter.notifyDataSetChanged();
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                setProgressBarIndeterminateVisibility(false);
                change_Button_Text("搜索设备","ENABLE");

            }
            //查找设备
            else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                change_Button_Text("找到一个设备...","DISABLE");
                //查找到一个设备就添加到列表类中
                mfindDeviceList.add(new DeviceClass(device.getName(),device.getAddress()));
                mfindDeviceAdapter.notifyDataSetChanged();

            }
            else if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)){
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
                if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    setProgressBarIndeterminateVisibility(true);
                }
                else {
                    setProgressBarIndeterminateVisibility(false);
                }
            }
            else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice remoteDecive = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(remoteDecive == null){
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                if(status == BluetoothDevice.BOND_BONDED) {
                    showToast("已绑定" + remoteDecive.getName());
                } else if(status == BluetoothDevice.BOND_BONDING) {
                    showToast("正在绑定" + remoteDecive.getName());
                } else if(status == BluetoothDevice.BOND_NONE) {
                    showToast("未绑定" + remoteDecive.getName());
                }
            }
        }
    };


    @SuppressLint("MissingPermission")
    private void show_bondDeviceList(){
        mbondedDeviceList.clear();
        @SuppressLint("MissingPermission") List<BluetoothDevice> bondDevices = new ArrayList<>(mBluetoothAdapter.getBondedDevices());//mbluetoothController.getBondedDeviceList();//查找已绑定设备
        for(int i=0;i<bondDevices.size();i++){
            mbondedDeviceList.add(new DeviceClass(bondDevices.get(i).getName(),bondDevices.get(i).getAddress()));
        }
        mbondDeviceAdapter.notifyDataSetChanged();
    }


/*
* */










    //点击按键搜索后按键的变化
    private void change_Button_Text(String text,String state){
        Button button = findViewById(R.id.btn_SearchDevice);
        if("ENABLE".equals(state)){
            button.setEnabled(true);
            button.getBackground().setAlpha(255); //0~255 之间任意调整
            button.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
        else {
            button.setEnabled(false);
            button.getBackground().setAlpha(150); //0~255 之间任意调整
            button.setTextColor(ContextCompat.getColor(this, R.color.search_device_btn_bg));
        }
        button.setText(text);
    }




/*
标准的显示和申请权限函数
 */
//检查权限是否被授予，23及以上的版本可以用这个函数
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermission(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }


    private void showToast(String text){//原本使用的toast
        if(mToast == null){
            mToast = Toast.makeText(this, text,Toast.LENGTH_SHORT);
            mToast.show();
        }
        else {
            mToast.setText(text);
            mToast.show();
        }
    }

    /*
    版本适配函数
    */

    private boolean isOpenBluetooth() {//在Android12.0之前打开蓝牙的之前需要先判断蓝牙是否打开
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        if (adapter==null){
            return false;
        }
        return adapter.isEnabled();
    }

    private boolean isAndroid12() {//判断当前是否为Android12及以上版本
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }


    //点击设备后执行的函数
   /* private final AdapterView.OnItemClickListener toMainActivity = (adapterView, view, i, l) -> {
        //Main2Activity是第二个界面的，运行会出错，为了展现目前的效果，对这里先改为注释
        Intent intent = new Intent(DeviceListActivity.this,MainActivity.class);
        startActivity(intent);

    };*/
}





