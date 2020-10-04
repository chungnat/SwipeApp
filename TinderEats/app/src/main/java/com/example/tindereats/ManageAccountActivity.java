package com.example.tindereats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ManageAccountActivity extends AppCompatActivity {

    private int GET_FROM_GALLERY = 3;
    private AccountManager account;

    private ImageButton profileImgBtn;
    private EditText nameET;
    private EditText usernameET;
    private EditText emailET;
    private TextView errorTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        profileImgBtn = findViewById(R.id.profileImgBtn);
        nameET = findViewById(R.id.nameET);
        usernameET = findViewById(R.id.usernameET);
        emailET = findViewById(R.id.emailET);
        errorTv = findViewById(R.id.errorTv);
        errorTv.setVisibility(View.INVISIBLE);
        final Button saveBtn = findViewById(R.id.saveBtn);

        // Set name and email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        final String displayName = user.getDisplayName();
        final String email = user.getEmail();
        nameET.setText(displayName);
        emailET.setText(email);

        // Set username
        account = new AccountManager(getApplicationContext(), uid);
        final String username = account.getUsername();
        usernameET.setText(username);

        // Find and load the user's profile picture
        ContextWrapper cw = new ContextWrapper(this);
        File directory = cw.getDir("profilePic", Context.MODE_PRIVATE);
        File file = new File(directory, uid + ".png");
        Picasso.get().invalidate(file);
        Picasso.get().load(file).transform(new RoundCornersTransformation(300, 0, true, true)).into(profileImgBtn);

        profileImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccount(displayName, email, username);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            // Load image into image view
            Uri selectedImage = data.getData();
            Picasso.get().load(selectedImage).transform(new RoundCornersTransformation(200, 0, true, true)).into(profileImgBtn);
            account.updateProfilePic(selectedImage);
        }
    }

    private void updateAccount(String prevName, String prevEmail, String prevUsername) {
        boolean changed = false;
        if(!prevName.equals(nameET.getText().toString())) {
            account.updateDisplayName(nameET.getText().toString());
            changed = true;
        }
        if(!prevEmail.equals(emailET.getText().toString())) {
            account.updateEmail(emailET.getText().toString());
            changed = true;
        }
        final String newUsername = usernameET.getText().toString();
        if(!prevUsername.equals(newUsername)) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference updateUsername = database.getReference("usernames");
            updateUsername.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        Map<String, String> usernames = (HashMap<String, String>) snapshot.getValue();
                        if (usernames.containsKey(newUsername)) {  // if username already taken
                            errorTv.setVisibility(View.VISIBLE);
                        }
                        else {
                            errorTv.setVisibility(View.INVISIBLE);
                            account.updateUsername(newUsername);
                            finish();
                        }
                    }
                    else {  // if no list of usernames yet
                        errorTv.setVisibility(View.INVISIBLE);
                        account.updateUsername(newUsername);
                        finish();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {} // read error
            });
        }
        if(changed) finish();
    }
}