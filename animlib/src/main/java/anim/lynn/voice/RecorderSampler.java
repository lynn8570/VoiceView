package anim.lynn.voice;

import android.util.Log;

import java.util.Timer;

/**
 * Created by zowee-laisc on 2018/4/16.
 */

public abstract class RecorderSampler {

    protected VolumeListener mListener;
    protected OnStartRecorderError mCallback;
    protected int mSamplingInterval = 100;
    protected boolean mIsRecording;
    protected Timer mTimer;

    public RecorderSampler(int samplingInterval) {

        this.mSamplingInterval = samplingInterval;

    }

    abstract void startRecorder(OnStartRecorderError callback);

    abstract void stopRecorder();

    /**
     * getter isRecording
     *
     * @return true:recording, false:not recording
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    public void onError(int msg) {
        if (mCallback != null) {
            mCallback.onError(msg);
        }
    }

    public void setVolumeLister(VolumeListener listener) {
        this.mListener = listener;

    }

    protected void onCalculateVolume(float volume) {
        //Log.i("Recorder", "onCalculateVolume =" + volume);
        if (mListener != null) {
            mListener.onCalculateVolume(volume);
        }
    }

    public interface OnStartRecorderError {
        void onError(int msg);
    }

    public interface VolumeListener {

        /**
         * calculate input volume
         *
         * @param volume mic-input volume
         */
        void onCalculateVolume(float volume);
    }

}
