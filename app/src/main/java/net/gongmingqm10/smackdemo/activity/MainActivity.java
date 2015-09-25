package net.gongmingqm10.smackdemo.activity;

import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import net.gongmingqm10.smackdemo.R;
import net.gongmingqm10.smackdemo.adapter.ChatListAdapter;
import net.gongmingqm10.smackdemo.model.ChatMessage;
import net.gongmingqm10.smackdemo.xmpp.XMPPManager;
import net.gongmingqm10.smackdemo.xmpp.XMPPState;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.messageEdit)
    EditText messageEdit;
    @InjectView(R.id.messageList)
    ListView messageList;
    @InjectView(R.id.sendBtn)
    Button sendBtn;

    private ChatManager chatManager;
    private Chat chat;

    private ChatListAdapter adapter;

    private String username;
    private final int MESSAGE_FLAG = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = getIntent().getStringExtra("friend");
        ButterKnife.inject(this);
        initState();
        sendBtn.setOnClickListener(new SendMessageListener());
        adapter = new ChatListAdapter(this);
        messageList.setAdapter(adapter);
    }

    private class SendMessageListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String message = messageEdit.getText().toString().trim();
            if (TextUtils.isEmpty(message) || chatManager == null) return;
            try {
                chat.sendMessage(message);
            } catch (SmackException.NotConnectedException e) {
                Log.i("XMPPManager", " sendMessage  NotConnectedException " + message);
                e.printStackTrace();
            }
            ChatMessage chatMessage = new ChatMessage().setLocal(true).setContent(message);
            addMessageToList(chatMessage);
            messageEdit.setText("");
        }
    }

    private void addMessageToList(ChatMessage message) {
        adapter.addMessage(message);
    }

    private void initState() {
        chatManager = ChatManager.getInstanceFor(XMPPManager.getInstance().connection);
        chatManager.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                Log.i("XMPPManager", chat.getThreadID() + "chatCreated " + username + " " +createdLocally, new Exception());
                if (createdLocally) {
                   // Chat created by myself
                } else {
                   // Chat created by others
                    chat.addMessageListener(chatMessageListener);
                }

            }
        });

        if (!TextUtils.isEmpty(username)) {
            chat = chatManager.createChat(username + "@" + XMPPManager.serverName, chatMessageListener);
        }

    }

    private ChatMessageListener chatMessageListener = new ChatMessageListener() {
        @Override
        public void processMessage(Chat chat, Message message) {
            Log.i("XMPPManager", "processMessage " + message.getFrom());
            // Process the incoming message.
            android.os.Message sendMessage = new android.os.Message();
            sendMessage.what = MESSAGE_FLAG;
            sendMessage.obj = message.getBody();
            handler.sendMessage(sendMessage);
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MESSAGE_FLAG) {
                String message = (String) msg.obj;
                addMessageToList(new ChatMessage().setLocal(false).setContent(message));
            }

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_connect) {
            if (XMPPManager.getInstance().getState() == XMPPState.NOT_CONNECTED) {
                XMPPManager.getInstance().reConnect();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        chat.close();
        XMPPManager.getInstance().connection.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setTitle(XMPPManager.getInstance().getState().getMessage());
    }
}
