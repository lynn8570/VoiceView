package com.example.lynn.voiceview;

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


/**
 * Created by lynn on 2018/4/5.
 */

public class VoiceLine extends View {


    private int mWidth;
    private int mHeight;

    private float mStrength;

    private Paint mPaint;

    private float[] mPositions=new float[2];
    public VoiceLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceLine);
        if(typedArray!=null){
            mPositions[0]=typedArray.getFloat(R.styleable.VoiceLine_position_bg,0.2f);
            mPositions[1]=typedArray.getFloat(R.styleable.VoiceLine_position_fr,1f);
        }

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


        int[] colors={getResources().getColor(R.color.voicebg),
                getResources().getColor(R.color.voicefr)};

        Log.i("positions=","p="+mPositions[0]+","+mPositions[1]);
        LinearGradient linearGradient = new LinearGradient(0,0,width/2,height/2,colors,mPositions, Shader.TileMode.MIRROR

        );
        mPaint.setShader(linearGradient);
        canvas.drawRect(0,0,width,height,mPaint);

    }


    public void startAnimation(){
        ValueAnimator animator =  ValueAnimator.ofObject(new VoiceEvaluator(),0f,100f,50f,60f,20f,120f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                try {
                    float result = (float) animation.getAnimatedValue();//0-1
                    if(result<1){
                        mPositions[0]=result;
                        mPositions[1]=result+0.1f;
                    }else{
                        mPositions[0]=0;
                        mPositions[1]=0;
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                invalidate();
            }
        });
        animator.setDuration(12000);

        animator.start();
    }

    public class VoiceEvaluator implements TypeEvaluator<Float>{

        @Override
        public Float evaluate(float fraction, Float startValue, Float endValue) {

            float result = endValue*fraction/100f;
            Log.i("ValueAnimator","result2="+result);

            return result;
        }
    }
}
