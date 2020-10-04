package com.example.tindereats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddFriendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        final TextView userErrorTv = findViewById(R.id.userErrorTv);
        final EditText userSearchET = findViewById(R.id.userSearchET);
        Button sendRequestBtn = findViewById(R.id.sendRequestBtn);
        userErrorTv.setVisibility(View.INVISIBLE);

        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = userSearchET.getText().toString();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference updateUsername = database.getReference("usernames");
                updateUsername.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            Map<String, String> usernames = (HashMap<String, String>) snapshot.getValue();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            AccountManager account = new AccountManager(getApplicationContext(), user.getUid());
                            if (usernames.containsKey(username) && !username.equals(account.getUsername())) {  // if username exists, and username is not the user's username
                                userErrorTv.setVisibility(View.INVISIBLE);

                                // Send friend request
                                // Send username to server
                                sendFriendRequest request = new sendFriendRequest();
                                request.execute(username, user.getUid());
                                // server will lookup uid of username
                                // make friends list or sender and recieiver
                                // friend will have uid,status
                                //status will be either true, sent, or recieved
                                // true = friends, sent = user sent a request to uid, recieved = user got a request
                                // in app, read the database to see all pending friends
                                finish();
                            }
                            else {  // if username doesn't exist
                                userErrorTv.setVisibility(View.VISIBLE);
                            }
                        }
                        else {  // if no list of usernames yet
                            userErrorTv.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {} // read error
                });

            }
        });
    }

    private class sendFriendRequest extends AsyncTask<String, String, Response> {

        @Override
        protected Response doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            String username = strings[0];
            String Uid = strings[1];
            String url = getString(R.string.ServerAddress) + "sendFriendRequest";
            MediaType mediaType = MediaType.get("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "Username=" + username
                                + "&Uid=" + Uid);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Response result) {

            if(result != null) {
                try {
                    Toast.makeText(AddFriendActivity.this, result.body().string(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(AddFriendActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}