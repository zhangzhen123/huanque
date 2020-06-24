package com.julun.huanque.common.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;

import com.julun.huanque.common.R;


/**
 * @Anchor: zhangzhen
 * @Date: 2019/10/10 14:45
 * @Description : 方形进度条 初始位置从矩形正上方中点开始 支持自定义进度条颜色 总条颜色 进度条宽度 进度条距边界横竖距离  进度值 最大值 小圆点展示等等
 */
public class SquareProgress extends View {

    private String TAG = "SquareProgress";
    //各个画笔的颜色
    private int maxColor = Color.GRAY;//总进度条颜色为灰色
    private int curColor = Color.BLUE;//当前进度条颜色为蓝色
    private int dotColor = Color.RED;//进度条前端的小圆点为红色
    private float allLength;//进度条的总长度
    private int maxProgress = 100;//总的进度条长度为100（可改变）
    private int curProgress = 0;//当前进度为30（可改变）
    private Paint curPaint;//当前进度条的画笔
    private Paint maxPaint;//总进度条的画笔
    private Paint dotPaint;//进度条前端小圆点的画笔
    private int width;//整个view的宽度，（包括paddingleft和paddingright）
    private int height;//整个view的高度，（包括paddingtop和paddingbottom）
    private float maxProgressWidth;//整个进度条画笔的宽度
    private float curProgressWidth;//当前进度条画笔的宽度
    private float dotDiameter;//进度条顶端小圆点的直径
    private RectShape maxRectShape;
    private RectShape curRectShape;
    private boolean canDisplayDot = false;//是否显示小圆点
    private Path curPath;//当前进度条的路径，（总的进度条的路径作为onDraw的局部变量）
    private float proWidth;//整个进度条构成矩形的宽度
    private float proHeight;//整个进度条构成矩形的高度
    private float dotCX;//小圆点的X坐标（相对view）
    private float dotCY;//小圆点的Y坐标（相对view）

    private boolean reverse = false;//是不是反向 默认是顺时针方向
    private int xPadding = 0;//进度条x距离边界位置
    private int yPadding = 0;


    public SquareProgress(Context context) {
        super(context);
        initView(null);
    }

    public SquareProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public SquareProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SquareProgress(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(attrs);
    }

    //
    private void initView(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SquareProgress);
            curColor = typedArray.getColor(R.styleable.SquareProgress_sp_progress_color, Color.BLUE);
            maxColor = typedArray.getColor(R.styleable.SquareProgress_sp_max_color, Color.GRAY);
            dotColor = typedArray.getColor(R.styleable.SquareProgress_dot_color, Color.RED);
            curProgress = typedArray.getInt(R.styleable.SquareProgress_sp_progress, curProgress);
            maxProgress = typedArray.getInt(R.styleable.SquareProgress_sp_max_progress, maxProgress);
            curProgressWidth = typedArray.getDimension(R.styleable.SquareProgress_sp_progress_width, dp2Px(10));
            maxProgressWidth = typedArray.getDimension(R.styleable.SquareProgress_sp_progress_width, dp2Px(10));
            dotDiameter = typedArray.getDimension(R.styleable.SquareProgress_dot_diameter, dp2Px(10));
            canDisplayDot = typedArray.getBoolean(R.styleable.SquareProgress_show_dot, false);
            reverse = typedArray.getBoolean(R.styleable.SquareProgress_sp_reverse, false);
            //进度条位置 xy默认至少进度条宽度或者圆点直径的一半
            float minPadding;
            if (canDisplayDot) {
                minPadding = Math.max(dotDiameter, curProgressWidth) / 2;
            } else {
                minPadding = curProgressWidth / 2;
            }
            xPadding = (int) (typedArray.getDimension(R.styleable.SquareProgress_x_padding, 0) + minPadding);
            yPadding = (int) (typedArray.getDimension(R.styleable.SquareProgress_y_padding, 0) + minPadding);

            typedArray.recycle();
        } else {
            curProgressWidth = dp2Px(10);//dp转px
            maxProgressWidth = dp2Px(10);//总的进度条的画笔设置
            dotDiameter = dp2Px(10);//圆点直径
        }

        curPaint = new Paint();//当前进度条的画笔设置
        curPaint.setAntiAlias(true);//设置画笔抗锯齿
        curPaint.setStyle(Paint.Style.STROKE);//设置画笔（忘了）
        curPaint.setStrokeWidth(curProgressWidth);//设置画笔宽度
        curPaint.setColor(curColor);//设置画笔颜色


        maxPaint = new Paint();
        maxPaint.setAntiAlias(true);
        maxPaint.setColor(maxColor);
        maxPaint.setStyle(Paint.Style.STROKE);
        maxPaint.setStrokeWidth(maxProgressWidth);

        dotPaint = new Paint();//小圆点的画笔设置

        dotPaint.setAntiAlias(true);
        dotPaint.setStyle(Paint.Style.FILL);//因为是画圆，所以这里是这种模式
        dotPaint.setColor(dotColor);

        maxRectShape = new RectShape();//没用到
        curRectShape = new RectShape();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = measureWidth(widthMeasureSpec);//得到view的宽度
        height = measureHeight(heightMeasureSpec);//得到view的高度
        setMeasuredDimension(width, height);//将自己重新测量的宽高度应用到视图上（只设置size而不设置mode，mode是在布局中就确定了的）
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int tWidth = width - getPaddingRight() - getPaddingLeft();//得到整个view出去padding后的宽度
        int tHeight = height - getPaddingTop() - getPaddingBottom();//得到整个view除去padding后的高度
