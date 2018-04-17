package anim.lynn.voice;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import anim.lynn.com.voice.R;


/**
 * Created by lynn on 2018/4/5.
 */

public class VoiceLine extends View {


    private int mWidth;
    private int mHeight;

    private float mStrength;

    private Paint mPaint;

    private final float MAX_DB = 100f; //正常交谈 60 分贝
    private final float MIN_DB = 40f;

    private float[] mPositions = new float[2];

    private RecorderSampler micDBSampler;

    public VoiceLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceLine);
        if (typedArray != null) {
            mPositions[0] = typedArray.getFloat(R.styleable.VoiceLine_position_bg, 0.2f);
            mPositions[1] = typedArray.getFloat(R.styleable.VoiceLine_position_fr, 1f);
        }

        // micDBSampler = new AudioRecorderSampler(100);

        micDBSampler = new MediaRecorderSampler(100);
        micDBSampler.setVolumeLister(new RecorderSampler.VolumeListener() {
            @Override
            public void onCalculateVolume(final float volume) {
                postAnimation(volume);
            }
        });


    }

    public void startRecord(RecorderSampler.OnStartRecorderError callback) {
        micDBSampler.startRecorder(callback);
    }

    public void stopRecord() {
        micDBSampler.stopRecorder();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        mPaint.setColor(getResources().getColor(R.color.voicebg));
        int width = getWidth();
        int height = getHeight();

//        canvas.drawRect(0,0,width,height,mPaint);
//        mPaint.setColor(getResources().getColor(R.color.voicefr));
//        mStrength = 0.5f;
//
//        float xWidth = width * mStrength;
//        canvas.drawRect((width-xWidth)/2,0, (width+xWidth)/2, height ,mPaint);
        //改为使用渐变效果


        int[] colors = {getResources().getColor(R.color.voicebg),
                getResources().getColor(R.color.voicefr)};

        //Log.i("positions=", "p=" + mPositions[0] + "," + mPositions[1]);
        LinearGradient linearGradient = new LinearGradient(0, 0, width / 2, height / 2, colors, mPositions, Shader.TileMode.MIRROR

        );
        mPaint.setShader(linearGradient);
        canvas.drawRect(0, 0, width, height, mPaint);

    }


    private float preValue = 0;

    public void postAnimation(final float volumn) {
        Log.i("onAnimationUpdate", "volumn=" + volumn);
        post(new Runnable() {
            @Override
            public void run() {
                startAnimation(volumn);
            }
        });
    }

    public void startAnimation(float curValue) {
        ValueAnimator animator = ValueAnimator.ofObject(new VoiceEvaluator(), preValue, curValue);
        preValue = curValue;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                try {
                    float result = (float) animation.getAnimatedValue();//0-1
                    //Log.i("onAnimationUpdate", "result3=" + result);
                    if (result < 1) {
                        mPositions[0] = result - 0.1f;
                        mPositions[1] = result;
                    } else {
                        mPositions[0] = 0;
                        mPositions[1] = 0;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                invalidate();
            }
        });
        animator.setDuration(1000);
        animator.start();
    }

    public class VoiceEvaluator implements TypeEvaluator<Float> {

        @Override
        public Float evaluate(float fraction, Float startValue, Float endValue) {

            // float result = endValue*fraction/100f;
            float t = fraction;

            // Log.i("onAnimationUpdate", "time=" + t);

            float result = 0f;
            if (t < (1 / 2.75f)) {//f=0.36
                result = (7.5625f * t * t);
            } else if (t < (2 / 2.75f)) {//f=0.72727272
                result = (7.5625f * (t -= (1.5f / 2.75f)) * t + .75f);//-7.5625*(x-0.54)*(x-0.54)+0.25
            } else if (t < (2.5 / 2.75)) {
                result = (7.5625f * (t -= (2.25f / 2.75f)) * t + .93755f);
            } else {
                result = (7.5625f * (t -= (2.625f / 2.75f)) * t + 0.984375f);
            }
            //Log.i("onAnimationUpdate", "result1=" + result);

            //偏移四十，根据实际声音微调
            startValue = startValue - MIN_DB;
            endValue = endValue - MIN_DB;

            startValue = startValue > MAX_DB ? MAX_DB : startValue;
            endValue = endValue > MAX_DB ? MAX_DB : endValue;

            result = startValue / MAX_DB + result * (endValue - startValue) / MAX_DB;
            // Log.i("onAnimationUpdate", "result 2=" + result);
            return 1 - result;
        }
    }
}
