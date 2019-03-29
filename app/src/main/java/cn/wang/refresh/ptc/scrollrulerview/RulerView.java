package cn.wang.refresh.ptc.scrollrulerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
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
 *
 * @author WANG
 * @date 2019/3/21
 */
public class RulerView extends View implements ScrollChange {

    private RulerHelper mRulerHelper;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private int mLineSpace;
    private int mSmallLineHeight;
    private int mLongLineHeight;
    private int mLineWidth;
    private int dpFor05;
    private int dpFor14;
    private Rect mRect;
    private int currentIndex = 0;
    private Scroller mScroller;
    private int mCountWidth;
    private int mMarginLeft = -1;
    private int mPaddingRight = -1;
    private int mCountRange = -1;
    private float mPreDistance = -1;
    private ScrollSelected scrollSelected;
    private boolean mPressUp = false;
    private boolean mPressDown = false;
    private boolean isFlaging = false;
    private int mMinVelocity;

    private VelocityTracker velocityTracker;


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
        mRulerHelper = new RulerHelper(this);
        initDistanceForDp();
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

    public void setScope(int start, int end) {
        mRulerHelper.setScope(start, end);
        int counts = mRulerHelper.getCounts();
        mCountWidth = counts * mLineSpace + counts * mLineWidth;
        invalidate();
    }

    public void setCurrentItem(String text) {
        mRulerHelper.setCurrentItem(text);
    }

    public String getCurrentText() {
        return mRulerHelper.getCurrentText();
    }

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

    private void drawRuler(Canvas canvas) {
        currentIndex = 0;
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
                String text = mRulerHelper.getTextByIndex(currentIndex);
                currentIndex++;
                canvas.drawText(text, mRect.centerX(), getMeasuredHeight() - dpFor14, mTextPaint);
            }
            canvas.drawRect(mRect, mLinePaint);
            mRect.setEmpty();
        }
    }

    private int getStartY(boolean isLong) {
        if (isLong) {
            return getMeasuredHeight() - mLongLineHeight - dpFor05;
        } else {
            return getMeasuredHeight() - mSmallLineHeight - dpFor05;
        }
    }

    private int getEndY() {
        return getMeasuredHeight() - dpFor05;
    }

    float startX;

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
                mPressDown = true;
                mPressUp = false;
                isFlaging = false;
                startX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mPressDown) {
                    mPressUp = false;
                    float distance = event.getX() - startX;
                    if (mPreDistance != distance) {
                        doScroll((int) -distance, 0, 0);
                        invalidate();
                    }
                    startX = event.getX();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mPressUp = true;
                mPressDown = false;
                velocityTracker.computeCurrentVelocity(1000);
                float xVelocity = velocityTracker.getXVelocity();
                if (Math.abs(xVelocity) >= mMinVelocity) {
                    isFlaging = true;
                    int finalX = mScroller.getCurrX();
                    int centerPointX = mRulerHelper.getCenterPointX();
                    int velocityX = (int) (xVelocity * 0.35);
                    Log.e("WANG","RulerView.fling........."+centerPointX );
                    mScroller.fling(finalX, 0, -velocityX, 0, -centerPointX, mCountRange + centerPointX, 0, 0);
                    invalidate();
                } else {
                    isFlaging = false;
                    Log.e("WANG", "RulerView.onTouchEvent.");
                    scrollFinish();
                }
                velocityTracker.clear();
                break;
            default:
                break;
        }
        return true;
    }

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
            if (mScroller.getCurrX() == mScroller.getFinalX() && mPressUp && isFlaging) {
                mPressUp = false;
                isFlaging = false;
                scrollFinish();
            }
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        }
        super.computeScroll();
    }

    @Override
    protected void onDetachedFromWindow() {
        velocityTracker.recycle();
        velocityTracker = null;
        super.onDetachedFromWindow();
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
