package com.ak.ta.salaamswipe.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ak.ta.salaamswipe.R;
import com.ak.ta.salaamswipe.adapter.InterestRecycleViewAdapter;
import com.ak.ta.salaamswipe.application.ApplicationController;
import com.ak.ta.salaamswipe.models.req.GeneralReq;
import com.ak.ta.salaamswipe.models.res.InterestListRes;
import com.ak.ta.salaamswipe.models.res.ListResp;
import com.ak.ta.salaamswipe.utils.Constants;
import com.ak.ta.salaamswipe.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EditInterestActivity extends BaseActivity {

    private FrameLayout headerEditProfile;
    private ImageView backIcon;
    private TextView editProfileDone;
    //  private String mUsrInterest = "";
    private List<InterestListRes> interestList = new ArrayList<InterestListRes>();
    private String mInterest;
    private InterestRecycleViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<String> mInterestList = new ArrayList<String>();

    @Override
    public int setLayout() {
        return R.layout.activity_edit_interest;
    }

    @Override
    public void initUi() {

        headerEditProfile = (FrameLayout) findViewById(R.id.edit_profile_header);
        headerEditProfile.setVisibility(View.VISIBLE);

        mInterestList = getIntent().getStringArrayListExtra(Constants.EXTRA_INTEREST_LIST);

        ((TextView) findViewById(R.id.eph_edit_profile)).setText(getResources().getString(R.string.lbl_interest));

        backIcon = (ImageView) findViewById(R.id.eph_back_icon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editProfileDone = (TextView) findViewById(R.id.eph_done);
        editProfileDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInterestApi();

            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.interest_rclv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        callInterestListApi();

    }

    @Override
    public String getName() {
        return null;
    }

    public void callInterestListApi() {
        if (ApplicationController.getApplicationInstance().isNetworkConnected()) {
            GeneralReq generalReq = new GeneralReq();

            generalReq.setUser_id(PrefUtils.getSharedPrefString(this, PrefUtils.USER_ID));
            Call<ListResp<InterestListRes>> call = mApis.getInterestList(generalReq);
            showProgressDialog(true);
            call.enqueue(new Callback<ListResp<InterestListRes>>() {


                @Override
                public void onResponse(Call<ListResp<InterestListRes>> call, Response<ListResp<InterestListRes>> response) {
                    if (response.isSuccessful()) {
                        showProgressDialog(false);
                        interestList.clear();
                        interestList = response.body().getData();
                        setPreInterest();
                        mAdapter = new InterestRecycleViewAdapter(EditInterestActivity.this, interestList, mInterestList.size());
                        mRecyclerView.setAdapter(mAdapter);


                    } else {
                        showProgressDialog(false);
                    }
                }

                @Override
                public void onFailure(Call<ListResp<InterestListRes>> call, Throwable t) {
                    showProgressDialog(false);
                }
            });
        }
    }


    public GeneralReq getUserReq() {
        GeneralReq generalReq = new GeneralReq();

        ArrayList<GeneralReq.IntrestsEntity> ieList = new ArrayList<GeneralReq.IntrestsEntity>();
        generalReq.setUser_id(PrefUtils.getSharedPrefString(this, PrefUtils.USER_ID));
        mInterest = "";

        for (int i = 0; i < interestList.size(); i++) {
            if (interestList.get(i).isSelected()) {
                GeneralReq.IntrestsEntity intrestsEntity = new GeneralReq.IntrestsEntity();
                intrestsEntity.setCategory_id(interestList.get(i).getCategory_id());
                mInterest = mInterest + interestList.get(i).getCategory_name();
                if (i < interestList.size() - 1) {
                    mInterest = mInterest + ", ";
                }

                if (interestList.get(i).getIs_primary().equals("1")) {
                    intrestsEntity.setIs_primary("1");
                } else {
                    intrestsEntity.setIs_primary("0");
                }
                ieList.add(intrestsEntity);
            }
        }
        generalReq.setIntrests(ieList);

        return generalReq;
    }

    public void userInterestApi() {
        if (ApplicationController.getApplicationInstance().isNetworkConnected()) {
            showProgressDialog(true);
            Call<ListResp<InterestListRes>> call = mApis.getUserInterest(getUserReq());
            final Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_INTEREST, mInterest);
            call.enqueue(new Callback<ListResp<InterestListRes>>() {


                @Override
                public void onResponse(Call<ListResp<InterestListRes>> call, Response<ListResp<InterestListRes>> response) {
                    if (response.isSuccessful()) {
                        showProgressDialog(false);
                        if (response.body() != null) {

                            //  DialogUtils.showToast(EditInterestActivity.this, response.body().getMsg());
                            setResult(Constants.RESULT_INTEREST, intent);
                            finish();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ListResp<InterestListRes>> call, Throwable t) {
                    showProgressDialog(false);
                }
            });
        }
    }

    public void setPreInterest() {
        for (int i = 0; i < interestList.size(); i++) {
            for (int j = 0; j < mInterestList.size(); j++) {
                if (mInterestList.get(j).equals(interestList.get(i).getCategory_name())) {
                    interestList.get(i).setSelected(true);
                }
            }
        }
    }

}

