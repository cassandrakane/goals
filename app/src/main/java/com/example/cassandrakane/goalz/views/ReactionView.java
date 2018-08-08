package com.example.cassandrakane.goalz.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.example.cassandrakane.goalz.R;
import com.example.cassandrakane.goalz.StoryFragment;
import com.example.cassandrakane.goalz.models.Image;
import com.example.cassandrakane.goalz.models.Reaction;
import com.example.cassandrakane.goalz.models.Video;
import com.example.cassandrakane.goalz.utils.Constants;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReactionView extends View {

    private static final int SCALE_DURATION = 200;
    private static final int TRANSLATION_DURATION = 700;
    private static final int CHILD_TRANSLATION_DURATION = 200;
    private static final int CHILD_DELAY = 50;
    private static final int DRAW_DELAY = 25;
    private RoundedBoard board;
    private List<Emotion> emotions;
    public int selectedIndex = -1;

    private String type;
    private ParseObject mObject;

    private SelectingAnimation selectingAnimation;
    private DeselectAnimation deselectAnimation;

    private StoryFragment fragment;
    private Context context;

    private Runnable runnable = new Runnable() {
        @Override public void run() {
            invalidate();
        }
    };

    public ReactionView(Context context, StoryFragment frag, ParseObject object) {
        super(context);
        this.context = context;
        fragment = frag;
        mObject = object;
        init();
    }

    public ReactionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReactionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        board = new RoundedBoard();
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        emotions = Arrays.asList(
                new Emotion(getContext(), getContext().getDrawable(R.drawable.thumbs_react)),
                new Emotion(getContext(), getContext().getDrawable(R.drawable.goals_react)),
                new Emotion(getContext(), getContext().getDrawable(R.drawable.clap_react)),
                new Emotion(getContext(), getContext().getDrawable(R.drawable.ok_react)),
                new Emotion(getContext(), getContext().getDrawable(R.drawable.bump_react))
        );

        selectingAnimation = new SelectingAnimation();
        deselectAnimation = new DeselectAnimation();
        deselectAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}

            @Override public void onAnimationEnd(Animation animation) {
                selectedIndex = -1;
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });

        startAnimation(new TranslationAnimation());

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < emotions.size(); i++) {
                    if (event.getX() > emotions.get(i).x &&
                            event.getX() < emotions.get(i).x + emotions.get(i).size &&
                            event.getY() > emotions.get(i).y - 10 && event.getY() < emotions.get(i).y + Emotion.MEDIUM_SIZE + 10) {
                        onSelect(i);
                        break;
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < emotions.size(); i++) {
                    if (event.getX() > emotions.get(i).x &&
                            event.getX() < emotions.get(i).x + emotions.get(i).size &&
                            event.getY() > emotions.get(i).y - 10 && event.getY() < emotions.get(i).y + Emotion.MEDIUM_SIZE + 10) {
                        onSelect(i);
                        break;
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                onDeselect();
                setVisibility(GONE);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void onSelect(int index) {
        if (selectedIndex == index) {
            return;
        }

        selectedIndex = index;

        selectingAnimation.prepare();
        startAnimation(selectingAnimation);
    }

    private void onDeselect() {
        /**
         * use selectedIndex
         */
        fragment.ivReaction.clearColorFilter();

        if (selectedIndex != -1) {
            Integer count = Integer.parseInt(fragment.tvReactionCount.getText().toString()) + 1;
            fragment.tvReactionCount.setText(Integer.toString(count));
        }

        switch (selectedIndex) {
            case 0:
                type = "thumbs";
                fragment.ivReaction.setImageDrawable(context.getResources().getDrawable(R.drawable.thumbs_react));
                break;
            case 1:
                type = "goals";
                fragment.ivReaction.setImageDrawable(context.getResources().getDrawable(R.drawable.goals_react));
                break;
            case 2:
                type = "clap";
                fragment.ivReaction.setImageDrawable(context.getResources().getDrawable(R.drawable.clap_react));
                break;
            case 3:
                type = "ok";
                fragment.ivReaction.setImageDrawable(context.getResources().getDrawable(R.drawable.ok_react));
                break;
            case 4:
                type = "bump";
                fragment.ivReaction.setImageDrawable(context.getResources().getDrawable(R.drawable.bump_react));
                break;
            default:
                type = "";

        }

        if (!type.equals("")) {

            if (mObject.get("video") != null) {
                final Video parseObject = (Video) mObject;
                final Reaction reaction = new Reaction(type, ParseUser.getCurrentUser());
                reaction.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        List<ParseObject> reactions = parseObject.getList("reactions");
                        if (reactions == null) {
                            reactions = new ArrayList<>();
                        }
                        reactions.add(reaction);
                        parseObject.setReactions(reactions);
                        parseObject.saveInBackground();
                        List<ParseObject> reacts = StoryFragment.goal.getReactions();
                        reacts.add(reaction);
                        StoryFragment.goal.setReactions(reacts);
                        StoryFragment.goal.saveInBackground();
                    }
                });
            } else {
                final Image parseObject = (Image) mObject;
                final Reaction reaction = new Reaction(type, ParseUser.getCurrentUser());
                reaction.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        List<ParseObject> reactions = parseObject.getList("reactions");
                        if (reactions == null) {
                            reactions = new ArrayList<>();
                        }

                        reactions.add(reaction);
                        parseObject.setReactions(reactions);
                        parseObject.saveInBackground();
                        List<ParseObject> reacts = StoryFragment.goal.getReactions();
                        reacts.add(reaction);
                        StoryFragment.goal.setReactions(reacts);
                        
                        StoryFragment.goal.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Log.i("sdf", "success");
                            }
                        });
                    }
                });
            }
        }

        deselectAnimation.prepare();
        startAnimation(deselectAnimation);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        board.draw(canvas);
        for (Emotion emotion : emotions) {
            emotion.draw(canvas);
        }

        postDelayed(runnable, DRAW_DELAY);
    }

    private void animateEmotions(float interpolatedTime) {

        for (Emotion emotion : emotions) {
            animateEmotionSize(emotion, interpolatedTime);
            animateEmotionPosition(emotion);
        }
    }

    private void animateEmotionPosition(Emotion emotion) {
        emotion.y = RoundedBoard.BASE_LINE - emotion.size;

        emotions.get(0).x = RoundedBoard.LEFT + Constants.HORIZONTAL_SPACING;
        emotions.get(emotions.size() - 1).x =
                RoundedBoard.LEFT + RoundedBoard.WIDTH - Constants.HORIZONTAL_SPACING
                        - emotions.get(emotions.size() - 1).size;

        for (int i = 1; i < selectedIndex; i++) {
            emotions.get(i).x = emotions.get(i - 1).x + emotions.get(i - 1).size
                    + Constants.HORIZONTAL_SPACING;
        }

        for (int i = emotions.size() - 2; i > selectedIndex; i--) {
            emotions.get(i).x = emotions.get(i + 1).x - emotions.get(i).size
                    - Constants.HORIZONTAL_SPACING;
        }

        if (selectedIndex > 0) {
            emotions.get(selectedIndex).x = emotions.get(selectedIndex - 1).x
                    + emotions.get(selectedIndex - 1).size + Constants.HORIZONTAL_SPACING;
        }
    }

    private void animateEmotionSize(Emotion emotion, float interpolatedTime) {
        emotion.setCurrentSize(emotion.startAnimatedSize +
                (int) (interpolatedTime * (emotion.endAnimatedSize - emotion.startAnimatedSize)));
    }

    private void animateRoundedBoard(float interpolatedTime) {
        board.setCurrentHeight(board.startAnimatedHeight + (interpolatedTime *
                (board.endAnimatedHeight - board.startAnimatedHeight)));
    }

    private class SelectingAnimation extends Animation {

        SelectingAnimation() {
            setDuration(SCALE_DURATION);
        }

        void prepare(){
            prepareEmotions();
            prepareRoundedBoard();
        }

        private void prepareEmotions() {
            for (int i = 0; i < emotions.size(); i++) {
                emotions.get(i).startAnimatedSize = emotions.get(i).size;

                if (i == selectedIndex) {
                    emotions.get(i).endAnimatedSize = Emotion.LARGE_SIZE;
                } else {
                    emotions.get(i).endAnimatedSize = Emotion.SMALL_SIZE;
                }
            }
        }

        private void prepareRoundedBoard() {
            board.startAnimatedHeight = board.height;
            board.endAnimatedHeight = RoundedBoard.SCALED_DOWN_HEIGHT;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            animateRoundedBoard(interpolatedTime);
            animateEmotions(interpolatedTime);
        }
    }

    private class DeselectAnimation extends Animation {

        DeselectAnimation() {
            setDuration(SCALE_DURATION);
        }

        void prepare(){
            prepareRoundedBoard();
            prepareEmotions();
        }

        private void prepareEmotions() {
            for (Emotion emotion : emotions) {
                emotion.startAnimatedSize = emotion.size;
                emotion.endAnimatedSize = Emotion.MEDIUM_SIZE;
            }
        }

        private void prepareRoundedBoard() {
            board.startAnimatedHeight = board.height;
            board.endAnimatedHeight = RoundedBoard.HEIGHT;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            animateRoundedBoard(interpolatedTime);
            animateEmotions(interpolatedTime);
        }
    }

    private class TranslationAnimation extends Animation {

        private static final int TRANSLATION_DISTANCE = 150;
        private final int EMOTION_RADIUS = Emotion.MEDIUM_SIZE / 2;

        TranslationAnimation() {
            setDuration(TRANSLATION_DURATION);
            prepareRoundedBoard();
            prepareEmotions();
        }

        private void prepareEmotions() {
            for (int i = 0; i < emotions.size(); i++) {
                emotions.get(i).endAnimatedY = RoundedBoard.TOP + Constants.VERTICAL_SPACING;
                emotions.get(i).startAnimatedY =
                        emotions.get(i).y = RoundedBoard.BOTTOM + TRANSLATION_DISTANCE;

                emotions.get(i).startAnimatedX
                        = emotions.get(i).x = i == 0 ? RoundedBoard.LEFT
                        + Constants.HORIZONTAL_SPACING + (Emotion.MEDIUM_SIZE / 2)
                        : emotions.get(i - 1).x + Emotion.MEDIUM_SIZE + Constants.HORIZONTAL_SPACING;
            }
        }

        private void prepareRoundedBoard() {
            board.startAnimatedY = board.y = RoundedBoard.BOTTOM + TRANSLATION_DISTANCE;
            board.endAnimatedY = RoundedBoard.TOP;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            translateEmotions(interpolatedTime);
            translateRoundedBoard();
        }

        private void translateEmotions(float interpolatedTime) {
            float currentTime = interpolatedTime * TRANSLATION_DURATION;
            for (int i = 0; i < emotions.size(); i++) {

                int delayOfChild = CHILD_DELAY * i;

                Emotion view = emotions.get(i);
                if ((currentTime > delayOfChild)) {
                    if ((currentTime - delayOfChild) <= CHILD_TRANSLATION_DURATION) {

                        float progressOfChild = ((currentTime - delayOfChild) / CHILD_TRANSLATION_DURATION);
                        view.y = view.startAnimatedY +
                                progressOfChild * (view.endAnimatedY - view.startAnimatedY);

                        view.x = view.startAnimatedX - progressOfChild * EMOTION_RADIUS;

                        view.setCurrentSize((int) (progressOfChild * Emotion.MEDIUM_SIZE));
                    } else {
                        view.x = view.startAnimatedX - EMOTION_RADIUS;
                        view.y = view.endAnimatedY;
                        view.setCurrentSize(Emotion.MEDIUM_SIZE);
                    }
                }
            }
        }

        private void translateRoundedBoard() {
            Emotion firstEmoticon = emotions.get(0);
            float d =
                    (firstEmoticon.y - firstEmoticon.startAnimatedY) / (firstEmoticon.endAnimatedY
                            - firstEmoticon.startAnimatedY) * (board.endAnimatedY - board.startAnimatedY);

            board.y = board.startAnimatedY + d;
        }

    }

}

