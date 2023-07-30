package BlueToothPackage;

//数据交互与断开（服务端与客户端的聊天实现）
/*服务端和客户端都分别开启一个聊天线程，聊天线程的主要功能其实就一个——一直执行read()。
也就是处于监听状态，等待另一方write()*/
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Handler;

public class ConnectedThread extends Thread{
    private final BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private final Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler handler){
        //创建一个Socket，其实就是相当于创建一个聊天通道（BluetoothServerSocket和BluetoothSocket）
        //Handler用于同进程的线程间通信。用最简单的话描述： handler其实就是子线程运行并生成Message后，Looper获取message并传递给Handler。本质是“消息池”、“快递中转站”，实现不同线程间的信息交互。
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler = handler;
// 使用临时对象获取输入和输出流，因为成员流是最终的
        try {
            tmpIn = socket.getInputStream();//输入流
            tmpOut = socket.getOutputStream();//输出流
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }
    public void run(){
        byte[] buffer = new byte[1024];  // 用于流的缓冲存储
        int bytes; // 从read()返回bytes
        // 持续监听InputStream，直到出现异常
        while (true) {
            try {//代码区
                // 从InputStream读取数据
                bytes = mmInStream.read(buffer);
                // 将获得的bytes发送到UI层activity
                if( bytes >0) {
                    Message message = mHandler.obtainMessage(Constant.MSG_GOT_DATA, new String(buffer, 0, bytes, "utf-8"));
                    mHandler.sendMessage(message);
                }
                Log.d("GOTMSG", "message size" + bytes);
            } catch (IOException e) {//异常处理
                mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
                break;
            }
        }
    }

    //在main中调用此函数，将数据发送到远端设备中
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    //在main中调用此函数，断开连接
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }


}
