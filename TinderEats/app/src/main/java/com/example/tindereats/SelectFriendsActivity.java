package com.example.tindereats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SelectFriendsActivity extends AppCompatActivity implements OnCheckClick{

    private RecyclerView selectList;
    private TextView noFriendsListTv;
    public List<String> invitedFriends;
    public OnCheckClick instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friends);
        instance = this;
        invitedFriends = new ArrayList<>();
        EditText searchFriendsEt = findViewById(R.id.searchFriendsEt);
        selectList = findViewById(R.id.selectFriendsRv);
        selectList.setLayoutManager(new LinearLayoutManager(this));
        Button pickRestBtn = findViewById(R.id.pickRestBtn);
        noFriendsListTv = findViewById(R.id.noFriendsListTv);
        noFriendsListTv.setVisibility(View.INVISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        GetFriendsList request = new GetFriendsList();
        request.execute(user.getUid());
        pickRestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRestaurant = new Intent(getApplicationContext(), RestaurantFilterActivity.class);
                toRestaurant.putStringArrayListExtra("list", (ArrayList<String>) invitedFriends);
                startActivity(toRestaurant);
            }
        });

        // TODO make edit text filter recyclerview
        searchFriendsEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

            }
        });
    }

    @Override
    public void onClick(String value, boolean bool) {
        if(bool) {
            invitedFriends.add(value);
        }
        else {
            invitedFriends.remove(value);
        }
    }

    // TODO make all server post requests have token verification
    private class GetFriendsList extends AsyncTask<String, String, Response> {

        @Override
        protected Response doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            String uid = strings[0];
            String url = getString(R.string.ServerAddress) + "getFriends";
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
                        noFriendsListTv.setVisibility(View.VISIBLE);
                    }
                    else {
                        noFriendsListTv.setVisibility(View.INVISIBLE);
                    }
                    selectList.setAdapter(new MySelectFriendsRecyclerViewAdapter(nameList, usernameList, iconList, size, instance));
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