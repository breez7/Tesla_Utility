package com.james.ffmpeg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class OnePlayerActivity extends AppCompatActivity {
    public static final String TAG = "james_ffmpeg";
    public boolean isBuffering = true;

    @Override
    protected void onDestroy() {
        if(videoView.getPlayer() != null){
            videoView.getPlayer().stop();
            videoView.getPlayer().release();
        }
        super.onDestroy();
    }

    public boolean isPlaying = false;
    public boolean isTouched = false;
    Uri uri;
    Uri[] uris;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MainActivity.REQUEST_PLAY && resultCode == Activity.RESULT_CANCELED){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            this.finish();

        }
        else if(requestCode == MainActivity.REQUEST_PLAY && resultCode == Activity.RESULT_OK)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if(data != null) {
                uri = data.getData();
                String path = Utils.getRealPathFromURI(this, uri);
                Log.d(TAG, uri.toString());
                Log.d(TAG, uri.getPath());
                Log.d(TAG, DocumentsContract.getDocumentId(uri));
                Log.d(TAG, Utils.getRemovableSDCardPath(this));
                /* Instantiate a DefaultLoadControl.Builder. */
                DefaultLoadControl.Builder builder = new
                        DefaultLoadControl.Builder();

                /*How many milliseconds of media data to buffer at any time. */
                final int loadControlBufferMs = 65000; /* This is 50000 milliseconds in ExoPlayer 2.9.6 */
                /* Configure the DefaultLoadControl to use the same value for */
                DefaultLoadControl loadControl = builder.setBufferDurationsMs(
                        loadControlBufferMs,
                        loadControlBufferMs,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS).setBackBuffer(loadControlBufferMs,true).createDefaultLoadControl();
                SimpleExoPlayer player = new SimpleExoPlayer.Builder(getApplicationContext()).setLoadControl(loadControl).build();
                videoView.setPlayer(player);

                videoView.setFastForwardIncrementMs(2000);
                videoView.setRewindIncrementMs(2000);
                DefaultDataSourceFactory mediaDataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "ONE"));
                mediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);

                player.prepare(mediaSource);
                player.setPlayWhenReady(true);

            }
        }
    }

    PlayerView videoView;
    MediaSource mediaSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_oneplayer);

        videoView = findViewById(R.id.videoView);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, MainActivity.REQUEST_PLAY);
    }



}
