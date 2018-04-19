package anim.lynn.voice;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import anim.lynn.com.voice.R;

/**
 * Created by zowee-laisc on 2018/4/18.
 */

public class VoiceWave extends View {

    public static final float DEFAULT_BOARD_WIDTH = 10f;

    private Paint mBgPaint;
    private Paint mBoarderPaint;
    private Paint mWavePaint;


    private BitmapShader mWaveShader;

    private Matrix mShaderMatrix;

    private float mWaveXshift = 0;//属性变化 0-1

    private AnimatorSet mAnimatorset;

    public float getWaveXshift() {
        return mWaveXshift;
    }

    public void setWaveXshift(float mWaveXshift) {
        if (this.mWaveXshift != mWaveXshift) {
            this.mWaveXshift = mWaveXshift;
            //变化的是重新绘制view，实现动画效果
            invalidate();
        }


    }

    public VoiceWave(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {


        initBgPaint();
        initBoarderPaint();
        initWavePaint();
        initAnimation();

    }

    private void initWavePaint() {
        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStrokeWidth(2);
        mWavePaint.setColor(getResources().getColor(R.color.wavefr));
        updateWaveShader();
    }


    private void updateWaveShader() {
        if (getWaveBitmap() != null) {
            mWaveShader = new BitmapShader(getWaveBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);//x坐标repeat模式，y方向上最后一个像素重复
            mWavePaint.setShader(mWaveShader);
        } else {
            mWavePaint.setShader(null);
        }
    }


    private Bitmap getWaveBitmap() {//返回一个波形的图案，两条线

        int wavecount = 1;//容纳多少个完整波形


        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        Log.i("linlian", "width=" + width + " height=" + height);
        if (width > 0 && height > 0) {

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas waveLineCanvas = new Canvas(bitmap);


            //坐标点数组，x从0-width，y=Asin(Wx+Q)+H
            // W = 2*PI/width
            // A = height
            // h = height
            // Q = 0
            final int endX = width + 1;
            final int endY = height + 1;
            float W = (float) (2f * Math.PI * wavecount / width);
            float A = height / 10f;//波浪幅度在整体的10分之一
            float H = height / 2;//默认水位在一半的位置
            float[] waveY = new float[endX];

            for (int x = 0; x < endX; x++) {
                waveY[x] = (float) (A * Math.sin(W * x)) + H;
            }

            int xShift = width / 4;
            mWavePaint.setColor(getResources().getColor(R.color.wavebg));
            for (int x = 0; x < endX; x++) {
                waveLineCanvas.drawLine(x, waveY[(x + xShift) % endX], x, endY, mWavePaint);// .:|:. 像这样画线
            }
            mWavePaint.setColor(getResources().getColor(R.color.wavefr));
            for (int x = 0; x < endX; x++) {

                waveLineCanvas.drawLine(x, waveY[x], x, endY, mWavePaint);// .:|:. 像这样画线
            }

            return bitmap;
        }
        return null;
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

    private void initAnimation() {
        mShaderMatrix = new Matrix();
        ObjectAnimator waveXshiftAnimator = ObjectAnimator.ofFloat(this, "waveXshift", 0f, 1f);
        waveXshiftAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveXshiftAnimator.setDuration(2000);
        waveXshiftAnimator.setInterpolator(new LinearInterpolator());
        mAnimatorset = new AnimatorSet();
        mAnimatorset.play(waveXshiftAnimator);

    }

    private void startAnimation() {
        if (mAnimatorset != null) {
            mAnimatorset.start();
        }
    }

    private void cancelAnimation() {
        if (mAnimatorset != null) {
            mAnimatorset.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float boaderwidth = DEFAULT_BOARD_WIDTH;

        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (getWidth() - boaderwidth) / 2f - 1f, mBoarderPaint);//画边框
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, getWidth() / 2f - boaderwidth, mBgPaint);//画圆形背景


        if (mWaveShader != null) {
            if (mWavePaint.getShader() == null) {
                mWavePaint.setShader(mWaveShader);
            }
            Log.i("linlian2", "mWaveXshift=" + mWaveXshift);
            Log.i("linlian2", "getWidth=" + getWidth() + " getHeight=" + getHeight());
            float dx = mWaveXshift * getWidth();
            mShaderMatrix.setTranslate(dx, 0);//平移波浪，实现推进效果

            mWaveShader.setLocalMatrix(mShaderMatrix);
        } else {
            mWavePaint.setShader(null);
        }
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, getWidth() / 2f - boaderwidth, mWavePaint);//画波浪

        // canvas.drawPath(getEquilateralTriangle(new Point(0,getHeight()),getWidth(),getHeight()),mBgPaint); 画三角形背景

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateWaveShader();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnimation();
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
