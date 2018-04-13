package anim.lynn.voice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zowee-laisc on 2018/4/13.
 */

public class MicDBSampler {

    public static final String TAG = "MicStatusRecorderView";
    private static final int RECORDING_SAMPLE_RATE = 44100;

    private AudioRecord mAudioRecord;
    private boolean mIsRecording;
    private int mBufSize;


    private CalculateVolumeListener mVolumeListener;
    private int mSamplingInterval = 100;
    private Timer mTimer;


    public MicDBSampler(int samplingInterval) {

        this.mSamplingInterval = samplingInterval;
    }

    public void setmVolumeListener(CalculateVolumeListener mVolumeListener) {
        this.mVolumeListener = mVolumeListener;
    }


    /**
     * getter isRecording
     *
     * @return true:recording, false:not recording
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    private void initAudioRecord() {
        int bufferSize = AudioRecord.getMinBufferSize(
                RECORDING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        mAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

        if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            mBufSize = bufferSize;
        }

    }

    /**
     * start AudioRecord.read
     */
    public void startRecording() {
        initAudioRecord();
        mTimer = new Timer();
        mAudioRecord.startRecording();
        mIsRecording = true;
        runRecording();
    }

    /**
     * stop AudioRecord.read
     */
    public void stopRecording() {
        mIsRecording = false;
        mTimer.cancel();

        release();

    }

    private void runRecording() {
        final byte buf[] = new byte[mBufSize];
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // stop recording
                if (!mIsRecording) {
                    mAudioRecord.stop();
                    return;
                }
                mAudioRecord.read(buf, 0, mBufSize);

                float decibel = calculateDecibel(buf);


                // callback for return input value
                if (mVolumeListener != null) {
                    mVolumeListener.onCalculateVolume(decibel);
                }
            }
        }, 0, mSamplingInterval);
    }

    private float calculateDecibel(byte[] buf) {
        int sum = 0;
        for (int i = 0; i < mBufSize; i++) {
            sum += Math.abs(buf[i]);
        }
        // avg 10-50
        return sum / mBufSize;
    }

    /**
     * release member object
     */
    public void release() {
        stopRecording();
        mAudioRecord.release();
        mAudioRecord = null;
        mTimer = null;
    }

    public interface CalculateVolumeListener {

        /**
         * calculate input volume
         *
         * @param volume mic-input volume
         */
        void onCalculateVolume(float volume);
    }
}

