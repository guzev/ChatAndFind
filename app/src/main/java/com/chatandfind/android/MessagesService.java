package com.chatandfind.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.chatandfind.android.config.Config;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;

public class MessagesService extends Service {
    private final static String TAG = "MessagesService";
    NotificationManager notificationManager;
    DatabaseReference databaseReference;
    String encodedEmail;
    ChildEventListener userChatListListener;
    HashSet<String> stringSet;

    public MessagesService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        encodedEmail = Config.encodeForFirebaseKey(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        stringSet = new HashSet<>();
        userChatListListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if ((long) dataSnapshot.child("lastSeenMessageTime").getValue() == (long) dataSnapshot.child("lastMessageTime").getValue()) {
                    stringSet.remove(dataSnapshot.getKey());
                } else {
                    stringSet.add(dataSnapshot.getKey());
                }
                sendNotification();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if ((long) dataSnapshot.child("lastSeenMessageTime").getValue() == (long) dataSnapshot.child("lastMessageTime").getValue()) {
                    stringSet.remove(dataSnapshot.getKey());
                } else {
                    stringSet.add(dataSnapshot.getKey());
                }
                sendNotification();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if ((long) dataSnapshot.child("lastSeenMessageTime").getValue() != (long) dataSnapshot.child("lastMessageTime").getValue()) {
                    stringSet.remove(dataSnapshot.getKey());
                }
                sendNotification();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        databaseReference = FirebaseDatabase.getInstance().getReference().child(Config.CHAT_LIST).child(encodedEmail);
        databaseReference.addChildEventListener(userChatListListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        databaseReference.removeEventListener(userChatListListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void sendNotification() {
        if (stringSet.size() == 0) {
            notificationManager.cancel(0);
        } else {
            String contentText = "У вас новые сообщения в " + stringSet.size() + (stringSet.size()  == 1 ? " чате" : " чатах");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle("Новые сообщения")
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.new_message_icon_notification)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.chatfind));

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            builder.setContentIntent(pendingIntent);
            notificationManager.notify(0, builder.build());
        }
    }
}
