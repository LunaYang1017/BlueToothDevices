package com.example.frombuttontoconnect;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btn_SearchBlueTooth;//声明组件
    private Button btn_MsgChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_SearchBlueTooth=(Button) findViewById(R.id.btn_SearchBlueTooth);//新版不用强制转化
        btn_MsgChat=(Button) findViewById(R.id.btn_MsgChat);
        setListener();

    }


    private void setListener(){
        OnClick onClick=new OnClick();

        btn_SearchBlueTooth.setOnClickListener(onClick);
        btn_MsgChat.setOnClickListener(onClick);
    }
    private class OnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {

            if(v.getId()==R.id.btn_SearchBlueTooth){
                Intent intent=new Intent(MainActivity.this, DeviceListActivity.class);
                startActivity(intent);//当点击后跳转到蓝牙查找界面
            }
            else if (v.getId()==R.id.btn_MsgChat){
                Intent intent=new Intent(MainActivity.this, MsgChatActivity.class);
                startActivity(intent);//当点击后跳转到蓝牙查找界面
            }
            
        }
    }
    
}

