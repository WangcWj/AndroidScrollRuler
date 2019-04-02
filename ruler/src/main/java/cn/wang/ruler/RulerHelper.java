package cn.wang.ruler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created to : {@link RulerView}的帮助类
 *
 * GitHub -> https://github.com/WangcWj/AndroidScrollRuler
 * 提交issues联系作者.
 *
 * @author WANG
 * @date 2019/3/21
 */
public class RulerHelper {

    private int currentLine = -1;
    private int offSet = 500;
    private int lineNumbers;
    private List<Integer> mPoints;
    private int mCenterPointX;
    private ScrollChange scrollChange;

    private String currentText;

    private List<String> texts;

    public RulerHelper(ScrollChange scrollChange) {
        texts = new ArrayList<>();
        mPoints = new ArrayList<>();
        this.scrollChange = scrollChange;
    }

    public int getCounts() {
        return lineNumbers;
    }

    public String getCurrentText() {
        return currentText;
    }

    public void setCenterPoint(int mCenterPoint) {
        this.mCenterPointX = mCenterPoint;
    }

    public boolean isLongLine(int index) {
        int lineIndex = index / 10;
        if (currentLine != lineIndex) {
            currentLine = lineIndex;
            return true;
        }
        return false;
    }

    public void setScope(int start, int count,int offSet) {
        if(offSet != 0) {
            this.offSet = offSet;
        }
        lineNumbers = (count - start) / (this.offSet / 10);
        for (int i = start; i <= count; i += this.offSet) {
            texts.add(String.valueOf(i));
        }
    }

    public String getTextByIndex(int index) {
        if (index < 0 || index >= texts.size()) {
            return "";
        }
        return texts.get(index);
    }

    public int getCenterPointX() {
        return mCenterPointX;
    }

    public void setCurrentItem(String text) {
        setCurrentText(text);
    }

    public void setCurrentText(int index) {
        if (index >= 0 && index < texts.size()) {
            this.currentText = texts.get(index);
        }
    }


    public void setCurrentText(String currentText) {
        this.currentText = currentText;
    }

    public int getScrollDistance(int x) {
        for (int i = 0; i < mPoints.size(); i++) {
            int pointX = mPoints.get(i);
            if (0 == i && x < pointX) {
                //当前的x比第一个位置的x坐标都小 也就是需要往右移动到第一个长线的位置.
                setCurrentText(0);
                return x - pointX;
            } else if (i == mPoints.size() - 1 && x > pointX) {
                //当前的x比最后一个左边的x都大,也就是需要往左移动到最后一个长线位置.
                setCurrentText(texts.size() - 1);
                return x - pointX;
            } else {
                if (i + 1 < mPoints.size()) {
                    int nextX = mPoints.get(i + 1);
                    if (x > pointX && x <= nextX) {
                        int distance = (nextX - pointX) / 2;
                        int dis = x - pointX;
                        if (dis > distance) {
                            //说明往下一个移动
                            setCurrentText(i + 1);
                            return x - nextX;
                        } else {
                            setCurrentText(i);
                            //往前一个移动
                            return x - pointX;
                        }
                    }
                }
            }
        }
        return 0;
    }

    public void addPoint(int x) {
        mPoints.add(x);
        if (mPoints.size() == texts.size() && null != scrollChange) {
            int index = texts.indexOf(currentText);
            if (index < 0) {
                return;
            }
            int currentX = mPoints.get(index);
            if (currentX < 0) {
                return;
            }
            scrollChange.startScroll(mCenterPointX - currentX);
        }
    }

    public boolean isFull() {
        return texts.size() == mPoints.size();
    }

    public void destroy(){
        mPoints.clear();
        texts.clear();
        mPoints = null;
        texts = null;
        scrollChange = null;
    }
}
