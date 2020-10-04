package com.example.tindereats;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FinishSignUpActivity extends AppCompatActivity {

    private int GET_FROM_GALLERY = 3;
    ImageButton profilePic;
    AccountManager account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_sign_up);

        profilePic = findViewById(R.id.profileImgBtn);
        TextView nameTv = findViewById(R.id.nameTv);
        final EditText usernameEdit = findViewById(R.id.usernameEdit);
        Button skipBtn = findViewById(R.id.skipBtn);
        Button doneBtn = findViewById(R.id.doneBtn);
        final TextView nameErrorTv = findViewById(R.id.nameErrorTv);
        nameErrorTv.setVisibility(View.INVISIBLE);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                goHome();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        Picasso.get().load(R.mipmap.default_icon).transform(new RoundCornersTransformation(200, 0, true, true)).into(profilePic);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        final String displayName = user.getDisplayName();
        nameTv.setText(displayName);
        account = new AccountManager(getApplicationContext(), uid);
        // let user upload a picture
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        // user skipped, assign them default profile picture and random username
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = displayName.replaceAll("\\s", "");
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference updateUsername = database.getReference("usernames");
                updateUsername.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            Map<String, String> usernames = (HashMap<String, String>) snapshot.getValue();
                            String assignedUsername = username;
                            Random r = new Random();
                            // look through current usernames to find one that is not taken
                            while (usernames.containsKey(assignedUsername)) {  // randomly assign an open username
                                assignedUsername = username + r.nextInt(100);
                            }
                            account.updateUsername(assignedUsername);
                        }
                        else {  // if no list of usernames yet
                            account.updateUsername(username);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {} // read error
                });
                goHome();
            }
        });

        // user put in own info, save picture and username
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameEdit.getText().toString().replaceAll("\\s", "");
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference updateUsername = database.getReference("usernames");
                updateUsername.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            Map<String, String> usernames = (HashMap<String, String>) snapshot.getValue();
                            if (usernames.containsKey(username)) {  // if username already taken
                               nameErrorTv.setVisibility(View.VISIBLE);
                            }
                            else {
                                nameErrorTv.setVisibility(View.INVISIBLE);
                                account.updateUsername(username);
                                goHome();
                            }
                        }
                        else {  // if no list of usernames yet
                            account.updateUsername(username);
                            goHome();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {} // read error
                });
            }
        });
    }

    protected void goHome() {
        Intent goHome = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(goHome);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            // Load image into image view
            Uri selectedImage = data.getData();
            Picasso.get().load(selectedImage).transform(new RoundCornersTransformation(200, 0, true, true)).into(profilePic);
            account.updateProfilePic(selectedImage);
        }
    }
}