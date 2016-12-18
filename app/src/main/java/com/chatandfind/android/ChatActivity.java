package com.chatandfind.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.bumptech.glide.RequestManager;
import com.chatandfind.android.config.Config;
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

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private final int addUserActivityCode = 1;
    private final int renameChatActivityCode = 2;

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

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ProgressBar progressBar;
    private TextView statusText;
    private String chatId;
    private Button sendButton;
    private EditText editText;
    private String encodedEmail;
    private Toolbar toolbar;
    private RequestManager glide;

    //Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference databaseReference;
    private DatabaseReference chatDatabaseReference;
    private DatabaseReference settingsDatabaseReference;
    private FirebaseRecyclerAdapter<Message, ChatActivity.MessageViewHolder> recyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        this.glide = Glide.with(this);
        Intent intent = getIntent();
        chatId = (String) intent.getCharSequenceExtra(Config.CHAT_ID_TAG);

        recyclerView = (RecyclerView) findViewById(R.id.activity_chat_recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.activity_chat_progress_bar);
        statusText = (TextView) findViewById(R.id.activity_chat_status_text);
        sendButton = (Button) findViewById(R.id.activity_chat_send_button);
        editText = (EditText) findViewById(R.id.activity_chat_edit_text);
        toolbar = (Toolbar) findViewById(R.id.chat_activity_toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAuth = mFirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        encodedEmail = Config.encodeForFirebaseKey(mFirebaseUser.getEmail());
        databaseReference = FirebaseDatabase.getInstance().getReference();
        chatDatabaseReference = databaseReference.child(Config.CHATS).child(chatId);
        settingsDatabaseReference = databaseReference.child(Config.CHATS_SETTINGS).child(chatId);
        settingsDatabaseReference.child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setTitle((String) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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
                final Message message = new Message(editText.getText().toString().trim(), mFirebaseUser.getDisplayName(), null, System.currentTimeMillis());
                if (mFirebaseUser.getPhotoUrl() != null) {
                    message.setPhotoUrl(mFirebaseUser.getPhotoUrl().toString());
                }
                chatDatabaseReference.push().setValue(message);
                settingsDatabaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String text = message.getText();
                        long time = message.getTime();
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String user_email = userSnapshot.getKey();
                            DatabaseReference userList = databaseReference.child(Config.CHAT_LIST).child(user_email);
                            databaseReference.child(Config.CHAT_LIST).child(user_email).child(chatId).child("lastMessage").setValue(text);
                            databaseReference.child(Config.CHAT_LIST).child(user_email).child(chatId).child("lastMessageTime").setValue(time);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                editText.setText("");
            }
        });

        recyclerAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(Message.class, R.layout.item_message, ChatActivity.MessageViewHolder.class, chatDatabaseReference) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHolder.messageTextView.setText(model.getText());
                viewHolder.senderTextView.setText(model.getName());
                if (model.getPhotoUrl() != null) {
                    glide.load(model.getPhotoUrl()).into(viewHolder.senderImegeView);
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

        chatDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
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
                Intent add_user_intent = new Intent(this, AddUserActivity.class);
                startActivityForResult(add_user_intent, addUserActivityCode);
                break;
            case R.id.chat_rename:
                Intent chat_rename_intent = new Intent(this, RenameChatActivity.class);
                startActivityForResult(chat_rename_intent, renameChatActivityCode);
                break;
            case R.id.show_list_of_members:
                Intent showListIntent = new Intent(this, chatMembersActivity.class);
                showListIntent.putExtra(Config.CHAT_ID_TAG, chatId);
                startActivity(showListIntent);
                break;
            case R.id.chat_activity_to_map:
                Intent toMapIntent = new Intent(this, GoogleMapsActivity.class);
                toMapIntent.putExtra(Config.CHAT_ID_TAG, chatId);
                startActivity(toMapIntent);
                break;
            case R.id.exit_chat:
                settingsDatabaseReference.child("users").child(encodedEmail).setValue(null);
                databaseReference.child(Config.CHAT_LIST).child(encodedEmail).child(chatId).setValue(null);
                settingsDatabaseReference.child("users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            settingsDatabaseReference.setValue(null);
                            chatDatabaseReference.setValue(null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
                finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            switch (requestCode) {
                case addUserActivityCode:
                    final String new_email = data.getStringExtra(Config.NEW_USER_EMAIL);
                    databaseReference.child(Config.USERS).child(new_email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            settingsDatabaseReference.child("users").child(new_email).child("displayName").setValue(map.get("displayName"));
                            settingsDatabaseReference.child("users").child(new_email).child("photoUrl").setValue(map.get("photoUrl"));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    databaseReference.child(Config.CHAT_LIST).child(encodedEmail).child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            Chat newChat = new Chat((String) map.get("title"), (String) map.get("lastMessage"), (Long) map.get("lastMessageTime"));
                            newChat.setId(chatId);
                            FirebaseDatabase.getInstance().getReference().child(Config.CHAT_LIST).child(new_email).child(chatId).setValue(newChat);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    break;
                case renameChatActivityCode:
                    final String new_chat_name = data.getStringExtra(Config.NEW_CHAT_NAME);
                    Log.d(TAG, "new chat name: " + new_chat_name);
                    settingsDatabaseReference.child("title").setValue(new_chat_name);
                    settingsDatabaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String user_email = userSnapshot.getKey();
                                Log.d(TAG, "user encodedEmail: " + user_email);
                                databaseReference.child(Config.CHAT_LIST).child(user_email).child(chatId).child("title").setValue(new_chat_name);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    break;
            }
        }
    }
}
