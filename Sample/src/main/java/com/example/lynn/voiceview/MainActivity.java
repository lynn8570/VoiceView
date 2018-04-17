package com.example.lynn.voiceview;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import anim.lynn.voice.RecorderSampler;
import anim.lynn.voice.VoiceLine;


public class MainActivity extends AppCompatActivity {
    VoiceLine voiceLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        voiceLine = findViewById(R.id.voice_view);


    }

    @Override
    protected void onStart() {
        super.onStart();
        voiceLine.startRecord(new RecorderSampler.OnStartRecorderError() {
            @Override
            public void onError(int msg) {
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        voiceLine.stopRecord();
    }
}
