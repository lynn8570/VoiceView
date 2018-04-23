package anim.lynn.voice;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import anim.lynn.com.voice.R;

/**
 * Created by zowee-laisc on 2018/4/21.
 */

public class WaveTools {

    private Paint mWavePaint;
    private BitmapShader mWaveShader;
    private Matrix mShaderMatrix;
    private float mWaveXshift = 0;//属性变化 0-1
    private float mWaveYshift = 0;
    private View mView;

    private int mWidth;
    private int mHeight;
    private AnimatorSet mAnimatorset;

    public WaveTools(View view) {
        this.mView = view;
        init();
    }

    private void init() {
        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStrokeWidth(2);
        mWavePaint.setColor(mView.getContext().getResources().getColor(R.color.wavefr));
        mShaderMatrix = new Matrix();
        initAnimation();
        updateWaveShader();
    }

    public float getWaveXshift() {
        return mWaveXshift;
    }

    public void setWaveXshift(float mWaveXshift) {
        if (this.mWaveXshift != mWaveXshift) {
            this.mWaveXshift = mWaveXshift;
            //变化的是重新绘制view，实现动画效果
//            invalidate();

            mView.invalidate();
        }
    }

    public float getWaveYshift() {
        return mWaveYshift;
    }

    public void setWaveYshift(float mWaveYshift) {
        if (this.mWaveYshift != mWaveYshift) {

            this.mWaveYshift = mWaveYshift;
            mView.invalidate();
        }

    }

    private void initAnimation() {

        ObjectAnimator waveXshiftAnimator = ObjectAnimator.ofFloat(this, "waveXshift", 0f, 1f);
        waveXshiftAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveXshiftAnimator.setDuration(2000);
        waveXshiftAnimator.setInterpolator(new LinearInterpolator());

        ObjectAnimator waveYshiftAnimator = ObjectAnimator.ofFloat(this, "waveYshift", 0f, 0.5f, 0f, -0.5f, 0f);
        waveYshiftAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveYshiftAnimator.setDuration(20000);
        waveYshiftAnimator.setInterpolator(new LinearInterpolator());
        mAnimatorset = new AnimatorSet();
        mAnimatorset.playTogether(waveXshiftAnimator, waveYshiftAnimator);

    }


    public void updateWaveShader() {

        if (getWaveBitmap() != null) {
            mWaveShader = new BitmapShader(getWaveBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);//x坐标repeat模式，y方向上最后一个像素重复
            mWavePaint.setShader(mWaveShader);
        } else {
            mWavePaint.setShader(null);
        }
    }


    public void startAnimation() {
        if (mAnimatorset != null) {
            mAnimatorset.start();
        }
    }

    public void cancelAnimation() {
        if (mAnimatorset != null) {
            mAnimatorset.cancel();
        }
    }

    private Bitmap getWaveBitmap() {//返回一个波形的图案，两条线

        int wavecount = 1;//容纳多少个完整波形


        int width = mView.getMeasuredWidth();
        int height = mView.getMeasuredHeight();
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
            mWavePaint.setColor(mView.getResources().getColor(R.color.wavebg));
            for (int x = 0; x < endX; x++) {
                waveLineCanvas.drawLine(x, waveY[(x + xShift) % endX], x, endY, mWavePaint);// .:|:. 像这样画线
            }
            mWavePaint.setColor(mView.getResources().getColor(R.color.wavefr));
            for (int x = 0; x < endX; x++) {

                waveLineCanvas.drawLine(x, waveY[x], x, endY, mWavePaint);// .:|:. 像这样画线
            }

            return bitmap;
        }
        return null;
    }


    public Paint getWavePaint() {
        if (mWaveShader != null) {
            if (mWavePaint.getShader() == null) {
                mWavePaint.setShader(mWaveShader);
            }

            float dx = mWaveXshift * mView.getWidth();
            float dy = mWaveYshift * mView.getWidth();
            mShaderMatrix.setTranslate(dx, dy);//平移波浪，实现推进效果

            mWaveShader.setLocalMatrix(mShaderMatrix);
        } else {
            mWavePaint.setShader(null);
        }

        return mWavePaint;
    }
}
