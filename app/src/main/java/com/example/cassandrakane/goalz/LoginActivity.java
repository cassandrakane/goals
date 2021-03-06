package com.example.cassandrakane.goalz;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.cassandrakane.goalz.utils.DataFetcher;
import com.example.cassandrakane.goalz.utils.Util;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    DataFetcher dataFetcher;

    @BindView(R.id.tvUsername) EditText tvUsername;
    @BindView(R.id.tvPassword) EditText tvPassword;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        tvUsername.setText("");
        tvPassword.setText("");

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }

        tvUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Util.hideKeyboard(v, LoginActivity.this);
                }
            }
        });

        tvPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Util.hideKeyboard(v, LoginActivity.this);
                }
            }
        });
    }

    public void login(View v) {
        progressBar.setVisibility(View.VISIBLE);
        final String username = tvUsername.getText().toString();
        final String password = tvPassword.getText().toString();
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    // transition to home screen
                    dataFetcher = new DataFetcher(user, LoginActivity.this);
                    Util.updateFriends();
                    overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
                    Toast.makeText(LoginActivity.this, "Welcome, " + username + "!", Toast.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Login failed. Try again or sign up.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void toSignup(View v) {
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

}
