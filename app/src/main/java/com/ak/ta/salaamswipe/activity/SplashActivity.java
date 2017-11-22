package com.ak.ta.salaamswipe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ak.ta.salaamswipe.R;
import com.ak.ta.salaamswipe.constants.AppConstant;
import com.ak.ta.salaamswipe.gcm.GCMUtils;
import com.ak.ta.salaamswipe.utils.PrefUtils;
import com.flurry.android.FlurryAgent;

/**
 * Created by ashishkumar on 16/6/16.
 * Display animation at splash screen
 */
public class SplashActivity extends Activity {
    public Handler mHandler;
    Intent intent = null;
    private int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler = new Handler();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performNavigation();
            }
        }, SPLASH_TIME_OUT);

        GCMUtils.getRegId(this);

    }

    public void performNavigation() {
        boolean isLoggedIn = PrefUtils.getSharedPrefBoolean(this, AppConstant.IS_LOGGED_IN);

        if (!isLoggedIn) {
            intent = new Intent(SplashActivity.this, SwipeViewHomeActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, HomeFindingPeopleActivity.class);

        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }
}
