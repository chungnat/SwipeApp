package com.example.tindereats;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AccountManager {

    private Context context;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private String uid;
    private SharedPreferences sharedPref;

    public AccountManager(Context context, String uid) {
        this.context = context;
        this.uid = uid;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.storage = FirebaseStorage.getInstance();
        this.database = FirebaseDatabase.getInstance();
    }

    public void updateDisplayName(String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Tag", "User profile updated.");
                        }
                    }
                });
        DatabaseReference updateName = database.getReference("users/" + uid + "/name");
        updateName.setValue(name);
    }

    public void updateEmail(String email) {
        DatabaseReference updateName = database.getReference("users/" + uid + "/email");
        updateName.setValue(email);
        user.updateEmail(email);
    }

    // Updates a given username to sharedpreferences and database
    // Does not check if username is valid
    public void updateUsername(String username) {
        String prevUsername = sharedPref.getString("Username:" + uid, "");
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("Username:" + uid, username);
        prefEditor.apply();
        DatabaseReference updateUsername = database.getReference("users/" + uid + "/username");
        updateUsername.setValue(username);
        if(!prevUsername.isEmpty()) {
            updateUsername = database.getReference("usernames/" + prevUsername);
            updateUsername.removeValue();
        }
        updateUsername = database.getReference("usernames/" + username);
        updateUsername.setValue(uid);
    }

    // Retrieves username from sharedpref
    // Will return empty string if user has recently deleted local data or reinstalled app
    public String getUsername() {
        return sharedPref.getString("Username:" + uid, "");
    }

    // where uri is the local path of the picture
    // returns false if image path does not lead to a valid file
    public boolean updateProfilePic(Uri picture) {
        Bitmap bitmap = null;
        // Save uploaded photo into internal storage
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), picture);
            bitmap.setHasAlpha(true);
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("profilePic", Context.MODE_PRIVATE);
            File file = new File(directory, uid + ".png");
            if(file.exists()) {
                file.delete();
            }
            file = new File(directory, uid + ".png");
            if (!file.exists()) {
                Log.d("path", file.toString());
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        final StorageReference storageRef = storage.getReference(uid + ".png");
        final UploadTask uploadTask = storageRef.putFile(picture);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    // On upload success, get the image's Uri
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return storageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {  // If getting the image's Uri is successful
                            Uri downloadUri = task.getResult();
                            // Update the user's photoUrl
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Tag", "User profile updated.");
                                            }
                                        }
                                    });
                            // Update user's icon in user's profile in realtime database
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users/" + uid + "/icon");
                            mDatabase.setValue(downloadUri.toString());
                        } else {
                            // Handle failures
                        }
                    }
                });
            }
        });
        return true;
    }
}
