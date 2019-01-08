package com.xgame.chat.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xgame.R;
import com.xgame.base.GameProvider;

import java.util.ArrayList;

public class GamePicker extends FrameLayout {

    public interface OnGameSelectedListener {
        void onSelected(int gameId);
    }

    private static final int GAMES_PER_PAGE = 8;

    private ArrayList<GameProvider.GameProfile> mGames;
    private OnGameSelectedListener mOnSelectedListener;

    private ViewPager mViewPager;
    private GameAdapter mGameAdapter;
    private LinearLayout mIndicators;

    private ImageView mErrorImage;
    private TextView mErrorHintText;

    private int mPageCount;

    public GamePicker(@NonNull Context context) {
        super(context);
        init();
    }

    public GamePicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Context context = getContext();

        mViewPager = new ViewPager(context);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                GamePicker.this.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        int indicatorHeight = getResources().getDimensionPixelOffset(R.dimen.chat_game_picker_indicator_height);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, indicatorHeight);
        addView(mViewPager, layoutParams);

        mGameAdapter = new GameAdapter();
        mViewPager.setAdapter(mGameAdapter);

        mIndicators = new LinearLayout(context);
        mIndicators.setGravity(Gravity.CENTER_HORIZONTAL);
        layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, indicatorHeight);
        layoutParams.gravity = Gravity.BOTTOM;
        addView(mIndicators, layoutParams);
    }

    public void setGames(ArrayList<GameProvider.GameProfile> games) {
        mGames = games;
        layoutForNewData();
    }

    private void layoutForNewData() {

        Context context = getContext();
        int gameNum = mGames.size();
        mPageCount = gameNum / GAMES_PER_PAGE + (gameNum % GAMES_PER_PAGE == 0 ? 0 : 1);
        mIndicators.removeAllViews();
        for (int i = 0; i < mPageCount; i++) {
            ImageView view = new ImageView(context);
            view.setBackgroundResource(R.drawable.game_picker_indicator);
            mIndicators.addView(view);
        }

        mGameAdapter.notifyDataSetChanged();

        if (gameNum == 0) {
            showErrorHint();
        } else {
            hideErrorHint();
        }
    }

    private void showErrorHint() {
        if (mErrorImage == null) {
            Resources res = getResources();

            mErrorImage = new ImageView(getContext());
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            mErrorImage.setImageResource(R.drawable.game_picker_no_data_hint);
            addView(mErrorImage, layoutParams);

            mErrorHintText = new TextView(getContext());
            mErrorHintText.setText(R.string.chat_game_picker_no_data_hint);
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            layoutParams.setMargins(0, 0, 0,
                    res.getDimensionPixelOffset(R.dimen.chat_game_picker_no_data_hint_bottom_margin));
            addView(mErrorHintText, layoutParams);
        }

        mErrorHintText.setVisibility(View.VISIBLE);
        mErrorImage.setVisibility(View.VISIBLE);
    }

    private void hideErrorHint() {
        if (mErrorImage != null) {
            mErrorHintText.setVisibility(View.GONE);
            mErrorImage.setVisibility(View.GONE);
        }
    }


    private void onPageSelected(int position) {
        int childCount = mIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mIndicators.getChildAt(i);
            child.setSelected(i == position);
        }
    }

    public void setOnGameSelectedListener(OnGameSelectedListener listener) {
        mOnSelectedListener = listener;
    }

    private void onGameSelected(int gameId) {
        if (mOnSelectedListener != null) {
            mOnSelectedListener.onSelected(gameId);
        }
    }

    private class GameAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = getContext();
            GamePanel gamePanel = new GamePanel(context);
            int gameStartIndex = position * GAMES_PER_PAGE;
            int gameEndIndex = (position + 1)  * GAMES_PER_PAGE - 1;
            if (gameEndIndex >= mGames.size()) {
                gameEndIndex = mGames.size() - 1;
            }
            for (int i = gameStartIndex; i <= gameEndIndex; i++) {
                GameItemView itemView = new GameItemView(context);
                itemView.attachGameInfo(mGames.get(i));

                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GameProvider.GameProfile gameInfo = (GameProvider.GameProfile) view.getTag();
                        onGameSelected(Integer.valueOf(gameInfo.id));
                    }
                });

                gamePanel.addView(itemView);
            }

            container.addView(gamePanel);
            return gamePanel;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return mPageCount;
        }
    }

    private static class GamePanel extends ViewGroup {
        private static final int CHILD_PER_ROW = 4;
        private static final int ROW_COUNT = 2;

        private int mChildWidth;
        private int mChildHeight;

        public GamePanel(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int start, int top, int end, int bottom) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                child.layout(getPaddingStart() + (i % CHILD_PER_ROW) * mChildWidth,
                        getPaddingTop() + (i / CHILD_PER_ROW) * mChildHeight,
                        getPaddingStart() + (i % CHILD_PER_ROW + 1) * mChildWidth,
                        getPaddingTop() + (i / CHILD_PER_ROW + 1) * mChildHeight);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int width = getMeasuredWidth();
            int height = getMeasuredHeight();

            mChildWidth = (width - getPaddingStart() - getPaddingStart()) / CHILD_PER_ROW;
            mChildHeight = (height - getPaddingTop() - getPaddingBottom()) / ROW_COUNT;

            int childWidthSpec = MeasureSpec.makeMeasureSpec(mChildWidth, MeasureSpec.EXACTLY);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(mChildHeight, MeasureSpec.EXACTLY);

            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                child.measure(childWidthSpec, childHeightSpec);
            }
        }
    }

    private static class GameItemView extends FrameLayout {
        private ImageView imageView;
        private TextView nameText;

        public GameItemView(@NonNull Context context) {
            super(context);

            inflate(context, R.layout.game_picker_item, this);

            imageView = findViewById(R.id.image);
            nameText = findViewById(R.id.name);
        }

        public void attachGameInfo(GameProvider.GameProfile info) {
            Glide.with(getContext()).load(info.icon).into(imageView);
            nameText.setText(info.name);
            setTag(info);
        }
    }
}
