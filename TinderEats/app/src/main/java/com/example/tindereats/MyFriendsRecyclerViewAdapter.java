package com.example.tindereats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;


public class MyFriendsRecyclerViewAdapter extends RecyclerView.Adapter<MyFriendsRecyclerViewAdapter.ViewHolder> {

    private JSONArray nameList;
    private JSONArray usernameList;
    private JSONArray iconList;
    private int size;
    private Context context;
    private FriendsFragment instance;

    public MyFriendsRecyclerViewAdapter(JSONArray nameList, JSONArray usernameList, JSONArray iconList, int size, Context context, FriendsFragment instance) {
        this.nameList = nameList;
        this.usernameList = usernameList;
        this.iconList = iconList;
        this.size = size;
        this.context = context;
        this.instance = instance;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if(size > 0) {
            try {
                holder.name.setText(nameList.getString(position));
                holder.usernameCardTv.setText("@" + usernameList.getString(position));
                Picasso.get().load(iconList.getString(position)).transform(new RoundCornersTransformation(60, 0, true, true)).into(holder.icon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        holder.removeFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                try {
                    builder.setMessage("Unfriend " + nameList.getString(position) + "?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    modifyFriend request = new modifyFriend();
                                    try {
                                        request.execute(FirebaseAuth.getInstance().getCurrentUser().getUid(), usernameList.getString(position), "False");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    nameList.remove(position);
                                    iconList.remove(position);
                                    usernameList.remove(position);
                                    notifyItemRemoved(position);
                                    size--;
                                    notifyItemRangeChanged(position, size);
                                    if(size == 0) {
                                        instance.noFriendsTv.setVisibility(View.VISIBLE);
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    // Do nothing
                                }
                            });
                    builder.create().show();
                } catch (JSONException e) {
                    e.printStackTrace();
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
        private ImageView icon;
        private TextView name;
        private Button removeFriendBtn;
        private TextView usernameCardTv;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            icon = mView.findViewById(R.id.friendIcon);
            name = mView.findViewById(R.id.nameTv);
            removeFriendBtn = mView.findViewById(R.id.removeFriendBtn);
            usernameCardTv = mView.findViewById(R.id.usernameCardTv);
        }

    }
}