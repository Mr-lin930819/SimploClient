package com.localhost.lin.simploc.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Lin on 2015/11/20.
 */
public class NoneScrollListView extends ListView {

    public NoneScrollListView(Context context) {
        super(context);
    }

    public NoneScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoneScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
