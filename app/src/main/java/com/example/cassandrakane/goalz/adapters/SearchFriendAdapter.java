package com.example.cassandrakane.goalz.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cassandrakane.goalz.R;
import com.example.cassandrakane.goalz.SearchFriendsActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class SearchFriendAdapter extends RecyclerView.Adapter<SearchFriendAdapter.ViewHolder> implements Filterable {

    List<ParseUser> searchList;
    List<ParseUser> filteredList;
    Context context;

    public SearchFriendAdapter(List<ParseUser> list) {
        searchList = list;
        filteredList = list;
    }

    @NonNull
    @Override
    public SearchFriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchFriendAdapter.ViewHolder holder, int position) {
        final ParseUser user = filteredList.get(position);
        holder.tvUsername.setText(user.getUsername());
        if (user.getParseFile("profile") != null) {
            ParseFile imageFile = user.getParseFile("profile");
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(imageFile.getFile().getAbsolutePath());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
            roundedBitmapDrawable.setCornerRadius(85.0f);
            roundedBitmapDrawable.setAntiAlias(true);
            holder.ivProfile.setImageDrawable(roundedBitmapDrawable);
        }
        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(user);
            }
        });
    }

    public void addFriend(final ParseUser user) {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        List<ParseUser> friends = currentUser.getList("friends");
        friends.add(0, user);
        currentUser.put("friends", friends);
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    try {
                        currentUser.fetch();
                        Intent data  = new Intent();
                        data.putExtra(ParseUser.class.getSimpleName(), user);
                        ((SearchFriendsActivity) context).setResult(RESULT_OK, data);
                        ((SearchFriendsActivity) context).finish();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    Log.i("Search Friend Activity", "Failed to update object, with error code: " + e.toString());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredList = searchList;
                } else {
                    List<ParseUser> filtered = new ArrayList<>();
                    for (ParseUser user : searchList) {
                        if (user.getUsername().toLowerCase().contains(charString)) {
                            filtered.add(user);
                        }
                    }
                    filteredList = filtered;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredList = (List<ParseUser>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvUsername;
        ImageView ivProfile;
        Button addBtn;

        public ViewHolder(View view) {
            super(view);

            tvUsername = view.findViewById(R.id.tvUsername);
            ivProfile = view.findViewById(R.id.ivProfile);
            addBtn = view.findViewById(R.id.add_btn);
        }
    }

}