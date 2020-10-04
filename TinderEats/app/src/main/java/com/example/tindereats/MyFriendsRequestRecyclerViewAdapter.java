package com.example.tindereats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

public class MyFriendsRequestRecyclerViewAdapter extends RecyclerView.Adapter<MyFriendsRequestRecyclerViewAdapter.ViewHolder> {

    private JSONArray nameList;
    private JSONArray usernameList;
    private JSONArray iconList;
    private int size;
    private String uid;
    private Context context;


    public MyFriendsRequestRecyclerViewAdapter(JSONArray nameList, JSONArray usernameList, JSONArray iconList, int size, Context context) {
        this.nameList = nameList;
        this.usernameList = usernameList;
        this.iconList = iconList;
        this.size = size;
        this.context = context;
        this.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if(size > 0) {
            try {
                holder.nameRTv.setText(nameList.getString(position));
                holder.usernameRTv.setText("@" + usernameList.getString(position));
                Picasso.get().load(iconList.getString(position)).transform(new RoundCornersTransformation(60, 0, true, true)).into(holder.friendRIcon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        holder.denyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyFriend request = new modifyFriend();
                try {
                    request.execute(uid, usernameList.getString(position), "False");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                removeRequest(position);
                Toast.makeText(context, "Friend Request Rejected", Toast.LENGTH_SHORT).show();
            }
        });
        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyFriend request = new modifyFriend();
                try {
                    request.execute(uid, usernameList.getString(position), "True");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                removeRequest(position);
                Toast.makeText(context, "Friend Request Accepted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeRequest(int position) {
        nameList.remove(position);
        iconList.remove(position);
        usernameList.remove(position);
        notifyItemRemoved(position);
        size--;
        notifyItemRangeChanged(position, size);
        if(size == 0) {
            ((FriendRequestsActivity)context).noRequestsTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return size;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        private ImageView friendRIcon;
        private TextView nameRTv;
        private ImageButton acceptBtn;
        private ImageButton denyBtn;
        private TextView usernameRTv;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            friendRIcon = mView.findViewById(R.id.friendRIcon);
            nameRTv = mView.findViewById(R.id.nameRTv);
            acceptBtn = mView.findViewById(R.id.acceptBtn);
            denyBtn = mView.findViewById(R.id.denyBtn);
            usernameRTv = mView.findViewById(R.id.usernameRTv);
        }

    }


}
