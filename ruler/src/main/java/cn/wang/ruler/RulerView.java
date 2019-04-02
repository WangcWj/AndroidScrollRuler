package cn.wang.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created to :
 * 1.遇到的问题是怎么让他滚动?
 * 2.滚动的时候绘制的内容不会错乱掉?
 * 3.怎么判断滑动的边界?
 * 4.怎么让它Fling?
 * 5.fling之后怎么去判断中间坐标的位置?
 *
 * GitHub -> https://github.com/WangcWj/AndroidScrollRuler
 * 提交issues联系作者.
 *
 * @author WANG
 * @date 2019/3/21
 */
public class RulerView extends View implements ScrollChange {

    /**
     * 帮助类,帮助计算或者存储一些数据.
     */
    private RulerHelper mRulerHelper;

    /**
     * 这个 负责平缓滑动
     */
    private Scroller mScroller;

    /**
     * 每次滑到长刻度的时候 会调用一次
     */
    private ScrollSelected scrollSelected;

    /**
     * ...自行百度一下
     */
    private VelocityTracker velocityTracker;

    /**
     * 花刻度的Paint
     */
    private Paint mLinePaint;

    /**
     * 画文本的
     */
    private Paint mTextPaint;

    /**
     * 每个刻度之间的间距 dp
     */
    private int mLineSpace;

    /**
     * 小刻度的高度 dp
     */
    private int mSmallLineHeight;

    /**
     * 长刻度的高度 dp
     */
    private int mLongLineHeight;

    /**
     * 每根线的宽度 dp
     */
    private int mLineWidth;

    /**
     * 每个矩形用一个Rect去画,因为之前用line去画的话发现宽度会减少一半.
     */
    private Rect mRect;

    /**
     * 一个下标计数器,从集合中取出每个下标对应的Text值.
     */
    private int mTextIndex = 0;

    /**
     * 所有刻度+padding+margin的总px长度.
     */
    private int mCountWidth;

    /**
     * 出去当前屏幕之外的剩余的px长度.
     */
    private int mCountRange = -1;

    /**
     * 也就是过滤一下 不会那么频繁的去调用而已.
     */
    private float mPreDistance = -1;

    /**
     * 说明现在的操作是手指抬起之后.
     */
    private boolean mPressUp = false;

    /**
     * 表示目前处于fling的滑动中.
     */
    private boolean isFling = false;

    /**
     * x轴的最小速率.
     */
    private int mMinVelocity;

    /**
     * 点击事件的初始x坐标.
     */
    float startX;

    private int mMarginLeft = -1;
    private int mPaddingRight = -1;
    private int dpFor05;
    private int dpFor14;

    public RulerView(Context context) {
        super(context);
        init(context);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        initDistanceForDp();
        mRulerHelper = new RulerHelper(this);
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(dp2px(10));
        mTextPaint.setColor(Color.parseColor("#cccccc"));
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setColor(Color.parseColor("#dddddd"));

        mRect = new Rect();
        mScroller = new Scroller(context);

        velocityTracker = VelocityTracker.obtain();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mMinVelocity = viewConfiguration.getScaledMinimumFlingVelocity();

    }

    private void initDistanceForDp() {
        mLineWidth = dp2px(1);
        mLineSpace = dp2px(5);
        mSmallLineHeight = dp2px(4);
        mLongLineHeight = dp2px(9);
        dpFor05 = dp2px(0.5f);
        dpFor14 = dp2px(14);
    }

    /**
     * 设置刻度尺的范围, 比如: 100 - 2000
     * @param start
     * @param end
     */
    public void setScope(int start, int end,int offSet) {
        mRulerHelper.setScope(start, end,offSet);
        int counts = mRulerHelper.getCounts();
        mCountWidth = counts * mLineSpace + counts * mLineWidth;
        invalidate();
    }

    /**
     * 设置当前刻度初始化的位置 你也可以自定义
     * @param text
     */
    public void setCurrentItem(String text) {
        mRulerHelper.setCurrentItem(text);
    }

    /**
     * 获取当前指针停留的那个长刻度的位置.
     * @return
     */
    public String getCurrentText() {
        return mRulerHelper.getCurrentText();
    }

