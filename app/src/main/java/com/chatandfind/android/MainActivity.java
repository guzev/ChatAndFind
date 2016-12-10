package com.chatandfind.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
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
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView lastMessage;
        public TextView lastMessageTime;

        public ChatViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.chatName);
            lastMessage = (TextView) itemView.findViewById(R.id.lastMessage);
            lastMessageTime = (TextView) itemView.findViewById(R.id.lastTime);
        }
    }

    //Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<Chat, MainActivity.ChatViewHolder> recyclerAdapter;


    ProgressBar progressBar;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.chatRecyclerView);
        statusText = (TextView) findViewById(R.id.statusText);

        mFirebaseAuth = mFirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
        }

        //initial database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        recyclerAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class, R.layout.item_chat, MainActivity.ChatViewHolder.class, databaseReference.child("chats")) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, Chat model, int position) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHolder.title.setText(model.getTitle());
                viewHolder.lastMessage.setText(model.getLastMessage());
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdfDate.format(new Date(model.getLastMessageTime()));
                viewHolder.lastMessageTime.setText(date);
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


        databaseReference.child("chats").addValueEventListener(chatsListener);
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
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.add_chat:
                Chat newChat = new Chat("new Chat!", "нет сообщений", new Date().getTime());
                databaseReference.child("chats").push().setValue(newChat);
            default:
                return true;
        }
    }
}
