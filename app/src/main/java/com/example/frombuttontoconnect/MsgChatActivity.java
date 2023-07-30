package com.example.frombuttontoconnect;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import BlueToothPackage.AcceptThread;
import BlueToothPackage.ChatService;
import BlueToothPackage.ConnectThread;
import BlueToothPackage.Constant;
import MsgView.Msg;
import MsgView.MsgAdapter;

public class MsgChatActivity extends AppCompatActivity {

    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private List<Msg> msgList=new ArrayList<>();//消息信息列表
    //private ChatService chatService=null;
   // private StringBuffer outStringBuffer;//字符缓冲区
    private Toast mToast;
    private AcceptThread mAcceptThread;
    private BluetoothManager mblueToothController;
    private ConnectThread mConnectThread;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init_chat();

        /*//创建accept线程
        mAcceptThread = new AcceptThread(mblueToothController.getAdapter(), mUIHandler);
        mAcceptThread.start();
        //创建connect线程
        mConnectThread = new ConnectThread(device,mblueToothController.getAdapter(),mUIHandler);
        mConnectThread.start();*/


        send.setOnClickListener(view -> {
        String content=inputText.getText().toString();
        if (!"".equals(content)){
            Msg msg=new Msg(content,Msg.TYPE_SENT);
            msgList.add(msg);
            adapter.notifyItemInserted(msgList.size()-1);
            msgRecyclerView.scrollToPosition(msgList.size()-1);
            inputText.setText("");
            //发送消息
            /*if (chatService.getState()==ChatService.STATE_CONNECTED){
            byte[]send=content.getBytes();
            chatService.write(send);
            outStringBuffer.setLength(0);
            inputText.setText(outStringBuffer);}
            if (chatService.getState()!=ChatService.STATE_CONNECTED) showToast("蓝牙未连接，没能发送");*/
        }

    });

    }

    private void init_chat(){
        inputText= findViewById(R.id.input_text);
        send=findViewById(R.id.send);
        msgRecyclerView=findViewById(R.id.msg_recycler_view);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter=new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        Show_msgRecyclerView();

        /*//创建服务对象,不完全看懂
       // chatService=new ChatService(this,mHandler);
        outStringBuffer=new StringBuffer("");*/

    }
    public void Show_msgRecyclerView(){
        Msg msg1 = new Msg("通道已连接，我们可以开始聊天了",Msg.TYPE_RECEIVED);
        msgList.add(msg1);
        Msg msg2 = new Msg("你好，收得到消息不？",Msg.TYPE_SENT);
        msgList.add(msg2);
        Msg msg3 = new Msg("你好，我收到了",Msg.TYPE_RECEIVED);
        msgList.add(msg3);
        adapter.notifyDataSetChanged();
    }

    //明确快递中转站（Handler）收到what类型后，向UI界面传递什么更新内容
    private class MyHandler extends Handler {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case Constant.MSG_GOT_DATA:		//这些代表什么，文章前面的定义有说明
                    showToast("data:" + String.valueOf(message.obj));
                    break;
                case Constant.MSG_ERROR:
                    showToast("error:" + String.valueOf(message.obj));
                    Log.e("提示","error:" + String.valueOf(message.obj));
                    break;
                case Constant.MSG_CONNECTED_TO_SERVER:
                    showToast("连接到服务端");
                    break;
                case Constant.MSG_GOT_A_CLINET:
                    showToast("找到客户端");
                    break;
            }
        }
    }


    private void showToast(String text){//提示弹窗
        if(mToast == null){
            mToast = Toast.makeText(this, text,Toast.LENGTH_SHORT);
            mToast.show();
        }
        else {
            mToast.setText(text);
            mToast.show();
        }
    }

}
