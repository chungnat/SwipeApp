package com.example.tindereats;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class FriendsFragment extends Fragment {


    private static final String ARG_COLUMN_COUNT = "column-count";
    RecyclerView recyclerView;
    private int mColumnCount = 1;
    private FriendsFragment instance;
    public TextView noFriendsTv;

    public FriendsFragment() {
    }

    @SuppressWarnings("unused")
    public static FriendsFragment newInstance(int columnCount) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);
        noFriendsTv = view.findViewById(R.id.noFriendsTv);
        noFriendsTv.setVisibility(View.INVISIBLE);
        // Set the adapter
        Context context = view.getContext();
        recyclerView = view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }


        FloatingActionButton newFriend = view.findViewById(R.id.addFriendFab);
        newFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addFriend = new Intent(getActivity(), AddFriendActivity.class);
                startActivity(addFriend);
            }
        });
        Button friendRequestBtn = view.findViewById(R.id.friendRequestBtn);

        friendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToRequests = new Intent(getActivity(), FriendRequestsActivity.class);
                startActivity(goToRequests);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        GetFriendsList request = new GetFriendsList();
        request.execute(user.getUid());
    }

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
                        noFriendsTv.setVisibility(View.VISIBLE);
                    }
                    else {
                        noFriendsTv.setVisibility(View.INVISIBLE);
                    }
                    recyclerView.setAdapter(new MyFriendsRecyclerViewAdapter(nameList, usernameList, iconList, size, getActivity(),instance));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}