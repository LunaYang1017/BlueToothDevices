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
}
