package com.wzx.library.selectimages;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wzx.library.R;
import com.wzx.library.util.DisplayUtil;
import com.wzx.library.util.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wangzixu on 2018/1/27.
 */
public class ActivitySelectImages extends Activity implements View.OnClickListener {
    private ImageView mBack;
    private TextView mFoldername;
    private TextView mConfirm;
    private RecyclerView mRecyview;
    private GridLayoutManager mManager;
    private ArrayList<BeanSelectImages> mData = new ArrayList<>();
    private AdapterSelectImages mAdapter;
    private Handler mHandler = new Handler();
    private int mCurrentPage = 0;
    private ArrayList<ArrayList<BeanSelectImages>> mImgDirs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectimages);
        StatusBarUtil.setStatusBarTransparnet(this);

        initView();
        loadData();
    }

    private void initView() {
        mBack = (ImageView) findViewById(R.id.back);
        mFoldername = (TextView) findViewById(R.id.foldername);
        mConfirm = (TextView) findViewById(R.id.confirm);
        mRecyview = (RecyclerView) findViewById(R.id.recyview);

        mBack.setOnClickListener(this);
        mFoldername.setOnClickListener(this);
        mConfirm.setOnClickListener(this);

        mManager = new GridLayoutManager(this, 3);
        mRecyview.setLayoutManager(mManager);
        mRecyview.setHasFixedSize(true);
        mRecyview.setItemAnimator(new DefaultItemAnimator());


        mRecyview.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final Rect mBounds = new Rect();
            int mDivider;
            private Paint mPaint = new Paint();

            {
                mDivider = DisplayUtil.dip2px(ActivitySelectImages.this, 1);
//                mPaint.setColor(0xff125e5f);
                mPaint.setColor(Color.BLACK);
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View view = parent.getChildAt(i);
                    parent.getDecoratedBoundsWithMargins(view, mBounds);
                    c.drawRect(mBounds.left, mBounds.top, mBounds.left + mDivider, mBounds.bottom-mDivider, mPaint);
                    c.drawRect(mBounds.left, mBounds.bottom - mDivider, mBounds.right, mBounds.bottom, mPaint);
                }
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
                int spanIndex = layoutParams.getSpanIndex();
                if (spanIndex == 0) {
                    outRect.set(0, 0, 0, mDivider);
                } else {
                    outRect.set(mDivider, 0, 0, mDivider);
                }
            }
        });

        mAdapter = new AdapterSelectImages(this, mData);
        mRecyview.setAdapter(mAdapter);

        mRecyview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    if (mHasMoreData && !mIsLoading) {
//                        boolean can = mRecyView.canScrollVertically(1);
//                        if (!can) {
//                            mAdapter.setFooterLoading();
//                            mRecyView.scrollToPosition(mManager.getItemCount() - 1);
//                            loadData(false);
//                        }
//                    }
                }
            }
        });
    }

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getImages();
            }
        }).start();
    }

    public void getImages() {
        String[] projection = { MediaStore.Images.Media._ID
                , MediaStore.Images.Media.DATA
                , MediaStore.Images.Media.WIDTH
                , MediaStore.Images.Media.HEIGHT
                , MediaStore.Images.Media.BUCKET_DISPLAY_NAME};


        ContentResolver contentResolver = ActivitySelectImages.this.getContentResolver();
        Cursor mCursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?", new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED
                + " DESC");

//        Cursor mCursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.MIME_TYPE + "=? or "
//                + MediaStore.Images.Media.MIME_TYPE + "=?", new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED
//                + " DESC limit 200 offset " + mCurrentPage*200);

        if (mCursor != null) {
            HashMap<String, ArrayList<BeanSelectImages>> tempMap = new HashMap<>();
            final ArrayList<BeanSelectImages> tempList = new ArrayList<>();
            try {
                while (mCursor.moveToNext()) {
                    BeanSelectImages bean = new BeanSelectImages();
                    // 获取图片路径
                    String id = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
                    String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String bucketname = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String w = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.WIDTH));
                    String h = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.HEIGHT));
                    if (TextUtils.isEmpty(w) || TextUtils.isEmpty(h)
                            || Integer.valueOf(w) <= 0
                            || Integer.valueOf(h) <= 0) {
                        continue;
                    }
                    Log.d("wangzixu", "loadData mCurrentPage " + mCurrentPage + " bucketname = " + bucketname);
                    bean.width = Integer.valueOf(w);
                    bean.height = Integer.valueOf(h);
                    bean.id = id;
                    bean.path = path;

                    tempList.add(bean);

                    // 获取父文件夹路径
//                            String parentName = new File(path).getParentFile().getName();

                    if (tempMap.containsKey(bucketname)) {
                        tempMap.get(bucketname).add(bean);
                    } else {
                        ArrayList<BeanSelectImages> list = new ArrayList<>();
                        list.add(bean);
                        tempMap.put(bucketname, list);
                        mImgDirs.add(list);
                    }
                }

                if (tempList.size() > 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mData.addAll(tempList);
                            tempList.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    });
//                    mCurrentPage++;
//                    getImages();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mCursor.close();
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.foldername:
                break;
            case R.id.confirm:
                break;
            default:
                break;
        }
    }
}
