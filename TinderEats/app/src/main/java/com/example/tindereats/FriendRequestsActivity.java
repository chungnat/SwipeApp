package com.example.tindereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FriendRequestsActivity extends AppCompatActivity {

    private RecyclerView requestList;
    public TextView noRequestsTv;
    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        this.context = this;
        requestList = findViewById(R.id.requestList);
        noRequestsTv = findViewById(R.id.noRequestsTv);
        noRequestsTv.setVisibility(View.INVISIBLE);
        requestList.setLayoutManager(new LinearLayoutManager(this));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        getPendingFriends request = new getPendingFriends();
        request.execute(user.getUid());
    }

    public void noFriendsTv(boolean bool) {
        if(bool) {
            noRequestsTv.setVisibility(View.VISIBLE);
        }
        else {
            noRequestsTv.setVisibility(View.INVISIBLE);
        }
    }
    private class getPendingFriends extends AsyncTask<String, String, Response> {


        @Override
        protected Response doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            String uid = strings[0];
            String url = getString(R.string.ServerAddress) + "getPendingFriends";
            MediaType mediaType = MediaType.get("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "Uid=" + uid);
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
                String resp = null;
                try {
                    resp = result.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject jsonResp = new JSONObject(resp);
                    JSONArray nameList = jsonResp.getJSONArray("names");
                    JSONArray usernameList = jsonResp.getJSONArray("usernames");
                    JSONArray iconList = jsonResp.getJSONArray("icons");
                    int size = jsonResp.getInt("size");
                    if(size == 0) {
                        noFriendsTv(true);
                    }
                    else {
                        noFriendsTv(false);
                    }
                    requestList.setAdapter(new MyFriendsRequestRecyclerViewAdapter(nameList, usernameList, iconList, size, context));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}