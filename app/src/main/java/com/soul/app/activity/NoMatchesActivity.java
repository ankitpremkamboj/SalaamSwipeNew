package com.soul.app.activity;

import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.soul.app.R;
import com.soul.app.activity.*;
import com.soul.app.utils.Utility;

/*  Created by ashishkumar .
* If User did not found any match then show this screen
* */

public class NoMatchesActivity extends com.soul.app.activity.BaseActivity {

    FrameLayout matchHeader;
    private ImageView homeIcon;
    private ImageView profile_icon;

    @Override
    public int setLayout() {
        Utility.setStatusBarGradiant(this);

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
                Intent intent = new Intent(NoMatchesActivity.this, HomeFindingPeopleActivity.class);
                startActivity(intent);
                finish();
            }
        });

        profile_icon = (ImageView) findViewById(R.id.profile_icon);
        profile_icon.setOnClickListener(new View.OnClickListener() {
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
