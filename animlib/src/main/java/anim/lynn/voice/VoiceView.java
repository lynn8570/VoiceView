package anim.lynn.voice;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import anim.lynn.voice.sampler.MediaRecorderSampler;
import anim.lynn.voice.sampler.RecorderSampler;

/**
 * Created by zowee-laisc on 2018/4/21.
 */

public abstract class VoiceView extends View implements RecorderSampler.VolumeListener {
    protected RecorderSampler micDBSampler;

    public VoiceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        micDBSampler = new MediaRecorderSampler(100);
        micDBSampler.addVolumeLister(this);
    }
    public void startRecord(RecorderSampler.OnStartRecorderError callback) {
        micDBSampler.startRecorder(callback);
    }

    public void stopRecord() {
        micDBSampler.stopRecorder();
    }

}
