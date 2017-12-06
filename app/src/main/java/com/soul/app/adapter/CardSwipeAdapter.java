package com.soul.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.soul.app.R;
import com.soul.app.interfaces.PagerListener;
import com.soul.app.models.res.UserListRes;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by techahead on 30/6/16.
 */
public class CardSwipeAdapter extends BaseAdapter implements PagerListener {


    // public List<String> matchList;
    public Context context;
    ViewHolder viewHolder;
    List<UserListRes.DataBean> mData;
    private LayoutInflater inflater;
    //  CardSwipePagerAdapter mCardSwipePagerAdapter;
    private LinkedHashMap<Integer, ArrayList<String>> mLMap = new LinkedHashMap<Integer, ArrayList<String>>();
    // private ImageView imgView;
    // int count;

    public CardSwipeAdapter(List<UserListRes.DataBean> mData, Context context) {
        this.mData = mData;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;


        if (rowView == null) {
            rowView = inflater.inflate(R.layout.row_swipe_view, parent, false);
            // configure view holder
            viewHolder = new ViewHolder();

            viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.cardImage);
            rowView.setTag(viewHolder);

            //  this.imgView = viewHolder.cardImage;

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mData.get(position).getProfile_pic().contains("http")) {
            // String profilePic = mData.get(position).getProfile_pic().replace("http", "https");
            mData.get(position).getProfile_pic().replace("http", "https");
        }
        Glide.with(context).load(mData.get(position).getProfile_pic()).into(viewHolder.cardImage);

        //viewHolder.cardImage.setTag(mData.get(position).getUser_id());

        return rowView;
    }

    @Override
    public void setCurrentPos(Context context, int pos) {
//        Glide.with(context).load(matchList.get(pos).getmImageList().get(pos).getImagePath()).into(viewHolder.cardImage);
    }


    //  public void setImage(String image) {
    //     Picasso.with(context).load(image).placeholder(R.drawable.home_placeholder).into(viewHolder.cardImage);
    //  }

    public static class ViewHolder {
        public ImageView cardImage;

    }
}

