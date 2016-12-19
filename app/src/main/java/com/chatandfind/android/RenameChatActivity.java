package com.chatandfind.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chatandfind.android.config.Config;

public class RenameChatActivity extends AppCompatActivity {
    EditText editText;
    TextView errorTextView;
    Button renameChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_chat);

        editText = (EditText) findViewById(R.id.rename_chat_edit_text);
        errorTextView = (TextView) findViewById(R.id.rename_chat_error_text);
        renameChatButton = (Button) findViewById(R.id.rename_chat_button);

        renameChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = editText.getText().toString().trim();
                if (newName.length() > 0 && newName.equals(Config.encodeForFirebaseKey(newName))) {
                    Intent intent = new Intent();
                    intent.putExtra(Config.NEW_CHAT_NAME, newName);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    errorTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
