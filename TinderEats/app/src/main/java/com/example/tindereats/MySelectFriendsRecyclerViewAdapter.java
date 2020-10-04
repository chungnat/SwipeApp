package com.example.tindereats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class MySelectFriendsRecyclerViewAdapter extends RecyclerView.Adapter<MySelectFriendsRecyclerViewAdapter.ViewHolder> {

    private JSONArray nameList;
    private JSONArray usernameList;
    private JSONArray iconList;
    private int size;
    private String uid;
    private OnCheckClick callback;

    public MySelectFriendsRecyclerViewAdapter(JSONArray nameList, JSONArray usernameList, JSONArray iconList, int size, OnCheckClick listener) {
        this.nameList = nameList;
        this.usernameList = usernameList;
        this.iconList = iconList;
        this.size = size;
        this.callback = listener;
        this.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_friend_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if(size > 0) {
            try {
                holder.nameSTv.setText(nameList.getString(position));
                holder.usernameSTv.setText("@" + usernameList.getString(position));
                Picasso.get().load(iconList.getString(position)).transform(new RoundCornersTransformation(60, 0, true, true)).into(holder.friendSIcon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        holder.friendCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.friendCheckbox.isChecked()) {
                    try {
                        callback.onClick(usernameList.getString(position), true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {  // user unchecked checkbox
                    try {
                        callback.onClick(usernameList.getString(position), false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return size;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        private ImageView friendSIcon;
        private TextView nameSTv;
        private TextView usernameSTv;
        private CheckBox friendCheckbox;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            friendSIcon = view.findViewById(R.id.friendSIcon);
            nameSTv = view.findViewById(R.id.nameSTv);
            usernameSTv = view.findViewById(R.id.usernameSTv);
            friendCheckbox = view.findViewById(R.id.friendCheckbox);
        }

    }
}
