package com.chatandfind.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chatandfind.android.databaseObjects.MemberOfChat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatMembersActivity extends AppCompatActivity {
    private final static String TAG = "chatMembersActivity";

    public static class ChatMemberViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView photoView;
        public TextView nameTextView;

        public ChatMemberViewHolder(View itemView) {
            super(itemView);
            photoView = (CircleImageView) itemView.findViewById(R.id.member_photo);
            nameTextView = (TextView) itemView.findViewById(R.id.member_display_name);
        }
    }

    String chatId;
    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;

    //Firebase variables
    DatabaseReference databaseReference;
    FirebaseRecyclerAdapter<MemberOfChat, ChatMemberViewHolder> recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_members);

        recyclerView = (RecyclerView) findViewById(R.id.members_recycler_view);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        chatId = intent.getStringExtra(Config.CHAT_ID_TAG);

        databaseReference = databaseReference.child(Config.CHATS_SETTINGS).child(chatId).child("users");

        recyclerAdapter = new FirebaseRecyclerAdapter<MemberOfChat, ChatMemberViewHolder>(MemberOfChat.class, R.layout.item_chat_member, ChatMemberViewHolder.class, databaseReference) {
            @Override
            protected void populateViewHolder(ChatMemberViewHolder viewHolder, MemberOfChat model, int position) {
                viewHolder.nameTextView.setText(model.getDisplayName());
                if (model.getPhotoUrl() != null) {
                    Glide.with(chatMembersActivity.this).load(model.getPhotoUrl()).into(viewHolder.photoView);
                }
            }
        };
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
    }
}
