package anim.lynn.voice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import anim.lynn.com.voice.R;

/**
 * Created by zowee-laisc on 2018/4/18.
 */

public class VoiceWave extends View {


    public static final float DEFAULT_BOARD_WIDTH = 10f;

    private Paint mBgPaint;
    private Paint mBoarderPaint;


    public VoiceWave(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        initBgPaint();
        initBoarderPaint();
        initWavePaint();

    }


    private WaveTools mWaveTools;

    private void initWavePaint() {

        mWaveTools = new WaveTools(this);

    }


    private void initBoarderPaint() {
        //默认的style是fill，填充的
        mBoarderPaint = new Paint();
        mBoarderPaint.setAntiAlias(true);
        mBoarderPaint.setStyle(Paint.Style.STROKE);//描边
        mBoarderPaint.setStrokeWidth(DEFAULT_BOARD_WIDTH);
        mBoarderPaint.setColor(Color.RED);
    }

    private void initBgPaint() {
        //默认的style是fill，填充的
        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(getResources().getColor(R.color.voicebg));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        //canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (getWidth() - boaderwidth) / 2f - 1f, mBoarderPaint);//画边框
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, getWidth() / 2f - DEFAULT_BOARD_WIDTH, mBgPaint);//画圆形背景


        //canvas.drawPath(getEquilateralTriangle(new Point(0,getHeight()),getWidth(),getHeight()),mBgPaint); 画三角形背景
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, getWidth() / 2f - DEFAULT_BOARD_WIDTH, mWaveTools.getWavePaint());//画波浪

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWaveTools.updateWaveShader();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWaveTools.startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWaveTools.cancelAnimation();
    }

    private Path getEquilateralTriangle(Point p1, int width, int height) {//p1，是左下角的点，p2为右下角坐标。p3为顶点坐标
        Point p2 = null, p3 = null;

        p2 = new Point(p1.x + width, p1.y);
        p3 = new Point(p1.x + (width / 2), (int) (height - Math.sqrt(3.0) / 2 * height));


        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);

        return path;
    }

}
