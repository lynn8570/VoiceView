package anim.lynn.voice;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zowee-laisc on 2018/4/13.
 */

public class AudioRecorderSampler extends RecorderSampler {

    public static final String TAG = "MicStatusRecorderView";
    private static final int RECORDING_SAMPLE_RATE = 44100;

    private AudioRecord mAudioRecord;

    private int mBufSize;


    public AudioRecorderSampler(int samplingInterval) {
        super(samplingInterval);

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

    @Override
    void startRecorder(OnStartRecorderError callback) {
        initAudioRecord();
        mTimer = new Timer();
        mAudioRecord.startRecording();
        mIsRecording = true;
        runRecording();
    }

    @Override
    void stopRecorder() {
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
                onCalculateVolume(decibel);
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

        mAudioRecord.release();
        mAudioRecord = null;
        mTimer = null;
    }


}

