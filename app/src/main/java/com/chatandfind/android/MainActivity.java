package com.chatandfind.android;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.chatandfind.android.config.Config;
import com.chatandfind.android.databaseObjects.Chat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static Context mContext;

    public static class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public String id;
        public String chatName;
        public TextView title;
        public TextView lastMessage;
        public TextView lastMessageTime;

        public ChatViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.chatName);
            lastMessage = (TextView) itemView.findViewById(R.id.lastMessage);
            lastMessageTime = (TextView) itemView.findViewById(R.id.lastTime);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick " + getAdapterPosition());
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra(Config.CHAT_ID_TAG, id);
            intent.putExtra(Config.CHAT_NAME_TAG, chatName);
            mContext.startActivity(intent);
        }
    }

    //Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference databaseReference;
    private DatabaseReference userChatsList;
    private DatabaseReference chatsSettingReference;
    private FirebaseRecyclerAdapter<Chat, MainActivity.ChatViewHolder> recyclerAdapter;


    ProgressBar progressBar;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    TextView statusText;
    String shortEmail;
    String displayName;
    String photoUrl;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.activity_main_progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerView);
        statusText = (TextView) findViewById(R.id.activity_main_statusText);
        toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.main_activity_list_of_chats);
        mContext = this;

        //initial database references
        databaseReference = FirebaseDatabase.getInstance().getReference();

        mFirebaseAuth = mFirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            shortEmail = mFirebaseUser.getEmail();
            shortEmail = shortEmail.substring(0, shortEmail.length() - 4);
            displayName = mFirebaseUser.getDisplayName();
            databaseReference.child(Config.USERS).child(shortEmail).child("displayName").setValue(displayName);
            if (mFirebaseUser.getPhotoUrl() != null) {
                photoUrl = mFirebaseUser.getPhotoUrl().toString();
                databaseReference.child(Config.USERS).child(shortEmail).child("photoUrl").setValue(photoUrl);
            } else {
                photoUrl = null;
            }
        }

        userChatsList = databaseReference.child(Config.CHAT_LIST).child(shortEmail);
        chatsSettingReference = databaseReference.child(Config.CHATS_SETTINGS);
        recyclerAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class, R.layout.item_chat, MainActivity.ChatViewHolder.class, userChatsList) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, Chat model, int position) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHolder.title.setText(model.getTitle());
                viewHolder.lastMessage.setText(model.getLastMessage());
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdfDate.format(new Date(model.getLastMessageTime()));
                viewHolder.lastMessageTime.setText(date);
                viewHolder.id = model.getId();
                viewHolder.chatName = model.getTitle();
            }
        };

        ValueEventListener chatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "chatsListener: onDataChange");
                if (!dataSnapshot.exists()) {
                    statusText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    statusText.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "chatsListener: onCancelled");
            }
        };


        userChatsList.addValueEventListener(chatsListener);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_sign_out:
                mFirebaseAuth.signOut();
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.main_menu_add_chat:
                Chat newChat = new Chat(Config.DEFAULT_CHAT_NAME, "нет сообщений", new Date().getTime());
                DatabaseReference newChatRef = userChatsList.push();
                newChat.setId(newChatRef.getKey());
                newChatRef.setValue(newChat);
                chatsSettingReference.child(newChat.getId()).child("title").setValue(Config.DEFAULT_CHAT_NAME);
                chatsSettingReference.child(newChat.getId()).child("users").child(shortEmail).child("displayName").setValue(displayName);
                chatsSettingReference.child(newChat.getId()).child("users").child(shortEmail).child("photoUrl").setValue(photoUrl);
                Log.d(TAG, "add chat with key: " + newChat.getId());
            default:
                return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
