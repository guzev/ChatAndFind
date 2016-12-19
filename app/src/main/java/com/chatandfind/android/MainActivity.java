package com.chatandfind.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.chatandfind.android.config.Config;
import com.chatandfind.android.databaseObjects.Chat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static Context mContext;

    public static class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public String id;
        public TextView title;
        public TextView lastMessage;
        public TextView lastMessageTime;
        public CircleImageView senderImageView;
        public ImageView newMessageIcon;

        public ChatViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.chatName);
            lastMessage = (TextView) itemView.findViewById(R.id.lastMessage);
            lastMessageTime = (TextView) itemView.findViewById(R.id.lastTime);
            senderImageView = (CircleImageView) itemView.findViewById(R.id.senderImageView);
            newMessageIcon = (ImageView) itemView.findViewById(R.id.newMessageIcon);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick " + getAdapterPosition());
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra(Config.CHAT_ID_TAG, id);
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
    private ValueEventListener chatsListener;


    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private TextView statusText;
    private String encodedEmail;
    private String displayName;
    private String photoUrl;
    private RequestManager glide;
    private boolean singInFinish;
    private boolean hasLocationPermission;

    private Toolbar toolbar;


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
        glide = Glide.with(this);

        //initial database references
        databaseReference = FirebaseDatabase.getInstance().getReference();

        mFirebaseAuth = mFirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            singInFinish = true;
            finish();
            return;
        } else {
            singInFinish = false;
            encodedEmail = Config.encodeForFirebaseKey(mFirebaseUser.getEmail());
            displayName = mFirebaseUser.getDisplayName();
            databaseReference.child(Config.USERS).child(encodedEmail).child("displayName").setValue(displayName);
            if (mFirebaseUser.getPhotoUrl() != null) {
                photoUrl = mFirebaseUser.getPhotoUrl().toString();
                databaseReference.child(Config.USERS).child(encodedEmail).child("photoUrl").setValue(photoUrl);
            } else {
                photoUrl = null;
            }
        }

        chatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "chatsListener: onDataChange");
                if (!dataSnapshot.exists()) {
                    statusText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
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

        userChatsList = databaseReference.child(Config.CHAT_LIST).child(encodedEmail);
        chatsSettingReference = databaseReference.child(Config.CHATS_SETTINGS);
        Query query = userChatsList.orderByChild("lastMessageTime");
        recyclerAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class, R.layout.item_chat, MainActivity.ChatViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, Chat model, int position) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHolder.title.setText(model.getTitle());
                viewHolder.lastMessage.setText(model.getLastMessage());
                String date = Config.sdfDate.format(new Date(model.getLastMessageTime()));
                viewHolder.lastMessageTime.setText(date);
                if (model.getPhotoUrl() != null) {
                    glide.load(model.getPhotoUrl()).into(viewHolder.senderImageView);
                }
                viewHolder.newMessageIcon.setVisibility(model.getLastMessageTime() != model.getLastSeenMessageTime() ? View.VISIBLE : View.INVISIBLE);
                viewHolder.id = model.getId();
            }
        };

        userChatsList.addValueEventListener(chatsListener);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);

        Intent serviceIntent = new Intent(this, MessagesService.class);
        serviceIntent.putExtra(Config.ENC_EMAIL_TAG, encodedEmail);
        startService(serviceIntent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Config.MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 && permissions[0].equals(android.Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(this, UpdatingLocationService.class));
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this, "location permission was denied", Toast.LENGTH_LONG).show();
            }
        }
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
                startActivity(new Intent(this, SignInActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            case R.id.main_menu_add_chat:
                long currentTime = System.currentTimeMillis();
                Chat newChat = new Chat(Config.DEFAULT_CHAT_NAME, "нет сообщений", currentTime, currentTime, photoUrl);
                DatabaseReference newChatRef = userChatsList.push();
                newChat.setId(newChatRef.getKey());
                newChatRef.setValue(newChat);
                chatsSettingReference.child(newChat.getId()).child("title").setValue(Config.DEFAULT_CHAT_NAME);
                chatsSettingReference.child(newChat.getId()).child("users").child(encodedEmail).child("displayName").setValue(displayName);
                chatsSettingReference.child(newChat.getId()).child("users").child(encodedEmail).child("photoUrl").setValue(photoUrl);
                Log.d(TAG, "add chat with key: " + newChat.getId());
            default:
                return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        stopService(new Intent(this, UpdatingLocationService.class));
        if (!singInFinish) userChatsList.removeEventListener(chatsListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "don't have location permission");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, Config.MY_LOCATION_REQUEST_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }
}
