package com.example.frombuttontoconnect;

import android.Manifest;
import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import BlueToothPackage.BluetoothController;
import DeviceListView.DeviceAdapter;
import DeviceListView.DeviceClass;

public class DeviceListActivity extends AppCompatActivity {

    private Button btn_SearchDevice;
    public DeviceAdapter mAdapter1,mAdapter2;//改成公共的了，原本是私有
    private  List<DeviceClass> mbondDeviceList=new ArrayList<>();//所有已绑定
    private  List<DeviceClass> mfindDeviceList=new ArrayList<>();//所有未绑定
    private BluetoothController mbluetoothController=new BluetoothController();
    private Toast mToast;



    @Override
    protected void onCreate(Bundle savedInstanceState) {//该页面里面执行的程序
        super.onCreate(savedInstanceState);//自动生成的

        setContentView(R.layout.activity_devicelist);//当前页面使用的layout
        btn_SearchDevice=findViewById(R.id.btn_SearchDevice);//声明按钮
        registerBluetoothReceiver();//软件运行时直接申请打开蓝牙，系统弹窗

       /* Init_Bluetooth();//开启蓝牙相关权限
        init_Filter();//初始化广播并打开
        Init_listView();//初始化设备列表
        show_bondDeviceList();//搜索展示已经绑定的蓝牙设备
        //Show_listview();
        //DeviceClass bondDevice;*/
    }


    //蓝牙初始化
    private void Init_Bluetooth(){
        //开启位置共享，抄的，看不懂
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            }
        }

        //mbluetoothController.enableVisibily(this);//让其他蓝牙看得到我
        mbluetoothController.turnOnBlueTooth(this,0);//打开蓝牙
    }

    //初始化列表，适配器的加载
    public void Init_listView(){
        mAdapter1 = new DeviceAdapter(DeviceListActivity.this, R.layout.device_item, mbondDeviceList);
        ListView listView1 = (ListView)findViewById(R.id.listview1);
        listView1.setAdapter(mAdapter1);
        mAdapter1.notifyDataSetChanged();
        //listView1.setOnItemClickListener(toMainActivity2);//设备点击事件，点击设备名称后执行toMainActivity2
        mAdapter2 = new DeviceAdapter(DeviceListActivity.this, R.layout.device_item, mfindDeviceList);
        ListView listView2 = (ListView)findViewById(R.id.listview2);
        listView2.setAdapter(mAdapter2);
        mAdapter2.notifyDataSetChanged();
    }

    //开启广播
    private void registerBluetoothReceiver(){
        IntentFilter filter = new IntentFilter();
        //开始查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //查找设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //设备扫描模式改变
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //绑定状态
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver,filter);
    }
    //广播内容
    private BroadcastReceiver receiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                mfindDeviceList.clear();
                mAdapter2.notifyDataSetChanged();
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                setProgressBarIndeterminateVisibility(false);
            }

            else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //查找到一个设备就添加到列表类中
                mfindDeviceList.add(new DeviceClass(device.getName(),device.getAddress()) );
                //mfindDeviceList.add(device);
                mAdapter2.notifyDataSetChanged();//刷新列表适配器，将内容显示出来
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

    //点击开始查找蓝牙设备
    public View findDevice(View view){
        mbluetoothController.findDevice();
        return view;
    }
    //查找已绑定的蓝牙设备
    private void show_bondDeviceList(){
        mbondDeviceList.clear();
        List<BluetoothDevice> bondDevices = mbluetoothController.getBondedDeviceList();//查找已绑定设备
        for(int i=0;i<bondDevices.size();i++){
            mbondDeviceList.add(new DeviceClass(bondDevices.get(i).getName(),bondDevices.get(i).getAddress()));
        }
        mAdapter1.notifyDataSetChanged();
    }
    //点击设备后执行的函数
    private AdapterView.OnItemClickListener toMainActivity = (adapterView, view, i, l) -> {
        Intent intent = new Intent(DeviceListActivity.this,MainActivity.class);
        startActivity(intent);
    };
    //设置toast的标准格式
    private void showToast(String text){
        if(mToast == null){
            mToast = Toast.makeText(this, text,Toast.LENGTH_SHORT);
            mToast.show();
        }
        else {
            mToast.setText(text);
            mToast.show();
        }
    }

    //点击设备后执行的函数
   /* private final AdapterView.OnItemClickListener toMainActivity = (adapterView, view, i, l) -> {
        //Main2Activity是第二个界面的，运行会出错，为了展现目前的效果，对这里先改为注释
        Intent intent = new Intent(DeviceListActivity.this,MainActivity.class);
        startActivity(intent);

    };*/
}





