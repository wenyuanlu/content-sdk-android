package com.maishuo.haohai.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FlowLayoutView extends ViewGroup {
    //靠左放置标签
    public static final int START_FROM_LEFT   = 1;
    //居中放置标签
    public static final int START_FROM_CENTER = 0;
    //靠右放置标签
    public static final int START_FROM_RIGHT  = -1;
    //默认居中展示
    private       int        lineMode          = START_FROM_CENTER;
    //每个item横向间距
    private final int        horizontalSpacing = dp2px(16);
    //每个item纵向间距
    private final int        verticalSpacing   = dp2px(16);
    // 行集合
    private final List<Line> lines             = new ArrayList<>();
    // 当前的行
    private       Line       line;
    // 当前行使用的空间
    private       int        lineUsedSize      = 0;

    public void setDefaultDisplayMode (int lineMode) {
        this.lineMode = lineMode;
    }

    public FlowLayoutView(Context context) {
        super(context);
    }

    public FlowLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 计算出所有子控件的宽和高，从而确定当前父布局的宽和高
     */
    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 实际可以用的宽和高(去除 padding 内边距)
        int width      = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height     = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingTop();
        int widthMode  = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // Line初始化
        resetLine();
        initLine();

        for (int count = getChildCount(), i = 0; i < count; i++) {
            View child = getChildAt(i);
            // 测量所有的childView
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                    widthMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : widthMode);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                    heightMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : heightMode);
            //也可以 measureChild(child, childWidthMeasureSpec, childHeightMeasureSpec);
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            // 计算当前行已使用的宽度
            int measuredWidth = child.getMeasuredWidth();
            // 如果添加进去后宽度超过可用的宽度，需要换行，否则childView继续添加到当前的行上
            if (lineUsedSize + measuredWidth > width) {
                // 先换行，先将上一行保存到 lines 集合中，再换行
                saveAndNewLine();
            }
            //存储当前行已使用的宽度
            line.setUsedLineSize(lineUsedSize += measuredWidth + horizontalSpacing);
            //继续添加到当前行 line
            line.addChild(child);
        }

        // 如果有最后一行（未填满）把它记录到集合中
        if (line != null && !lines.contains(line)) {
            saveAndNewLine();
        }

        // 把所有行的高度加上
        int totalHeight = 0;
        for (Line curLine : lines) {
            totalHeight += curLine.getHeight();
        }
        // 加上行的竖直间距
        totalHeight += verticalSpacing * (lines.size() - 1);
        // 加上上下padding
        totalHeight += getPaddingBottom();
        totalHeight += getPaddingTop();

        /**
         * 设置自身尺寸，设置布局的宽高，宽度直接采用父 View 传递过来的最大宽度，而不用考虑子view是否填满宽度
         * 因为该布局的特性就是填满一行后，再换行。
         * 高度根据设置的模式来决定采用所有子View的高度之和还是采用父view传递过来的高度
         */
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                resolveSize(totalHeight, heightMeasureSpec));
    }

    /**
     * 指定所有childView的位置，调用Line对象中的layout方法。
     */
    @Override
    protected void onLayout (boolean changed, int l, int t, int r, int b) {
        int paddingLeft      = getPaddingLeft();
        int paddingTop       = getPaddingTop();
        int totalUsableWidth = getMeasuredWidth() - paddingLeft - paddingTop;
        for (Line curLine : lines) {
            curLine.layout(lineMode, paddingLeft, paddingTop, totalUsableWidth, horizontalSpacing);
            // 计算下一行 Y 轴起点坐标
            paddingTop = paddingTop + curLine.getHeight() + verticalSpacing;
        }
    }

    /**
     * 与当前ViewGroup对应的LayoutParams
     */
    @Override
    public LayoutParams generateLayoutParams (AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    private void initLine () {
        if (line == null) {
            // 创建新一行
            line = new Line();
        }
    }

    private void resetLine () {
        lines.clear();
        line = new Line();
        lineUsedSize = 0;
    }

    public List<Line> getLines(){
        return lines;
    }

    /**
     * 换行，先将上一行保存到 lines 集合中，再换行
     */
    private void saveAndNewLine () {
        //过滤 当前行最后一个子控件末尾 horizontalSpacing
        if (lineUsedSize > 0) {
            line.setUsedLineSize(lineUsedSize - horizontalSpacing);
        }
        // 把之前的行记录下来加入到行集合中
        if (line != null) {
            lines.add(line);
        }
        //重置已用宽度为0
        lineUsedSize = 0;
        // 创建新的一行
        line = new Line();
    }

    public static class Line {
        // 子控件集合
        private final List<View> childList    = new ArrayList<>();
        // 行高
        private       int        height;
        // 当前行已经使用的宽度
        private       int        lineUsedSize = 0;

        /**
         * 添加childView
         *
         * @param childView 子控件
         */
        public void addChild (View childView) {
            childList.add(childView);
            // 更新行高为当前最高的一个childView的高度
            if (height < childView.getMeasuredHeight()) {
                height = childView.getMeasuredHeight();
            }
        }

        /**
         * 设置childView的绘制区域
         *
         * @param left 左上角x轴坐标
         * @param top  左上角y轴坐标
         */
        public void layout (int lineMode, int left, int top, int totalUsableWidth, int horizontalSpacing) {
            // 当前childView的左上角x轴坐标
            switch (lineMode) {
                case FlowLayoutView.START_FROM_LEFT:
                    for (View view : childList) {
                        // 设置childView的绘制区域
                        view.layout(left, top,
                                left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
                        // 计算下一个childView的位置
                        left += view.getMeasuredWidth() + horizontalSpacing;
                    }
                    break;

                case FlowLayoutView.START_FROM_CENTER:
                    int square = (horizontalSpacing * (childList.size() - 1) + totalUsableWidth - lineUsedSize)
                            / (childList.size() + 1);
                    for (View view : childList) {
                        // 设置childView的绘制区域
                        left += square;
                        view.layout(left, top,
                                left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
                        // 计算下一个childView的位置
                        left += view.getMeasuredWidth();
                    }
                    break;

                case FlowLayoutView.START_FROM_RIGHT:
                    left += totalUsableWidth - lineUsedSize;
                    if (childList.size() > 0) {
                        for (int index = childList.size() - 1; index >= 0; index--) {
                            // 设置childView的绘制区域
                            childList.get(index).layout(left, top,
                                    left + childList.get(index).getMeasuredWidth(), top + childList.get(index).getMeasuredHeight());
                            // 计算下一个childView的位置
                            left += childList.get(index).getMeasuredWidth() + horizontalSpacing;
                        }
                    }
                    break;
            }

        }

        public int getHeight () {
            return height;
        }

        public int getChildCount () {
            return childList.size();
        }

        public List<View> getChildData () {
            return childList;
        }

        public void setUsedLineSize (int lineUsedSize) {
            this.lineUsedSize = lineUsedSize;
        }
    }

    public static int dp2px (int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
