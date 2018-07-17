package com.example.cassandrakane.goalz.models;

import android.media.Image;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

@ParseClassName("Post")
public class Goal extends ParseObject {

    public Goal() {
        super();
    }

    public Goal(String title, String description, int duration, int frequency, int progress, int streak, ArrayList<ParseFile> story, ParseUser user) {
        super();
        setTitle(title);
        setDescription(description);
        setDuration(duration);
        setFrequency(frequency);
        setProgress(progress);
        setStreak(streak);
        setStory(story);
        setUser(user);
    }

    public ArrayList<ParseFile> getStory() {
        JSONArray arr = getJSONArray("images");
        ArrayList<ParseFile> story = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            try {
                story.add((ParseFile) (arr.get(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return story;
    }

    public String getTitle() {
        return getString("title");
    }

    public String getDescription() {
        return getString("description");
    }

    public int getDuration() {
        return getInt("duration");
    }

    public int getFrequency() {
        return getInt("frequency");
    }

    public int getProgress() {
        return getInt("progress");
    }

    public int getStreak() {
        return getInt("streak");
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setStory(ArrayList<ParseFile> story) {
        put("images", story);
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public void setDuration(int duration) {
        put("duration", duration);
    }

    public void setFrequency(int frequency) {
        put("frequency", frequency);
    }

    public void setProgress(int progress) {
        put("progress", progress);
    }

    public void setStreak(int streak) {
        put("streak", streak);
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }
}