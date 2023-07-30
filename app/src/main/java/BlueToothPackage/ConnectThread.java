package BlueToothPackage;

//发起聊天请求并建立通信通道（客户端）
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;
import android.os.Handler;

public class ConnectThread extends Thread{
    static final UUID MY_UUID = UUID.fromString(Constant.CONNECTTION_UUID);
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private ConnectedThread mConnectedThread;
    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter, Handler handler){
        // 将一个临时对象分配给mmSocket，因为mmSocket是最终的
        BluetoothSocket tmp = null;
        mmDevice = device;
        mBluetoothAdapter = adapter;
        mHandler = handler;
        try {
            // MY_UUID是应用程序的UUID，客户端代码使用相同的UUID
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }
    //由于请求过程处于阻塞状态，所以整个请求过程得用线程
    public void run() {
        // 关闭蓝牙的搜索功能，避免请求过程出现数据错误
        mBluetoothAdapter.cancelDiscovery();

        try {
            // 通过socket连接设备，阻塞运行直到成功或抛出异常时
            mmSocket.connect();
        } catch (Exception connectException) {
            mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, connectException));
            // 如果无法连接则关闭socket并退出
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
        // 在单独的线程中完成管理连接的工作
        manageConnectedSocket(mmSocket);
    }

    //开门成功后就开始下一个任务
    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        //发送连接成功提示文字
        mHandler.sendEmptyMessage(Constant.MSG_CONNECTED_TO_SERVER);
        //创建连接后的处理线程，即通信线程
        mConnectedThread = new ConnectedThread(mmSocket,mHandler);
        mConnectedThread.start();
    }

    //取消当前连接并关闭socket
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    //发送数据
    public void sendData(byte[] data) {
        if( mConnectedThread!=null){
            mConnectedThread.write(data);
        }
    }
}

