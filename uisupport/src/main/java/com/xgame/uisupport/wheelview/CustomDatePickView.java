package com.xgame.uisupport.wheelview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.xgame.common.util.PixelUtil;
import com.xgame.uisupport.R;

import java.util.Calendar;

/**
 * Created by zhengjunfei on 16-12-15.
 */

public class CustomDatePickView extends LinearLayout {
    private final int DEFAULT_YEAR = 12;
    private final int DEFAULT_TEXT_PADDING = 16;
    private static final int START_YEAR = 1917;
    private static final int DEFAULT_TEXT_SIZE = 15;

    private Context mContext;
    private WheelView wl_year, wl_month, wl_day;

    private ScrollListener mScrollerListener;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mTextPadding = DEFAULT_TEXT_PADDING;

    public CustomDatePickView(Context context) {
        this(context, null);
    }

    public CustomDatePickView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDatePickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.view_costom_date_picker, this);
        mTextPadding = PixelUtil.dip2px(context, DEFAULT_TEXT_PADDING);
        Calendar c = Calendar.getInstance();
        final int curYear = c.get(Calendar.YEAR);
        final int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
        final int curDay = c.get(Calendar.DAY_OF_MONTH);
        wl_year = (WheelView) findViewById(R.id.wl_year);
        wl_month = (WheelView) findViewById(R.id.wl_month);
        wl_day = (WheelView) findViewById(R.id.wl_day);
        initYearAdapter(curYear);
        initMonthAdapter(DEFAULT_YEAR);

        wl_year.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int oldMaxDay = getMaxDay(mYear,mMonth);
                int oldCurrentItem = wl_day.getCurrentItem();
                mYear = newValue + START_YEAR;
                int newMaxDay = getMaxDay(mYear,mMonth);
                if(mYear == curYear){
                    initMonthAdapter(curMonth);
                    initDayAdapter(newMaxDay);
                    if(oldMaxDay-1 == oldCurrentItem){
                        wl_day.setCurrentItem(newMaxDay-1);
                    }else{
                        wl_day.setCurrentItem(oldCurrentItem);
                    }
                }else{
                    initMonthAdapter(DEFAULT_YEAR);
                    initDayAdapter(getMaxDay(mYear,mMonth));
                    if(oldMaxDay-1 == oldCurrentItem){
                        wl_day.setCurrentItem(getMaxDay(mYear,mMonth)-1);
                    }else{
                        wl_day.setCurrentItem(oldCurrentItem);
                    }
                }
                mScrollerListener.onScrollerListener();
            }
        });

        wl_month.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int oldMaxDay = getMaxDay(mYear,mMonth);
                int oldCurrentItem = wl_day.getCurrentItem();

                mMonth = newValue + 1;
                if(mYear == curYear && mMonth == curMonth){
                    initDayAdapter(curDay);
                    if(oldMaxDay-1 == oldCurrentItem){
                        wl_day.setCurrentItem(curDay-1);
                    }else{
                        wl_day.setCurrentItem(oldCurrentItem);
                    }
                }else{
                    initDayAdapter(getMaxDay(mYear,mMonth));
                    if(oldMaxDay-1 == oldCurrentItem){
                        wl_day.setCurrentItem(getMaxDay(mYear,mMonth)-1);
                    }else{
                        wl_day.setCurrentItem(oldCurrentItem);
                    }
                }
                mScrollerListener.onScrollerListener();
            }
        });

        wl_day.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mScrollerListener.onScrollerListener();
            }
        });

    }

    public void setVisibleItems(int count) {
        wl_year.setVisibleItems(count);
        wl_month.setVisibleItems(count);
        wl_day.setVisibleItems(count);
    }

    private void initYearAdapter(int maxYear){
        NumericWheelAdapter adapter = new NumericWheelAdapter(mContext, START_YEAR, maxYear);
        adapter.setLabel("年");
        adapter.setTextColor(R.color.black);
        adapter.setTextSize(DEFAULT_TEXT_SIZE);
        adapter.setTextPadding(mTextPadding);
        wl_year.setViewAdapter(adapter);
        wl_year.setCyclic(true);//是否可循环滑动
    }

    private void initMonthAdapter(int month){
        NumericWheelAdapter adapter = new NumericWheelAdapter(mContext, 1, month, "%02d");
        adapter.setLabel("月");
        adapter.setTextColor(R.color.black);
        adapter.setTextSize(DEFAULT_TEXT_SIZE);
        adapter.setTextPadding(mTextPadding);
        wl_month.setViewAdapter(adapter);
        if(month == 1){
            wl_month.setCyclic(false);
        }else{
            wl_month.setCyclic(true);
        }
    }

    private void initDayAdapter(int maxDay){
        NumericWheelAdapter adapter = new NumericWheelAdapter(mContext, 1, maxDay, "%02d");
        adapter.setLabel("日");
        adapter.setTextColor(R.color.black);
        adapter.setTextSize(DEFAULT_TEXT_SIZE);
        adapter.setTextPadding(mTextPadding);
        wl_day.setViewAdapter(adapter);
        if(maxDay == 1){
            wl_day.setCyclic(false);
        }else{
            wl_day.setCyclic(true);
        }
    }

    /**
     * 如果传输了正确的生日就选择生日那一天,如果错误默认选择18年前的今天
     */
    public void setData(String birthday){
        String[] date = birthday.split("-");
        if(date.length == 3){
            try {
                showDate(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
            } catch (Exception e) {
                showErrorDate();
            }
        }else{
            showErrorDate();
        }
    }

    public void setData(int year,int month,int day){
        showDate(year,month,day);
    }

    private void showDate(int year,int month,int day){
        if(year == 0 || month == 0 || day == 0){
            showErrorDate();
        }
        mYear = year;
        mMonth = month;
        mDay = day;

        int maxDay = getMaxDay(mYear,mMonth);
        initDayAdapter(maxDay);

        wl_year.setCurrentItem(year-START_YEAR);
        wl_month.setCurrentItem(month-1);
        wl_day.setCurrentItem(day-1);
    }

    private void showErrorDate(){
        Calendar c = Calendar.getInstance();
        int curYear = c.get(Calendar.YEAR)-18;
        int curMonth = c.get(Calendar.MONTH)+1;//通过Calendar算出的月数要+1
        int curDay = c.get(Calendar.DAY_OF_MONTH);
        showDate(curYear,curMonth,curDay);
    }

    public String getDate(){
        StringBuilder stringBuilder = new StringBuilder();
        int year = wl_year.getCurrentItem() + START_YEAR;//年
        int month = wl_month.getCurrentItem() + 1;//月
        int day = wl_day.getCurrentItem() + 1;//日
        stringBuilder.append(year);
        stringBuilder.append("-");
        if(month < 10){
            stringBuilder.append("0");
        }
        stringBuilder.append(month);
        stringBuilder.append("-");
        if(day < 10){
            stringBuilder.append("0");
        }
        stringBuilder.append(day);
        return stringBuilder.toString();
    }

    public void setScrollerListener(ScrollListener listener){
        this.mScrollerListener = listener;
    }

    public interface ScrollListener{
        void onScrollerListener();
    }

    /**
     * 获取一个月有多少天
     * 闰年2月29天,平年2月28天
     */
    private int getMaxDay(int year,int month){

        if(month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12){
            return 31;
        }else if(month == 4 || month == 6 || month == 9 || month == 11){
            return 30;
        }else{
            if((year % 4 == 0 && year % 100 != 0) || year % 400 == 0){
                return 29;
            }else{
                return 28;
            }
        }
    }




}
