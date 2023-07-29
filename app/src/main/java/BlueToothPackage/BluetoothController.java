package BlueToothPackage;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class BluetoothController {
    private BluetoothAdapter mAdapter;

    public BluetoothController() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    //打开蓝牙
    public void turnOnBlueTooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    //查找蓝牙
    public void findDevice() {
        assert (mAdapter != null);
        mAdapter.startDiscovery();
    }

    public List<BluetoothDevice> getBondedDeviceList(){
        return new ArrayList<>(mAdapter.getBondedDevices());
    }

    public List<BluetoothDevice> getFindDeviceList(){
        return null;
    }
}


/*
* 你可以使用Android Studio来进行蓝牙设备的扫描。下面是一个简单的示例代码，用于在Android应用程序中扫描并显示附近的蓝牙设备：

首先，在你的AndroidManifest.xml文件中添加以下权限：

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

然后，在你的Activity中，你可以使用BluetoothAdapter来执行蓝牙扫描操作。以下是一个简单的示例：

```java
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> bluetoothDevices;
    private boolean isScanning = false;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothDevices = new ArrayList<>();

        // 检查蓝牙权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_COARSE_LOCATION);
        }

        // 初始化BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // 设备不支持蓝牙
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // 检查蓝牙是否已启用
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                startScan();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startScan();
            } else {
                Toast.makeText(this, "蓝牙未启用", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予
            } else {
                Toast.makeText(this, "需要位置权限来扫描蓝牙设备", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startScan() {
        if (!isScanning) {
            bluetoothDevices.clear();
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(scanCallback);
            isScanning = true;

            // 设置扫描时长
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, 10000); // 10秒后停止扫描
        }
    }

    private void stopScan() {
        if (isScanning) {
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.stopScan(scanCallback);
            isScanning = false;

            // 显示扫描到的设备
            for (BluetoothDevice device : bluetoothDevices) {
                Toast.makeText(this, device.getName() + ": " + device.getAddress(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (!bluetoothDevices.contains(device)) {
                bluetoothDevices.add(device);
            }
        }
    };
}
```

上面的代码首先检查应用程序是否具有蓝牙和位置权限。然后，它使用BluetoothAdapter来开始蓝牙扫描，并在扫描到设备时将其添加到`bluetoothDevices`列表中。在10秒后，扫描将停止，并显示扫描到的设备的名称和地址。

请确保在使用蓝牙功能之前在AndroidManifest.xml文件中添加了蓝牙权限。此外，还需要在Android设备上打开蓝牙功能，以便应用程序可以进行蓝牙扫描。

希望这可以帮助到你！*/