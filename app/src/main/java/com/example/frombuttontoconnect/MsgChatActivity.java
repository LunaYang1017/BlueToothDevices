package com.example.frombuttontoconnect;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import MsgView.Msg;
import MsgView.MsgAdapter;

public class MsgChatActivity extends AppCompatActivity {
    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private List<Msg> msgList=new ArrayList<>();//消息信息列表

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        inputText= findViewById(R.id.input_text);
        send=findViewById(R.id.send);
        msgRecyclerView=findViewById(R.id.msg_recycler_view);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter=new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        Show_msgRecyclerView();
    send.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        String content=inputText.getText().toString();
        if (!"".equals(content)){
            Msg msg=new Msg(content,Msg.TYPE_SENT);
            msgList.add(msg);
            adapter.notifyItemInserted(msgList.size()-1);
            msgRecyclerView.scrollToPosition(msgList.size()-1);
            inputText.setText("");
        }

    }
});

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


}