//        xPadding = tWidth / 10;
//        yPadding = tHeight / 10;
        int point1X = getPaddingLeft() + tWidth / 2;//得到第一个点的X坐标（相对于view）
        int point1Y = getPaddingTop() + yPadding;
        int point2X = tWidth + getPaddingLeft() - xPadding;
        int point2Y = getPaddingTop() + yPadding;
        int point3X = tWidth + getPaddingLeft() - xPadding;
        int point3Y = tHeight + getPaddingTop() - yPadding;
        int point4X = getPaddingLeft() + xPadding;
        int point4Y = tHeight + getPaddingTop() - yPadding;
        int point5X = getPaddingLeft() + xPadding;//
        int point5Y = getPaddingTop() + yPadding;
//        Log.i(TAG, "onDraw: point1:" + point1X + "," + point1Y);
//        Log.i(TAG, "onDraw: point2:" + point2X + "," + point2Y);
//        Log.i(TAG, "onDraw: point3:" + point3X + "," + point3Y);
//        Log.i(TAG, "onDraw: point4:" + point4X + "," + point4Y);
        proWidth = point3X - point5X;
        proHeight = point3Y - point5Y;
//        Log.i(TAG, "onDraw: point5:" + proWidth + "," + proHeight);
        Path maxpath = new Path();//整个进度条的路径
        maxpath.moveTo(point5X, point5Y);
        maxpath.lineTo(point2X, point2Y);
        maxpath.lineTo(point3X, point3Y);
        maxpath.lineTo(point4X, point4Y);
//        maxpath.lineTo(point5X, point5Y);
        maxpath.close();
        canvas.drawPath(maxpath, maxPaint);
        allLength = 2 * (proWidth + proHeight);
        curPath = new Path();//当前进度条的路径
        curPath.moveTo(point1X, point1Y);
        float curPercent = (float) curProgress / maxProgress;//当前进度占总进度的百分比
        float curLength = allLength * curPercent;//当前的需要显示的长度
        if (curPercent > 0) {
            if (reverse) {
                if (curLength < proWidth / 2) {//处在第一段上面的小圆点的原点坐标和当前进度条的路径
                    //第一阶段 右x x:0  y:w/2-w
                    dotCX = point1X - curLength;
                    dotCY = point1Y;
                    curPath.lineTo(dotCX, dotCY);
                } else if (curLength < (proHeight + proWidth / 2)) {
                    //第二阶段 右y x:w  y:0-h
                    dotCX = point5X;
                    dotCY = point5Y + curLength - proWidth / 2;
                    curPath.lineTo(point5X, point5Y);
                    curPath.lineTo(dotCX, dotCY);
                } else if (curLength < (1.5f * proWidth + proHeight)) {
                    //第三阶段 下x x:0-w  y:h
                    dotCX = point4X + (curLength - proHeight - proWidth / 2);
                    dotCY = point4Y;
                    curPath.lineTo(point5X, point5Y);
                    curPath.lineTo(point4X, point4Y);
                    curPath.lineTo(dotCX, dotCY);
                } else if (curLength < (1.5f * proWidth + proHeight * 2)) {
                    //第四阶段
                    dotCX = point3X;
                    dotCY = point3Y - (curLength - proWidth * 1.5f - proHeight);
                    curPath.lineTo(point5X, point5Y);
                    curPath.lineTo(point4X, point4Y);
                    curPath.lineTo(point3X, point3Y);
                    curPath.lineTo(dotCX, dotCY);
                } else if (curPercent <= 1) {
                    //第五阶段1 没满之前
                    dotCX = point2X - (curLength - 1.5f * proWidth - proHeight * 2);
                    dotCY = point2Y;
                    curPath.lineTo(point5X, point5Y);
                    curPath.lineTo(point4X, point4Y);
                    curPath.lineTo(point3X, point3Y);
                    curPath.lineTo(point2X, point2Y);
                    curPath.lineTo(dotCX, dotCY);
                } else if (curPercent > 1) {
                    //超过1的都画满
                    dotCX = point1X;
                    dotCY = point1Y;
                    curPath.lineTo(point5X, point5Y);
                    curPath.lineTo(point4X, point4Y);
                    curPath.lineTo(point3X, point3Y);
                    curPath.lineTo(point2X, point2Y);
                    curPath.lineTo(dotCX, dotCY);
                    curPath.close();
                }
            } else {
                if (curLength < proWidth / 2) {//处在第一段上面的小圆点的原点坐标和当前进度条的路径
                    //第一阶段 右x x:0  y:w/2-w
                    dotCX = point1X + curLength;
                    dotCY = point1Y;
                    curPath.lineTo(dotCX, dotCY);
                } else if (curLength < (proHeight + proWidth / 2)) {
                    //第二阶段 右y x:w  y:0-h
                    dotCX = point2X;
                    dotCY = point1Y + curLength - proWidth / 2;
                    curPath.lineTo(point2X, point2Y);
                    curPath.lineTo(dotCX, dotCY);
                } else if (curLength < (1.5f * proWidth + proHeight)) {
                    //第三阶段 下x x:0-w  y:h
                    dotCX = point3X - (curLength - proHeight - proWidth / 2);
                    dotCY = point4Y;
                    curPath.lineTo(point2X, point2Y);
                    curPath.lineTo(point3X, point3Y);
                    curPath.lineTo(dotCX, dotCY);
                } else if (curLength < (1.5f * proWidth + proHeight * 2)) {
                    //第四阶段
                    dotCX = point4X;
                    dotCY = point4Y - (curLength - proWidth * 1.5f - proHeight);
                    curPath.lineTo(point2X, point2Y);
                    curPath.lineTo(point3X, point3Y);
                    curPath.lineTo(point4X, point4Y);
                    curPath.lineTo(dotCX, dotCY);
                } else if (curPercent <= 1) {
                    //第五阶段1 没满之前
                    dotCX = point5X + (curLength - 1.5f * proWidth - proHeight * 2);
                    dotCY = point5Y;
                    curPath.lineTo(point2X, point2Y);
                    curPath.lineTo(point3X, point3Y);
                    curPath.lineTo(point4X, point4Y);
                    curPath.lineTo(point5X, point5Y);
                    curPath.lineTo(dotCX, dotCY);
                } else if (curPercent > 1) {
                    //超过1的都画满
                    dotCX = point1X;
                    dotCY = point1Y;
                    curPath.lineTo(point2X, point2Y);
                    curPath.lineTo(point3X, point3Y);
                    curPath.lineTo(point4X, point4Y);
                    curPath.lineTo(point5X, point5Y);
                    curPath.lineTo(dotCX, dotCY);
                    curPath.close();
                }
            }
        } else {
            dotCX = point1X;
            dotCY = point1Y;
            curPath.lineTo(point1X, point1Y);
        }
