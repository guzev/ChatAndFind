package com.chatandfind.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private String username;
    private String e_mail;
    private String imageURL;

    private TextView usernameView;
    private TextView e_mailView;
    private ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mFirebaseAuth = mFirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mFirebaseUser = mFirebaseAuth.getCurrentUser();

            // Name, email address, and profile photo Url
            username = mFirebaseUser.getDisplayName();
            e_mail = mFirebaseUser.getEmail();
            if (mFirebaseUser.getPhotoUrl() != null) {
                imageURL = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        usernameView.setText(username);
        e_mailView.setText(e_mail);
        if (imageURL != null) Glide.with(this).load(imageURL).into(photo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


}
