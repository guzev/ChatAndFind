package com.chatandfind.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chatandfind.android.config.Config;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddUserActivity extends AppCompatActivity {
    private static final String TAG = "AddUserActivity";

    EditText editText;
    TextView errorTextView;
    Button addUserButton;

    //Firebase variables
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        editText = (EditText) findViewById(R.id.add_user_edit_text);
        errorTextView = (TextView) findViewById(R.id.add_user_error_text);
        addUserButton = (Button) findViewById(R.id.add_user_button);
        databaseReference = FirebaseDatabase.getInstance().getReference().child(Config.USERS);

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = editText.getText().toString().trim();
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String encodedEmail = Config.encodeForFirebaseKey(email);
                        if (dataSnapshot.hasChild(encodedEmail)) {
                            Intent intent = new Intent();
                            intent.putExtra(Config.NEW_USER_EMAIL, encodedEmail);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            errorTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
