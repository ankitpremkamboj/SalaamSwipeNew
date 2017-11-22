package com.ak.ta.salaamswipe.activity;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ak.ta.salaamswipe.R;

/*  Created by ashishkumar .
* If User did not found any match then show this screen
* */

public class NoMatchesActivity extends BaseActivity {

    FrameLayout matchHeader;
    private ImageView homeIcon;

    @Override
    public int setLayout() {
        return R.layout.activity_no_matches;
    }

    @Override
    public void initUi() {

        matchHeader = (FrameLayout) findViewById(R.id.match_header);
        matchHeader.setVisibility(View.VISIBLE);

        homeIcon = (ImageView) findViewById(R.id.mh_home_icon);
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    public String getName() {
        return NoMatchesActivity.class.getName();
    }
}
