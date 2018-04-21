package anim.lynn.voice;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;

import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.util.ArrayList;
import java.util.List;

import anim.lynn.com.voice.R;

/**
 * Created by zowee-laisc on 2018/4/19.
 */

public class VoiceSvg extends View {
    private int mSvgResourceId;
    private SVG mSvg;
    private Thread mLoader;
    private final Object mSvgLock = new Object();

    private int mWidth;
    private int mHeight;

    private Paint mSourcePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<SvgPath> mPaths = new ArrayList<>();


    private float mSvgProgress = 0.5f;

    public float getSvgProgress() {
        return mSvgProgress;
    }

    public void setSvgProgress(float progress) {
        Log.i("linlian", "setProgress=" + progress);
        if (this.mSvgProgress != progress) {

            this.mSvgProgress = progress;
            updatePathsPhaseLocked();
            invalidate();
        }

    }


    private ObjectAnimator svgPathProgressAnimator;
    private void initAnimation() {
        svgPathProgressAnimator = ObjectAnimator.ofFloat(VoiceSvg.this, "svgProgress", 0f, 1f);
        svgPathProgressAnimator.setRepeatCount(0);
        svgPathProgressAnimator.setDuration(5000);
        svgPathProgressAnimator.setInterpolator(new LinearInterpolator());

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


    public void startAnimation() {
        if (svgPathProgressAnimator != null) {
            svgPathProgressAnimator.start();
        }
    }

    public void cancelAnimation() {
        if (svgPathProgressAnimator != null) {
            svgPathProgressAnimator.cancel();
        }
    }

    public VoiceSvg(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceSvg);
        if (typedArray != null) {
            mSvgResourceId = typedArray.getResourceId(R.styleable.VoiceSvg_svg_file, 0);
        }
        typedArray.recycle();
        init();

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


        startLoadSvg(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawRect(0, 0, mWidth, mHeight, mSourcePaint);

        synchronized (mSvgLock) {
            canvas.translate(getPaddingLeft(), getPaddingTop());
            final int count = mPaths.size();
            for (int i = 0; i < count; i++) {
                final SvgPath svgPath = mPaths.get(i);
                final Path path = svgPath.path;

                canvas.drawPath(path, mSourcePaint);
            }
        }
    }

    private void init() {
        mSourcePaint.setStyle(Paint.Style.STROKE);
        mSourcePaint.setColor(getResources().getColor(R.color.voicefr));
        mSourcePaint.setStrokeWidth(2);
        initAnimation();
    }

    private void startLoadSvg(final int w, final int h) {
        if (mLoader != null) {
            try {
                mLoader.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mSvgResourceId != 0) {

            mLoader = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mSvg = SVG.getFromResource(getContext(), mSvgResourceId);
                        mSvg.setDocumentPreserveAspectRatio(PreserveAspectRatio.UNSCALED);
                    } catch (SVGParseException e) {
                        e.printStackTrace();
                    }

                    synchronized (mSvgLock) {
                        mWidth = w - getPaddingLeft() - getPaddingRight();
                        mHeight = h - getPaddingTop() - getPaddingBottom();
                        getPathsForViewport(mWidth, mHeight);
                        updatePathsPhaseLocked();

                    }
                }
            }, "SVG loader");

            mLoader.start();
        }

    }


    /**
     * Render the svg to canvas and catch all the paths while rendering.
     *
     * @param width  - the width to scale down the view to,
     * @param height - the height to scale down the view to,
     * @return All the paths from the svg.
     */
    private List<SvgPath> getPathsForViewport(final int width, final int height) {
        final float strokeWidth = mSourcePaint.getStrokeWidth();
        Canvas canvas = new Canvas() {
            private final Matrix mMatrix = new Matrix();

            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return height;
            }

            @Override
            public void drawPath(Path path, Paint paint) {
                Path dst = new Path();

                //noinspection deprecation
                getMatrix(mMatrix);
                path.transform(mMatrix, dst);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(strokeWidth);
                mPaths.add(new SvgPath(dst, paint));
            }
        };

        rescaleCanvas(width, height, strokeWidth, canvas);

        return mPaths;
    }

    /**
     * Rescale the canvas with specific width and height.
     *
     * @param width       The width of the canvas.
     * @param height      The height of the canvas.
     * @param strokeWidth Width of the path to add to scaling.
     * @param canvas      The canvas to be drawn.
     */
    private void rescaleCanvas(int width, int height, float strokeWidth, Canvas canvas) {
        if (mSvg == null)
            return;
        final RectF viewBox = mSvg.getDocumentViewBox();

        final float scale = Math.min(width
                        / (viewBox.width() + strokeWidth),
                height / (viewBox.height() + strokeWidth));

        canvas.translate((width - viewBox.width() * scale) / 2.0f,
                (height - viewBox.height() * scale) / 2.0f);
        canvas.scale(scale, scale);

        mSvg.renderToCanvas(canvas);
    }


    /**
     * This refreshes the paths before draw and resize.
     */
    private void updatePathsPhaseLocked() {
        Log.i("linlian", "updatePathsPhaseLocked");
        final int count = mPaths.size();
        for (int i = 0; i < count; i++) {
            SvgPath svgPath = mPaths.get(i);
            svgPath.path.reset();
            svgPath.measure.getSegment(0.0f, svgPath.length * mSvgProgress, svgPath.path, true);
            // Required only for Android 4.4 and earlier
            svgPath.path.rLineTo(0.0f, 0.0f);
        }

    }

    /**
     * Path with bounds for scalling , length and paint.
     */
    public static class SvgPath {

        /**
         * Region of the path.
         */
        private static final Region REGION = new Region();
        /**
         * This is done for clipping the bounds of the path.
         */
        private static final Region MAX_CLIP =
                new Region(Integer.MIN_VALUE, Integer.MIN_VALUE,
                        Integer.MAX_VALUE, Integer.MAX_VALUE);
        /**
         * The path itself.
         */
        final Path path;
        /**
         * The paint to be drawn later.
         */
        final Paint paint;
        /**
         * The length of the path.
         */
        float length;
        /**
         * Listener to notify that an animation step has happened.
         */
        AnimationStepListener animationStepListener;
        /**
         * The bounds of the path.
         */
        final Rect bounds;
        /**
         * The measure of the path, we can use it later to get segment of it.
         */
        final PathMeasure measure;

        /**
         * Constructor to add the path and the paint.
         *
         * @param path  The path that comes from the rendered svg.
         * @param paint The result paint.
         */
        SvgPath(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;

            measure = new PathMeasure(path, false);
            this.length = measure.getLength();

            REGION.setPath(path, MAX_CLIP);
            bounds = REGION.getBounds();
        }

        /**
         * Sets the animation step listener.
         *
         * @param animationStepListener AnimationStepListener.
         */
        public void setAnimationStepListener(AnimationStepListener animationStepListener) {
            this.animationStepListener = animationStepListener;
        }

        /**
         * Sets the length of the path.
         *
         * @param length The length to be set.
         */
        public void setLength(float length) {
            path.reset();
            measure.getSegment(0.0f, length, path, true);
            path.rLineTo(0.0f, 0.0f);

            if (animationStepListener != null) {
                animationStepListener.onAnimationStep();
            }
        }

        /**
         * @return The length of the path.
         */
        public float getLength() {
            return length;
        }
    }

    public interface AnimationStepListener {

        /**
         * Called when an animation step happens.
         */
        void onAnimationStep();
    }
}
