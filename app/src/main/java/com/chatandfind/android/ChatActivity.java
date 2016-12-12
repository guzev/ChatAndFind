package com.chatandfind.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chatandfind.android.databaseObjects.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity{
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
    TextView statusText;
    String chatId;
    Button sendButton;
    EditText editText;
    String email;

    //Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference databaseReference;
    private DatabaseReference chatDatabaseReference;
    private DatabaseReference settingsDatabaseReference;
    private FirebaseRecyclerAdapter<Message, ChatActivity.MessageViewHolder> recyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        chatId = (String) intent.getCharSequenceExtra(Config.CHAT_ID_TAG);

        recyclerView = (RecyclerView) findViewById(R.id.activity_chat_recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.activity_chat_progress_bar);
        statusText = (TextView) findViewById(R.id.activity_chat_status_text);
        sendButton = (Button) findViewById(R.id.activity_chat_send_button);
        editText = (EditText) findViewById(R.id.activity_chat_edit_text);

        mFirebaseAuth = mFirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        email = mFirebaseUser.getEmail();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        chatDatabaseReference = databaseReference.child("chats").child(chatId);
        settingsDatabaseReference = databaseReference.child("chats_settings").child(chatId);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message(editText.getText().toString(), mFirebaseUser.getDisplayName(), null);
                if (mFirebaseUser.getPhotoUrl() != null) {
                    message.setPhotoUrl(mFirebaseUser.getPhotoUrl().toString());
                }
                chatDatabaseReference.push().setValue(message);
            }
        });

        recyclerAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(Message.class, R.layout.item_message, ChatActivity.MessageViewHolder.class, chatDatabaseReference) {
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

        ValueEventListener messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "messagesListener: onDataChange");
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
                Log.e(TAG, "messagesListener: onCancelled");
            }
        };


        chatDatabaseReference.addValueEventListener(messagesListener);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_add_user:
                Intent intent = new Intent(this, AddUserActivity.class);
                startActivityForResult(intent, 1);
            default:
                return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            String new_email = data.getStringExtra(Config.NEW_USER_EMAIL);
            settingsDatabaseReference.child("users").child(new_email).setValue(true);
        }
    }
}
