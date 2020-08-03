package com.julun.huanque.common.widgets;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.julun.huanque.common.R;
import com.julun.huanque.common.helper.DensityHelper;

import static com.julun.huanque.common.helper.DensityHelper.dp2px;

/**
 * 圆形进度条
 *
 * @author WanZhiYuan
 * @createDate 2019/03/08
 */
public class CircleBarView extends View {

    private Paint mOutsideRingPaint;//绘制最外边线圆弧的画笔
    private Paint mBgRingPaint;//绘制背景圆弧的画笔
    private Paint mProgressRingPaint;//绘制圆弧的画笔

    private RectF mRectF;//绘制圆弧的矩形区域

    private CircleBarAnim anim;

    private float progressNum = 100;//可以更新的进度条数值
    private float maxNum = 100;//进度条最大值
    private long animationTime = 3000L;//动画执行时间,默认3秒

    private float mStartAngle;//背景圆弧的起始角度
    private float mSweepAngle;//背景圆弧扫过的角度
    private float mBarWidth;//圆弧进度条宽度

    private int defaultSize;//自定义View默认的宽高
    private float progressSweepAngle;//进度条圆弧扫过的角度

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }

    private boolean isReverse = false;//是不是反向进度条
    private OnAnimationListener onAnimationListener;

    public CircleBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleBarView);

        int progressColor = typedArray.getColor(R.styleable.CircleBarView_circlebar_progress_color, Color.GREEN);
        int bgColor = typedArray.getColor(R.styleable.CircleBarView_bg_color, Color.GRAY);
        int outsideColor = typedArray.getColor(R.styleable.CircleBarView_outsides_color, Color.TRANSPARENT);
        mStartAngle = typedArray.getFloat(R.styleable.CircleBarView_start_angle, 0);
        mSweepAngle = typedArray.getFloat(R.styleable.CircleBarView_sweep_angle, 360);
        mBarWidth = typedArray.getDimension(R.styleable.CircleBarView_bar_width, dp2px(10));

        typedArray.recycle();

//        progressNum = 0;
        defaultSize = dp2px(100f);
        mRectF = new RectF();

        mProgressRingPaint = new Paint();
        mProgressRingPaint.setStyle(Paint.Style.STROKE);//只描边，不填充
        mProgressRingPaint.setColor(progressColor);
        mProgressRingPaint.setAntiAlias(true);//设置抗锯齿
        mProgressRingPaint.setStrokeWidth(mBarWidth);
//        mProgressRingPaint.setStrokeCap(Paint.Cap.ROUND);//设置画笔为圆角


        mBgRingPaint = new Paint();
        mBgRingPaint.setStyle(Paint.Style.STROKE);//只描边，不填充
        mBgRingPaint.setColor(bgColor);
        mBgRingPaint.setAntiAlias(true);//设置抗锯齿
        mBgRingPaint.setStrokeWidth(mBarWidth);
        mBgRingPaint.setStrokeCap(Paint.Cap.ROUND);

        mOutsideRingPaint = new Paint();
        mOutsideRingPaint.setStyle(Paint.Style.STROKE);//只描边，不填充
        mOutsideRingPaint.setColor(outsideColor);
        mOutsideRingPaint.setAntiAlias(true);//设置抗锯齿
        mOutsideRingPaint.setStrokeWidth(DensityHelper.Companion.px2dp(1));

        anim = new CircleBarAnim();

        setClickable(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = measureSize(defaultSize, heightMeasureSpec);
        int width = measureSize(defaultSize, widthMeasureSpec);
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形

        if (min >= mBarWidth * 2) {
            mRectF.set(mBarWidth / 2, mBarWidth / 2, min - mBarWidth / 2, min - mBarWidth / 2);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int min = Math.min(getWidth(), getHeight());// 获取View最短边的长度
        //获取控件中心坐标点
        int x = min / 2;
        //背景圆
        canvas.drawArc(mRectF, mStartAngle, mSweepAngle, false, mBgRingPaint);
        //进度圆
        canvas.drawArc(mRectF, mStartAngle, progressSweepAngle, false, mProgressRingPaint);
        //最外边线圆
        canvas.drawCircle(x, x, x, mOutsideRingPaint);
    }

    public class CircleBarAnim extends Animation {

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {//interpolatedTime从0渐变成1,到1时结束动画,持续时间由setDuration（time）方法设置
            super.applyTransformation(interpolatedTime, t);
            mPostInvalidate(interpolatedTime);
        }
    }
    public void mPostInvalidate(float interpolatedTime){
        if (isReverse) {
            progressSweepAngle = (interpolatedTime - 1) * mSweepAngle * progressNum / maxNum;
        } else {
            progressSweepAngle = interpolatedTime * mSweepAngle * progressNum / maxNum;
        }
        if (onAnimationListener != null) {
            onAnimationListener.changeProgress(interpolatedTime);
            if (interpolatedTime == 1) {
                onAnimationListener.onFinish();
            }
        }

        postInvalidate();
    }
    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }


    /**
     * 设置进度条最大值
     *
     * @param maxNum
     */
    public void setMaxNum(float maxNum) {
        this.maxNum = maxNum;
    }

    /**
     * 设置进度条最大值
     *
     * @param processNum
     */
    public void setProcessNum(float processNum) {
        this.progressNum = processNum;
    }

    /**
     * 设置进度条数值
     *
     * @param time 动画持续时间
     */
    public void setAnimationTime(long time) {
        animationTime = time;
        anim.setDuration(time);
        this.startAnimation(anim);
    }

    /**
     * 重新开始动画
     */
    public void restartAnimation() {
        anim.cancel();
        anim.setDuration(animationTime);
        this.startAnimation(anim);
    }

    public void setColor(int bgColor, int progressColor, int outsideColor) {
        mBgRingPaint.setColor(bgColor);
        mProgressRingPaint.setColor(progressColor);
        mOutsideRingPaint.setColor(outsideColor);
    }

    public void resetViews(int bgColor, int progressColor, int outsideColor) {
        setColor(bgColor, progressColor, outsideColor);
        restartAnimation();
    }

    public void clear() {
        if (anim != null) {
            anim.cancel();
            anim = null;
        }
        if (onAnimationListener != null) {
            onAnimationListener = null;
        }
    }

    // 没必要置空 弹窗复用会报错
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        if (anim != null) {
//            anim.cancel();
////            anim = null;
//        }
////        if (onAnimationListener != null) {
////            onAnimationListener = null;
////        }
//    }

    public interface OnAnimationListener {

        /**
         * @param interpolatedTime 从0渐变成1,到1时结束动画
         */
        void changeProgress(float interpolatedTime);

        void onFinish();

    }

    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        this.onAnimationListener = onAnimationListener;
    }
}
