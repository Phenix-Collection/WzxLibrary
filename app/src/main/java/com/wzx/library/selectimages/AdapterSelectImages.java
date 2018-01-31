package com.wzx.library.selectimages;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wzx.library.R;
import com.wzx.library.util.DisplayUtil;
import com.wzxlib.hfrecyview.DefaultHeaderFooterRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * Created by wangzixu on 2018/1/29.
 */
public class AdapterSelectImages extends DefaultHeaderFooterRecyclerViewAdapter<AdapterSelectImages.ViewHolder> {
    private ArrayList<BeanSelectImages> mData = new ArrayList<>();
    private Activity mContext;
    private int mItemH;

    public AdapterSelectImages(Activity context, ArrayList<BeanSelectImages> data) {
        mData = data;
        mContext = context;
        mItemH = (mContext.getResources().getDisplayMetrics().widthPixels - DisplayUtil.dip2px(mContext, 2)) / 3;
    }

    @Override
    protected int getHeaderItemCount() {
        return 0;
    }

    @Override
    protected int getContentItemCount() {
        return mData.size();
    }

    @Override
    protected ViewHolder onCreateHeaderItemViewHolder(ViewGroup parent, int headerViewType) {
        return null;
    }

    @Override
    protected ViewHolder onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_selectimages_item, parent, false);
        SelectImageItemHolder holder = new SelectImageItemHolder(view);
        return holder;
    }

    @Override
    protected void onBindHeaderItemViewHolder(ViewHolder headerViewHolder, int position) {
        headerViewHolder.renderView(position);
    }

    @Override
    protected void onBindContentItemViewHolder(ViewHolder contentViewHolder, int position) {
        contentViewHolder.renderView(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void renderView(int position) {
        }
    }

    class SelectImageItemHolder extends ViewHolder {
        public ImageView mImageView;
        public View mNumView;
        public TextView mTvNum;
        BeanSelectImages mBean;

        public SelectImageItemHolder(View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.image);
            mNumView = itemView.findViewById(R.id.rl_pickimage_item);
            mTvNum = itemView.findViewById(R.id.tv_pickimage_item);
        }

        @Override
        public void renderView(int position) {
            ViewGroup.LayoutParams params = mImageView.getLayoutParams();
            if (params != null) {
                params.height = mItemH;
            }
            Log.d("wangzixu", "renderView heigh = " + params.height + ", w  = " + params.width);
            mImageView.setLayoutParams(params);

            mBean = mData.get(position);
            Glide.with(mContext).load(mBean.path).into(mImageView);
        }
    }
}
