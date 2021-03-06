package com.example.cassandrakane.goalz;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cassandrakane.goalz.adapters.SearchFriendAdapter;
import com.example.cassandrakane.goalz.models.Goal;
import com.example.cassandrakane.goalz.models.GoalRequests;
import com.example.cassandrakane.goalz.models.SentFriendRequests;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;

public class SearchFriendsActivity extends AppCompatActivity {

    List<ParseUser> searched;
    SearchFriendAdapter searchfriendAdapter;

    @BindView(R.id.searchView) SearchView searchView;
    @Nullable @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.rvSearched) RecyclerView rvSearched;
    @BindView(R.id.btnConfirm) FloatingActionButton btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friends);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        ButterKnife.bind(this);

        String requestActivityName = getIntent().getStringExtra("requestActivity");
        List<ParseUser> selectedUsers = new ArrayList<ParseUser>();

        if (requestActivityName.equals(FriendsModalActivity.class.getSimpleName())) {
            final Goal goal = getIntent().getParcelableExtra(Goal.class.getSimpleName());

            searched = getNonPendingFriends(goal);
            btnConfirm.setVisibility(View.VISIBLE);

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<ParseUser> newPending = goal.getPendingUsers();
                    newPending.addAll(searchfriendAdapter.selectedFriends);
                    goal.setPendingUsers(newPending);
                    List<ParseUser> newFriends = goal.getFriends();
                    newFriends.addAll(searchfriendAdapter.selectedFriends);
                    goal.setFriends(newFriends);
                    goal.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            sendGoalRequest(goal, searchfriendAdapter.selectedFriends);
                            Toast.makeText(SearchFriendsActivity.this,  searchfriendAdapter.selectedFriends.size() > 1 ? "Sent requests to friends!" : "Sent request to friend!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }
            });
        }
        if (requestActivityName.equals(MainActivity.class.getSimpleName())) {
            searched = getUsers();
            btnConfirm.setVisibility(View.INVISIBLE);
        }
        searchfriendAdapter = new SearchFriendAdapter(searched, selectedUsers, requestActivityName);
        rvSearched.setLayoutManager(new GridLayoutManager(this, 3));
        rvSearched.setAdapter(searchfriendAdapter);

        TextView searchText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        Typeface typeface = getResources().getFont(R.font.quicksand_regular);
        searchText.setTypeface(typeface);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchfriendAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    public List<ParseUser> getFriends() {
        List<ParseUser> friends = null;
        try {
            friends = ParseUser.getCurrentUser().fetch().getList("friends");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public List<ParseUser> getNonPendingFriends(Goal goal) {
        List<ParseUser> friends = getFriends();
        List<ParseUser> pending = goal.getPendingUsers();

        for(int i = friends.size() - 1; i >= 0; i--) {
            if (pending.contains(friends.get(i)) || friends.get(i).getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                friends.remove(i);
            }
        }
        return friends;
    }

    public void sendGoalRequest(Goal goal, List<ParseUser> pending) {
        for (int i = 0; i < pending.size(); i++) {
            GoalRequests request = new GoalRequests(pending.get(i), ParseUser.getCurrentUser(), goal);
            request.saveInBackground();
        }
    }

    public List<ParseUser> getUsers() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        List<ParseUser> friends = getFriends();
        final List<ParseUser> users = new ArrayList<>();
        List<String> friendUsernames = new ArrayList<>();
        if (friends != null) {
            for (int i = 0; i < friends.size(); i++) {
                friendUsernames.add(friends.get(i).getUsername());
            }
        }
        final List<String> friendNames = friendUsernames;
        ParseQuery<SentFriendRequests> query2 = ParseQuery.getQuery("SentFriendRequests");
        query2.include("toUser");
        query2.include("fromUser");
        query2.whereEqualTo("fromUser", ParseUser.getCurrentUser());
        query2.findInBackground(new FindCallback<SentFriendRequests>() {
            @Override
            public void done(List<SentFriendRequests> objects, ParseException e) {
                if (objects != null) {
                    for (int i = 0; i < objects.size(); i++) {
                        SentFriendRequests request = objects.get(i);
                        try {
                            friendNames.add(request.getToUser().fetch().getUsername());
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
        ParseQuery<SentFriendRequests> query3 = ParseQuery.getQuery("SentFriendRequests");
        query3.include("toUser");
        query3.include("fromUser");
        query3.whereEqualTo("toUser", ParseUser.getCurrentUser());
        query3.findInBackground(new FindCallback<SentFriendRequests>() {
            @Override
            public void done(List<SentFriendRequests> objects, ParseException e) {
                if (objects != null) {
                    for (int i = 0; i < objects.size(); i++) {
                        SentFriendRequests request = objects.get(i);
                        try {
                            friendNames.add(request.getFromUser().fetch().getUsername());
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (objects != null) {
                    for (int i = 0; i < objects.size(); i++) {
                        if (!ParseUser.getCurrentUser().getUsername().equals(objects.get(i).getUsername())
                                && !friendNames.contains(objects.get(i).getUsername())) {
                            users.add(objects.get(i));
                        }
                    }
                }
            }
        });
        return users;
    }

    public void goBack(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
