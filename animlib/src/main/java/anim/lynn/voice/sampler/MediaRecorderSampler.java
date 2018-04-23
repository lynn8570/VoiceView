package anim.lynn.voice.sampler;

import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zowee-laisc on 2018/4/16.
 */

public class MediaRecorderSampler extends RecorderSampler {

    private File tmpFile;

    private MediaRecorder mMediaRecorder;
    private File mTempfile;

    public static final int FILE_CREATE_ERROR = 1;
    public static final int MEDIA_PREPARE_ERROR = 2;

    public MediaRecorderSampler(int samplingInterval) {
        super(samplingInterval);
    }

    private boolean initTempfile() {

        String tmpFile = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/tmp.arm";

        mTempfile = new File(tmpFile);
        if (!mTempfile.exists()) {
            try {
                return mTempfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    @Override
    public void startRecorder(OnStartRecorderError callback) {

        mCallback = callback;
        if (!initTempfile()) {
            //file error
            onError(FILE_CREATE_ERROR);
            return;
        }

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mMediaRecorder.setMaxDuration(1000 * 60 * 5);
        mMediaRecorder.setOutputFile(mTempfile.getAbsolutePath());


        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mIsRecording = true;
            runRecording();
        } catch (IOException e) {
            e.printStackTrace();
            onError(MEDIA_PREPARE_ERROR);
        }
    }


    private void runRecording() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // stop recording
                if (!mIsRecording) {
                    mMediaRecorder.stop();
                    return;
                }

                float ratio = mMediaRecorder.getMaxAmplitude();
                float db = 0f;
                if (ratio > 1)
                    db = (float) (20 * Math.log10(ratio));
                // callback for return input value
                onCalculateVolume(db);
            }
        }, 0, mSamplingInterval);
    }


    @Override
    public void stopRecorder() {
        mMediaRecorder.stop();
        mIsRecording = false;
        mMediaRecorder.release();
        mTimer.cancel();
        mMediaRecorder = null;
        mTimer = null;

    }
}
