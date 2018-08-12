package com.example.cassandrakane.goalz.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.cassandrakane.goalz.CameraFragment;
import com.example.cassandrakane.goalz.FriendActivity;
import com.example.cassandrakane.goalz.FriendsModalActivity;
import com.example.cassandrakane.goalz.MainActivity;
import com.example.cassandrakane.goalz.R;
import com.example.cassandrakane.goalz.ReactionModalActivity;
import com.example.cassandrakane.goalz.SearchFriendsActivity;
import com.example.cassandrakane.goalz.StoryFragment;
import com.example.cassandrakane.goalz.models.Goal;
import com.example.cassandrakane.goalz.models.Image;
import com.example.cassandrakane.goalz.models.Reaction;
import com.example.cassandrakane.goalz.models.Video;
import com.example.cassandrakane.goalz.utils.NavigationHelper;
import com.example.cassandrakane.goalz.utils.NotificationHelper;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    private final List<Goal> goals;
    private boolean personal;
    Context context;
    private Date currentDate = new Date();
    private ParseUser currentUser = ParseUser.getCurrentUser();
    private float startX = 0;
    private float endX = 0;
    private int startIndex = 0;
    private NavigationHelper navigationHelper;

    public GoalAdapter(List<Goal> gGoals, boolean personal) {
        this.goals = gGoals;
        this.personal = personal;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        return new ViewHolder(inflater.inflate(R.layout.item_goal, parent, false));
    }

    // bind the values based on the position of the element
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Goal goal = goals.get(position);
        startIndex = 0;

        if (personal) {
            final MainActivity activity = (MainActivity) context;
            navigationHelper = new NavigationHelper(activity.centralFragment.horizontalPager);

            final GestureDetector gestureDetector = getGestureDetector(goal, holder);

            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });

            holder.btnStory.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });

            holder.ivStory.setTransitionName("transition" + position);

        }

        setTextViews(goal, holder);
        setStreakViews(goal, holder);
        setFriendViews(goal, holder);
        setReactionViews(goal, holder);

        final List<ParseObject> story = goal.getStory();
        if (story.size() > 0) {
            setStory(goal, holder, story);
        } else {
            setEmptyGoal(goal, holder);
        }
    }

    @SuppressLint("DefaultLocale")
    private void setTextViews(final Goal goal, final ViewHolder holder) {
        holder.tvTitle.setText(goal.getTitle());
        if (goal.getCompleted()) {
            holder.tvProgress.setText(R.string.completed_label);
            Glide.with(context).asGif().load(R.drawable.confetti).into(holder.ivCelebrate);
        } else {
            holder.tvTitle.setTextColor(context.getResources().getColor(R.color.white));
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvProgress.setText(String.format("%d DAYS LEFT", goal.getDuration() - goal.getProgress()));
        }
    }

    private void setStreakViews(final Goal goal, final ViewHolder holder) {
        Date updateBy = goal.getUpdateStoryBy();
        if (updateBy != null) {
            if (currentDate.getTime() >= updateBy.getTime()) {
                // DISABLE FOR DEMO
                // goal.setStreak(0);

                long sum = updateBy.getTime() + TimeUnit.DAYS.toMillis(goal.getFrequency());
                Date newDate = new Date(sum);
                goal.setUpdateStoryBy(newDate);
                goal.setItemAdded(false);
                goal.saveInBackground();
            }
        }

        if (goal.getStreak() > 0) {
            holder.tvStreak.setText(String.format("%d", goal.getStreak()));
            holder.ivStar.setVisibility(View.VISIBLE);

            // HARDCODE FOR DEMO
            if (goal.getObjectId().equals("jBsVVmXedF") && !goal.getItemAdded()) {
                holder.ivStar.setImageResource(R.drawable.clock);
            } else {
                holder.ivStar.setImageResource(R.drawable.star);
            }

            // DISABLE FOR DEMO
//          int timeRunningOutHours = context.getResources().getInteger(R.integer.TIME_RUNNING_OUT_HOURS);
//          if (updateBy != null && (updateBy.getTime() - currentDate.getTime()) < TimeUnit.HOURS.toMillis(timeRunningOutHours) && !goal.getItemAdded() && !goal.getCompleted()){
//              holder.ivStar.setImageResource(R.drawable.clock);
//          } else {
//              holder.ivStar.setImageResource(R.drawable.star);
//          }
        }
    }

    private void setFriendViews(final Goal goal, final ViewHolder holder) {
        if (goal.getApprovedUsers().size() > 1) {
            holder.tvFriends.setVisibility(View.VISIBLE);
            holder.tvFriends.setText(String.valueOf(goal.getApprovedUsers().size() - 1));
            holder.tvFriends.setVisibility(View.VISIBLE);
            holder.btnFriends.setVisibility(View.VISIBLE);
            holder.btnFriends.setBackground(context.getResources().getDrawable(R.drawable.friend));
            holder.overlayFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, FriendsModalActivity.class);
                    i.putExtra(Goal.class.getSimpleName(), goal);
                    i.putExtra("personal", personal);
                    context.startActivity(i);
                }
            });
        } else if (personal) {
            holder.tvFriends.setVisibility(View.GONE);
            holder.btnFriends.setBackground(context.getResources().getDrawable(R.drawable.larger_add));
            holder.overlayFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.overlayFriends.setOnClickListener(null);
                    Intent i = new Intent(context, SearchFriendsActivity.class);
                    i.putExtra("requestActivity", FriendsModalActivity.class.getSimpleName());
                    i.putExtra(Goal.class.getSimpleName(), goal);
                    context.startActivity(i);
                }
            });
        }
    }

    private void setReactionViews(final Goal goal, final ViewHolder holder) {
        final List<Reaction> reax = goal.getReactions();
        int thumbs, goaled, claps, oks, bumps, rocks;
        thumbs = goaled = claps = oks = bumps = rocks = 0;
        int total = reax.size();
        for (int i = 0; i < reax.size(); i++) {

            String type = reax.get(i).getType();
            if (type != null) {
                switch (type) {
                    case "thumbs":
                        thumbs += 1;
                        break;
                    case "goals":
                        goaled += 1;
                        break;
                    case "clap":
                        claps += 1;
                        break;
                    case "ok":
                        oks += 1;
                        break;
                    case "bump":
                        bumps += 1;
                        break;
                    case "rock":
                        rocks += 1;
                        break;
                }
            }
        }

        // HARDCODE FOR DEMO
        if (goal.getObjectId().equals("jBsVVmXedF")) {
            holder.tvReaction.setText("7");
        } else if (goal.getObjectId().equals("zkavo2ePKo")) {
            holder.tvReaction.setText("4");
        } else if (goal.getObjectId().equals("0rTN9QsSlk")) {
            holder.tvReaction.setText("9");
        } else if (goal.getObjectId().equals("NaWB0UjSWz")) {
            holder.tvReaction.setText("3");
        } else {
            holder.tvReaction.setText(String.valueOf(total));
        }

        // holder.tvReaction.setText(String.valueOf(total));

        final ArrayList<Integer> reactionCounts = new ArrayList<>();
        reactionCounts.addAll(Arrays.asList(thumbs, goaled, claps, oks, bumps, rocks));
        holder.btnReaction.setTag(context.getResources().getColor(R.color.white));
        holder.btnReaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.btnReaction.setOnClickListener(null);
                Intent intent = new Intent(context, ReactionModalActivity.class);
                intent.putExtra("reactions", (Serializable) reax);
                intent.putIntegerArrayListExtra("reactionCounts", reactionCounts);
                context.startActivity(intent);
            }
        });
    }

    private void setStory(final Goal goal, final ViewHolder holder, final List<ParseObject> story) {
        startIndex = getStartIndex(story);
        holder.ibAdd.setVisibility(View.GONE);
        holder.tvAdd.setVisibility(View.GONE);
        holder.vGradient.setVisibility(View.VISIBLE);
        holder.rvGradient.setVisibility(View.VISIBLE);
        holder.tvProgress.setTextColor(context.getResources().getColor(R.color.white));
        holder.tvFriends.setTextColor(context.getResources().getColor(R.color.white));
        holder.btnFriends.setBackgroundTintList(context.getResources().getColorStateList(R.color.white));
        holder.btnReaction.setVisibility(View.VISIBLE);
        holder.tvReaction.setVisibility(View.VISIBLE);

        ParseFile image = null;

        try {
          Image parseObject = (Image) story.get(startIndex);

          image = parseObject.getImage();
        } catch (ClassCastException e) {
            e.printStackTrace();
            Video parseObject = (Video) story.get(startIndex);
            image = parseObject.getImage();
        }

        if (image != null) {
            Glide.with(context)
                    .load(image.getUrl())
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                    .into(holder.ivStory);
        }

        holder.btnStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context.getClass().isAssignableFrom(FriendActivity.class)) {
                    FriendActivity activity = (FriendActivity) context;
                    final FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    FragmentTransaction fragTransStory = fragmentManager.beginTransaction();
                    StoryFragment fragmentTwo = StoryFragment.newInstance(story, startIndex, currentUser, goal);
                    fragTransStory.add(R.id.root_layout, fragmentTwo).commit();

                    activity.ivProfile.setVisibility(View.GONE);
                    activity.cardView.setVisibility(View.GONE);
                    activity.btnBack.setVisibility(View.GONE);
                    activity.btnUnfriend.setVisibility(View.GONE);
                    activity.btnMessage.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setEmptyGoal(final Goal goal, final ViewHolder holder) {
        holder.btnFriends.setBackgroundTintList(context.getResources().getColorStateList(R.color.orange));
        holder.tvFriends.setTextColor(context.getResources().getColor(R.color.orange));
        holder.tvTitle.setTextColor(context.getResources().getColor(R.color.orange));
        holder.tvProgress.setTextColor(context.getResources().getColor(R.color.orange));
        holder.ivStory.setImageDrawable(null);
        holder.btnReaction.setVisibility(View.GONE);
        holder.tvReaction.setVisibility(View.INVISIBLE);
        holder.vGradient.setVisibility(View.INVISIBLE);
        holder.rvGradient.setVisibility(View.INVISIBLE);
        if (personal) {
            holder.ibAdd.setVisibility(View.VISIBLE);
            holder.tvAdd.setVisibility(View.VISIBLE);
            holder.ibAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigationHelper.toCamera();
                }
            });
            holder.btnStory.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    if (context.getClass().isAssignableFrom(MainActivity.class)) {
                        MainActivity activity = (MainActivity) context;
                        CameraFragment cameraFragment = CameraFragment.newInstance(goal);
                        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        FragmentTransaction fragTransStory = fragmentManager.beginTransaction();
                        fragTransStory.add(R.id.main_central_fragment, cameraFragment).commit();
                    }
                }
            });
        }
    }

    private GestureDetector getGestureDetector(final Goal goal, final ViewHolder holder) {
        return new GestureDetector(new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {

                final List<ParseObject> story = goal.getStory();

                if (story.size() > 0) {
                    startIndex = getStartIndex(story);
                    if (personal) {
                        MainActivity activity = (MainActivity) context;

//                        ProfileFragment current = (ProfileFragment) activity.getSupportFragmentManager().findFragmentById(R.id.root_layout);
//                        current.setSharedElementReturnTransition(TransitionInflater.from(context).inflateTransition(R.transition.default_transition));
//                        current.setExitTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.no_transition));
//
//                        StoryFragment frag = StoryFragment.newInstance(story, startIndex, currentUser, goal);
//                        frag.setSharedElementEnterTransition(TransitionInflater.from(context).inflateTransition(R.transition.default_transition));
//                        frag.setEnterTransition(TransitionInflater.from(context).inflateTransition(android.R.transition.no_transition));

                        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        FragmentTransaction fragTransStory = fragmentManager.beginTransaction();
                        StoryFragment frag = StoryFragment.newInstance(story, startIndex, currentUser, goal);
                        fragTransStory.add(R.id.main_central_fragment, frag).commit();
                    } else {
                        FriendActivity activity = (FriendActivity) context;
                        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
                        FragmentTransaction fragTransStory = fragmentManager.beginTransaction();
                        StoryFragment frag = StoryFragment.newInstance(story, startIndex, currentUser, goal);
                        fragTransStory.add(R.id.root_layout, frag).commit();
                        activity.ivProfile.setVisibility(View.INVISIBLE);
                        activity.cardView.setVisibility(View.INVISIBLE);
                        activity.btnBack.setVisibility(View.INVISIBLE);
                        activity.btnUnfriend.setVisibility(View.INVISIBLE);
                    }
                }

                return true;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                startX = motionEvent.getX();
                endX = motionEvent1.getX();
                if (endX >= startX + 60) {
                    navigationHelper.toCamera();
                    startX = 0;
                    endX = 0;
                } else if (startX >= endX + 50) {
                    navigationHelper.toFeed();
                    startX = 0;
                    endX = 0;
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                if (Math.abs(endX - startX) < 10) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.delete_goal)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    NotificationHelper notificationHelper = new NotificationHelper(context.getApplicationContext());
                                    notificationHelper.cancelReminder(goal);
                                    goals.remove(goal);
                                    goal.unpinInBackground();
                                    notificationHelper.cancelReminder(goal);
                                    removeGoal(goal.getObjectId());
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                startX = motionEvent.getX();
                endX = motionEvent1.getX();
                if (endX >= startX + 60) {
                    navigationHelper.toCamera();
                    startX = 0;
                    endX = 0;
                } else if (startX >= endX + 50) {
                    navigationHelper.toFeed();
                    startX = 0;
                    endX = 0;
                }
                return false;
            }

        });
    }

    private int getStartIndex(List<ParseObject> story) {
        for (int i = 0; i < story.size(); i++){
            boolean seen = false;
            ParseObject image = story.get(i);
            List<ParseUser> users = image.getList("viewedBy");
            if (users != null) {
                if (users.contains(currentUser)){
                    seen = true;
                }
            }

            if (!seen) {
                return i;
            }
        }
        return 0;
    }

    // slide the view from below itself to the current position
    private void slideRight(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                -view.getWidth()-7,                 // fromXDelta
                 0,                 // toXDelta
                0,  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    private void slideLeft(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                -view.getWidth()-7,                 // toXDelta
                0,                 // fromYDelta
                0); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    private void removeGoal(String id) {
        final ParseUser user = ParseUser.getCurrentUser();
        user.put("goals", goals);
        ParseACL acl = user.getACL();
        if (!acl.getPublicReadAccess()) {
            acl.setPublicReadAccess(true);
            user.setACL(acl);
        }
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    try {
                        user.fetch();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    Log.i("Profile Activity", "Failed to delete object, with error code: " + e.toString());
                }
            }
        });
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Goal");
        query.whereEqualTo("objectId", id);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                try {
                    if (object != null) {
                        object.delete();
                        object.saveInBackground();
                        notifyDataSetChanged();
                    }
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) TextView tvTitle;
        @BindView(R.id.tvStreak) TextView tvStreak;
        @BindView(R.id.tvProgress) TextView tvProgress;
        @BindView(R.id.ivStory) ImageView ivStory;
        @BindView(R.id.ivStar) ImageView ivStar;
        @BindView(R.id.btnFriends) Button btnFriends;
        @BindView(R.id.tvFriends) TextView tvFriends;
        @BindView(R.id.btnStory) Button btnStory;
        @BindView(R.id.ibAdd) Button ibAdd;
        @BindView(R.id.tvAdd) TextView tvAdd;
        @BindView(R.id.btnReaction) Button btnReaction;
        @BindView(R.id.tvReaction) TextView tvReaction;
        @BindView(R.id.vGradient) View vGradient;
        @BindView(R.id.rvGradient) View rvGradient;
        @BindView(R.id.ivCelebrate) ImageView ivCelebrate;
        @BindView(R.id.overlayFriends) Button overlayFriends;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}