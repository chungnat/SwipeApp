package com.example.tindereats;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 123;
    private DatabaseReference mDatabase;
    private String token;
    private String defaultIcon = "gs://tinder-eats.appspot.com/default_icon.png";


    public void onStart() {
        super.onStart();
        createNotificationChannel();

        // Get registration token for the device
        // Token is needed to send user notifications/messages
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        // Get new Instance ID token
                        token = task.getResult().getToken();
                        System.out.println(token);
                    }
                });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Match_Request";
            String description = "receive requests from friends";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("ID_MATCH", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        // OnCreate, if uno user logged in, start log in process
        if(mAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    new AuthUI.IdpConfig.EmailBuilder().build()
                            ))

                            .setTheme(R.style.LoginTheme)
//                                .setTosAndPrivacyPolicyUrls(
//                                        "https://example.com/terms.html",
//                                        "https://example.com/privacy.html")
                            .build(),

                    RC_SIGN_IN);
        }
        else {  // else, head to home screen
            loggedIn();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final String email = user.getEmail();
                final String name = user.getDisplayName();
                String uid = user.getUid();

                mDatabase = FirebaseDatabase.getInstance().getReference("users/" + uid);

                if(response.isNewUser()) {  // if user signed in is a new user
                    final FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference gsReference = storage.getReferenceFromUrl(defaultIcon);
                    gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {  // get Uri of default profile picture
                        @Override
                        public void onSuccess(Uri uri) {
                            User newUser = new User(name, email, "", uri.toString(), token);  // create User
                            mDatabase.setValue(newUser);  // Add user to database
                            // Update local user profile
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(uri)
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
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) { // if getting default profile pic failed, initialize user without uri
                            User newUser = new User(name, email, "", "", token);
                            mDatabase.setValue(newUser);
                        }
                    });
                    // Save default icon to local internal storage
                    Drawable drawable = AppCompatResources.getDrawable(this, R.mipmap.default_icon);
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    ContextWrapper cw = new ContextWrapper(getApplicationContext());
                    File directory = cw.getDir("profilePic", Context.MODE_PRIVATE);
                    File file = new File(directory, "profilePic" + ".jpg");
                    if (!file.exists()) {
                        Log.d("path", file.toString());
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                            fos.close();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent additionalInfo = new Intent(getApplicationContext(), com.example.tindereats.FinishSignUpActivity.class);
                    additionalInfo.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(additionalInfo);  // Go to next activity to finish sign up process
                }
                else {  // if user is not new user, upload token(in case new device), then go to home screen
                    DatabaseReference ref = mDatabase.child("registrationToken");
                    ref.setValue(token);
                    loggedIn();
                }

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if(response != null) {
                    int e = response.getError().getErrorCode();
                }
            }
        }
    }

    private void loggedIn() {
        Intent goToHome = new Intent(getApplicationContext(), com.example.tindereats.HomeActivity.class);
        goToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(goToHome);
        finish();
    }
}