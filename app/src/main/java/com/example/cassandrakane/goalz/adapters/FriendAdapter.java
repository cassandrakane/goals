package com.example.cassandrakane.goalz.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cassandrakane.goalz.FriendActivity;
import com.example.cassandrakane.goalz.R;
import com.example.cassandrakane.goalz.utils.Util;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ParseUser> friends;
    Context context;

    public FriendAdapter(List<ParseUser> friends) {
        this.friends = friends;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        return new ViewHolder(inflater.inflate(R.layout.item_friend, parent, false));
    }

    // bind the values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        // get the data according to position
        final ParseUser friend = friends.get(position);

        final ViewHolder viewHolder = (ViewHolder) holder;
        try {
            viewHolder.tvUsername.setText(friend.fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final ParseFile file = friend.getParseFile("image");
        Util.setImage(file, context.getResources(), viewHolder.ivProfile, R.color.orange);
        viewHolder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toFriendActivity(friend);
            }
        });
        viewHolder.tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toFriendActivity(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }


    private void toFriendActivity(ParseUser friend) {
        Intent i = new Intent(context, FriendActivity.class);
        i.putExtra(ParseUser.class.getSimpleName(), friend);
        context.startActivity(i);
    }

    // create ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvUsername) TextView tvUsername;
        @BindView(R.id.ivProfile) ImageView ivProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
