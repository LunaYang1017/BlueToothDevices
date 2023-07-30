package BlueToothPackage;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.frombuttontoconnect.MsgChatActivity;
import BlueToothPackage.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



public class ChatService {
    //本应用的主Activity组件名称
    private static final String NAME = "BluetoothChat";
    // UUID：通用唯一识别码,是一个128位长的数字，一般用十六进制表示
    //算法的核心思想是结合机器的网卡、当地时间、一个随机数来生成
    //在创建蓝牙连接
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private final BluetoothAdapter adapter;
    private final Handler mHandler;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private int state;
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    //构造方法，接收UI主线程传递的对象
    public ChatService(Context context, Handler handler) {
        //构造方法完成蓝牙对象的创建
        adapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
        mHandler = handler;
    }

    private synchronized void setState(int state) {
        state = state;
       // mHandler.obtainMessage(MsgChatActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized void start() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    //取消 CONNECTING 和 CONNECTED 状态下的相关线程，然后运行新的 connectThread 线程
    public synchronized void connect(BluetoothDevice device) {
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    /*
        开启一个 ConnectedThread 来管理对应的当前连接。之前先取消任意现存的 connectThread 、
        connectedThread 、 acceptThread 线程，然后开启新 connectedThread ，传入当前刚刚接受的
        socket 连接。最后通过 Handler来通知UI连接
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        //Message msg = mHandler.obtainMessage(MsgChatActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
       // bundle.putString(MsgChatActivity.DEVICE_NAME, device.getName());
       // msg.setData(bundle);
      //  mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    // 停止所有相关线程，设当前状态为 NONE
    public synchronized void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(STATE_NONE);
    }

    // 在 STATE_CONNECTED 状态下，调用 connectedThread 里的 write 方法，写入 byte
    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (state != STATE_CONNECTED)
                return;
            r = connectedThread;
        }
        r.write(out);
    }

    // 连接失败的时候处理，通知 ui ，并设为 STATE_LISTEN 状态
    private void connectionFailed() {
        setState(STATE_LISTEN);
        //Message msg = mHandler.obtainMessage(MsgChatActivity.MESSAGE_TOAST);
       // Bundle bundle = new Bundle();
       // bundle.putString(MsgChatActivity.TOAST, "链接不到设备");
      //  msg.setData(bundle);
      //  mHandler.sendMessage(msg);
    }

    // 当连接失去的时候，设为 STATE_LISTEN 状态并通知 ui
    private void connectionLost() {
        setState(STATE_LISTEN);
       // Message msg = mHandler.obtainMessage(MsgChatActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
       // bundle.putString(MsgChatActivity.TOAST, "设备链接中断");
       // msg.setData(bundle);
      //  mHandler.sendMessage(msg);
    }

    // 创建监听线程，准备接受新连接。使用阻塞方式，调用 BluetoothServerSocket.accept()
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                //使用射频端口（RF comm）监听
                tmp = adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        @Override
        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;
            while (state != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                if (socket != null) {
                    synchronized (ChatService.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        连接线程，专门用来对外发出连接对方蓝牙的请求和处理流程。
        构造函数里通过 BluetoothDevice.createRfcommSocketToServiceRecord() ，
        从待连接的 device 产生 BluetoothSocket. 然后在 run 方法中 connect ，
        成功后调用 BluetoothChatSevice 的 connected() 方法。定义 cancel() 在关闭线程时能够关闭相关socket 。
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            setName("ConnectThread");
            adapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    e.printStackTrace();
                }
                ChatService.this.start();
                return;
            }
            synchronized (ChatService.this) {
                connectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        双方蓝牙连接后一直运行的线程；构造函数中设置输入输出流。
        run()方法中使用阻塞模式的 InputStream.read()循环读取输入流，然后发送到 UI 线程中更新聊天消息。
        本线程也提供了 write() 将聊天消息写入输出流传输至对方，传输成功后回写入 UI 线程。最后使用cancel()关闭连接的 socket
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                  //  mHandler.obtainMessage(MsgChatActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
               // mHandler.obtainMessage(MsgChatActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