//        Log.i(TAG, "onDraw: dotC:" + dotCX + "," + dotCY);
        canvas.drawPath(curPath, curPaint);
        if (canDisplayDot) {
            canvas.drawCircle(dotCX, dotCY, dotDiameter * 0.5f, dotPaint);
        }

    }

    private int measureWidth(int widthMeasureSpec) {
        int result;
        int mode = MeasureSpec.getMode(widthMeasureSpec);//得到measurespec的模式
        int size = MeasureSpec.getSize(widthMeasureSpec);//得到measurespec的大小
        int padding = getPaddingLeft() + getPaddingRight();//得到padding在宽度上的大小
        if (mode == MeasureSpec.EXACTLY)//这种模式对应于match_parent和具体的数值dp
        {
            result = size;
        } else {
            result = getSuggestedMinimumWidth();//得到屏幕能给的最大的view的最小宽度，原话：Returns the suggested minimum width that the view should use. This returns the maximum of the view's minimum width and the background's minimum width
            result += padding;//考虑padding后最大的view最小宽度
            if (mode == MeasureSpec.AT_MOST)//这种模式对应于wrap_parent
            {
                result = Math.max(result, size);
            }
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int result;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int padding = getPaddingBottom() + getPaddingTop();
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = getSuggestedMinimumHeight();
            result += padding;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.max(result, size);
            }
        }
        return result;
    }

    public void setCurProgress(int curProgress) {
        this.curProgress = curProgress;
        invalidate();
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }

    public void setMaxColor(@ColorInt int maxColor) {
        this.maxColor = maxColor;
        if(maxPaint!=null){
            maxPaint.setColor(maxColor);
        }
        invalidate();
    }

    public void setCurColor(@ColorInt int curColor) {
        this.curColor = curColor;
        if(curPaint!=null){
            curPaint.setColor(curColor);
        }
        invalidate();
    }

    public void setDotColor(@ColorInt int dotColor) {
        this.dotColor = dotColor;
        if(dotPaint!=null){
            dotPaint.setColor(dotColor);
        }
        invalidate();
    }

    public void setProgressWidth(float progressWidth) {
        this.curProgressWidth = progressWidth;
        if(curPaint!=null){
            curPaint.setStrokeWidth(curProgressWidth);
        }
        this.maxProgressWidth = progressWidth;
        if(maxPaint!=null){
            maxPaint.setStrokeWidth(maxProgressWidth);
        }
        invalidate();
    }

    public void setDotDiameter(float dotDiameter) {
        this.dotDiameter = dotDiameter;
    }

    public void setCanDisplayDot(boolean canDisplayDot) {
        this.canDisplayDot = canDisplayDot;
        invalidate();
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
        invalidate();
    }

    /**
     * 数据转换: dp---->px
     */
    private float dp2Px(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}