    /**
     * 设置滑动选中监听,滑动中不会有监听,有需要可以自行添加.
     * @param scrollSelected
     */
    public void setScrollSelected(ScrollSelected scrollSelected) {
        this.scrollSelected = scrollSelected;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.AT_MOST) {
            heightSize = dp2px(54);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRulerHelper.getCounts() > 0) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
            if (mMarginLeft == -1 || mCountRange == -1) {
                if (null != layoutParams) {
                    mMarginLeft = layoutParams.leftMargin;
                    mPaddingRight = layoutParams.rightMargin;
                }
                mCountRange = mCountWidth - getWidth() + mMarginLeft + mPaddingRight;
                mRulerHelper.setCenterPoint(getWidth() / 2);
            }
            drawRuler(canvas);
        }
    }

    /**
     * 画刻度
     * @param canvas
     */
    private void drawRuler(Canvas canvas) {
        mTextIndex = 0;
        for (int index = 0; index <= mRulerHelper.getCounts(); index++) {
            boolean longLine = mRulerHelper.isLongLine(index);
            int lineCount = mLineWidth * index;
            mRect.left = index * mLineSpace + lineCount + mMarginLeft;
            mRect.top = getStartY(longLine);
            mRect.right = mRect.left + mLineWidth;
            mRect.bottom = getEndY();
            if (longLine) {
                if (!mRulerHelper.isFull()) {
                    mRulerHelper.addPoint(mRect.left);
                }
                String text = mRulerHelper.getTextByIndex(mTextIndex);
                mTextIndex++;
                canvas.drawText(text, mRect.centerX(), getMeasuredHeight() - dpFor14, mTextPaint);
            }
            canvas.drawRect(mRect, mLinePaint);
            mRect.setEmpty();
        }
    }

    /**
     * 首先你得知道矩形是怎么画的,left right top  bottom.
     * 了解之后就明白这个值是怎么计算的了.
     * @param isLong
     * @return
     */
    private int getStartY(boolean isLong) {
        if (isLong) {
            return getMeasuredHeight() - mLongLineHeight - dpFor05;
        } else {
            return getMeasuredHeight() - mSmallLineHeight - dpFor05;
        }
    }

    /**
     * 跟上面一样的道理
     * @return
     */
    private int getEndY() {
        return getMeasuredHeight() - dpFor05;
    }

    /**
     * event.getRawX() 是点击的view相对于屏幕左上角(0,0)的x坐标.
     * event.getX()  是点击的view相对于view本身的左上角(0,0)的x坐标.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mPressUp = false;
                isFling = false;
                startX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                mPressUp = false;
                float distance = event.getX() - startX;
                if (mPreDistance != distance) {
                    doScroll((int) -distance, 0, 0);
                    invalidate();
                }
                startX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mPressUp = true;
                velocityTracker.computeCurrentVelocity(1000);
                float xVelocity = velocityTracker.getXVelocity();
                if (Math.abs(xVelocity) >= mMinVelocity) {
                    isFling = true;
                    int finalX = mScroller.getCurrX();
                    int centerPointX = mRulerHelper.getCenterPointX();
                    int velocityX = (int) (xVelocity * 0.35);
                    mScroller.fling(finalX, 0, -velocityX, 0, -centerPointX, mCountRange + centerPointX, 0, 0);
                    invalidate();
                } else {
                    isFling = false;
                    scrollFinish();
                }
                velocityTracker.clear();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 滑动停止之后重新定位
     */
    public void scrollFinish() {
        int finalX = mScroller.getFinalX();
        int centerPointX = mRulerHelper.getCenterPointX();
        int currentX = centerPointX + finalX;
        int scrollDistance = mRulerHelper.getScrollDistance(currentX);
        if (0 != scrollDistance) {
            //第一个参数是滚动开始时的x的坐标
            //第二个参数是滚动开始时的y的坐标
            //第三个参数是在X轴上滚动的距离, 负数向右滚动.
            //第四个参数是在Y轴上滚动的距离,负数向下滚动.
            mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), -scrollDistance, 0, 300);
            invalidate();
            if (scrollSelected != null) {
                scrollSelected.selected(getCurrentText());
            }
        }
    }

    private void doScroll(int dx, int dy, int duration) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy, duration);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX() && mPressUp && isFling) {
                mPressUp = false;
                isFling = false;
                scrollFinish();
            }
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        }
        super.computeScroll();
    }

    /**
     * 回收一些资源吧,在{@link View#onDetachedFromWindow()}
     */
    public void destroy(){
        velocityTracker.recycle();
        velocityTracker = null;
        mRulerHelper.destroy();
        scrollSelected = null;
    }

    private int dp2px(float dp) {
        return (int) (getContext().getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    /**
     * 因为设置网络数据的时候有个延迟,然后设置范围数值之后View需要重新绘制一下,
     * 等绘制结束之后才能做一些滑动的操作,所以这里面的监听就是绘制结束之后做的监听.
     *
     * @param distance
     */
    @Override
    public void startScroll(int distance) {
        doScroll(-distance, 0, 300);
        postInvalidate();
    }
}
