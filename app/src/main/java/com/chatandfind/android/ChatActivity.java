package com.chatandfind.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chatandfind.android.databaseObjects.Chat;
import com.chatandfind.android.databaseObjects.Message;
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

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView senderTextView;
        public CircleImageView senderImegeView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            senderTextView = (TextView) itemView.findViewById(R.id.senderTextView);
            senderImegeView = (CircleImageView) itemView.findViewById(R.id.senderImageView);
        }
    }

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    ProgressBar progressBar;
    String chatId;

    //Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<Message, ChatActivity.MessageViewHolder> recyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        progressBar = (ProgressBar) findViewById(R.id.chatProgressBar);

        Intent intent = getIntent();
        chatId = (String) intent.getCharSequenceExtra(Config.CHAT_ID_TAG);

        mFirebaseAuth = mFirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        recyclerAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(Message.class, R.layout.item_message, ChatActivity.MessageViewHolder.class, databaseReference.child("chats").child(chatId)) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHolder.messageTextView.setText(model.getText());
                viewHolder.senderTextView.setText(model.getName());
                if (model.getPhotoUrl() != null) {
                    Glide.with(ChatActivity.this).load(model.getPhotoUrl()).into(viewHolder.senderImegeView);
                }
            }
        };

        /*ValueEventListener chatsListener = new ValueEventListener() {
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
        };*/


        //databaseReference.child("chats").addValueEventListener(chatsListener);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
    }
}
