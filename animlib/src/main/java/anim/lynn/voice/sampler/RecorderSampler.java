package anim.lynn.voice.sampler;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by zowee-laisc on 2018/4/16.
 */

public abstract class RecorderSampler {


    private List<VolumeListener> mListeners = new ArrayList<>();

    protected OnStartRecorderError mCallback;
    protected int mSamplingInterval = 100;
    protected boolean mIsRecording;
    protected Timer mTimer;

    public RecorderSampler(int samplingInterval) {

        this.mSamplingInterval = samplingInterval;

    }

    public abstract void startRecorder(OnStartRecorderError callback);

    public abstract void stopRecorder();

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

    public void addVolumeLister(VolumeListener listener) {
        mListeners.add(listener);

    }

    protected void onCalculateVolume(float volume) {
        //Log.i("Recorder", "onCalculateVolume =" + volume);
        for (int i = 0; i < mListeners.size(); i++) {
            mListeners.get(i).onCalculateVolume(volume);
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
