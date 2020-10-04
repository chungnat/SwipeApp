package com.example.tindereats;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;

public class userFragment extends Fragment {

    private ImageView icon;
    private TextView displayNameTv;
    private TextView usernameTv;

    public userFragment() {
        // Required empty public constructor
    }

    public static userFragment newInstance() {
        userFragment fragment = new userFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_user, container, false);

        icon = layout.findViewById(R.id.iconImg);
        displayNameTv = layout.findViewById(R.id.nameTv);
        usernameTv = layout.findViewById(R.id.usernameTv);
        Button accountBtn = layout.findViewById(R.id.accountBtn);
        Button logOutBtn = layout.findViewById(R.id.logOutBtn);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getDisplayName();
        String email = user.getEmail();
        String uid = user.getUid();
        Uri uri = user.getPhotoUrl();
        displayNameTv.setText(name);

        // Find and load the user's info
        updateAccountDetails();

        // Listener for manage account button
        accountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editAccount = new Intent(getActivity(), ManageAccountActivity.class);
                startActivity(editAccount);
            }
        });

        // Listener for log out button
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(getActivity())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent logOut = new Intent(getActivity(), MainActivity.class);
                                logOut.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(logOut);
                            }
                        });
            }
        });

        // Inflate the layout for this fragment
        return layout;
    }

    // TODO add display name to sharedprefs to make updates quicker
    public void updateAccountDetails() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ContextWrapper cw = new ContextWrapper(getActivity());
        final File directory = cw.getDir("profilePic", Context.MODE_PRIVATE);
        File file = new File(directory, user.getUid() + ".png");
        if(file.exists()) {  // if profile pic is in local storage, load it
            Picasso.get().invalidate(file);
            Picasso.get().load(file).transform(new RoundCornersTransformation(200, 0, true, true)).into(icon);
        }
        else {  // download image from cloud storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference(user.getUid() + ".png");
            File newFile = new File(directory, user.getUid() + ".png");
            storageRef.getFile(newFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    File pic = new File(directory, user.getUid() + ".png");
                    if(pic.exists()) {  // if profile pic is in local storage, load it
                        Picasso.get().invalidate(pic);
                        Picasso.get().load(pic).transform(new RoundCornersTransformation(200, 0, true, true)).into(icon);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
        AccountManager account = new AccountManager(getActivity(), user.getUid());
        String username = account.getUsername();
        if(username.isEmpty()) {  // if can't find username in sharedpref, retrieve username from database and save it in sharedpref
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("users/" + user.getUid() + "/username");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        String databaseUsername = snapshot.getValue(String.class);
                        usernameTv.setText("@" + databaseUsername);
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor prefEditor = sharedPref.edit();
                        prefEditor.putString("Username:" + user.getUid(), databaseUsername);
                        prefEditor.apply();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            usernameTv.setText("@" + username);
        }
        displayNameTv.setText(user.getDisplayName());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAccountDetails();
    }
}