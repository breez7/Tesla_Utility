package com.james.ffmpeg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

public class PlayerActivity extends AppCompatActivity {
    public static final String TAG = "james_ffmpeg";
    public boolean isBuffering = true;

    @Override
    protected void onDestroy() {
        if(videoView.getPlayer() != null){
            videoView.getPlayer().stop();
            videoView.getPlayer().release();
        }
        if(videoView_left.getPlayer() != null){
            videoView_left.getPlayer().stop();
            videoView_left.getPlayer().release();
        }
        if(videoView_right.getPlayer() != null){
            videoView_right.getPlayer().stop();
            videoView_right.getPlayer().release();
        }
        if(videoView3.getPlayer() != null){
            videoView3.getPlayer().stop();
            videoView3.getPlayer().release();
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
            this.finish();

        }
        else if(requestCode == MainActivity.REQUEST_PLAY && resultCode == Activity.RESULT_OK)
        {
            if(data != null) {
                uri = data.getData();
                String path = Utils.getRealPathFromURI(this, uri);
                Log.d(TAG, uri.toString());
                Log.d(TAG, uri.getPath());
                Log.d(TAG, DocumentsContract.getDocumentId(uri));
                Log.d(TAG, Utils.getRemovableSDCardPath(this));

//                path = "/sdcard/Download/output.mp4";
//                path = "/sdcard/Download/20191014174741back.mp4";
                SimpleExoPlayer player = new SimpleExoPlayer.Builder(getApplicationContext()).build();
                SimpleExoPlayer player_right = new SimpleExoPlayer.Builder(getApplicationContext()).build();
                SimpleExoPlayer player_left = new SimpleExoPlayer.Builder(getApplicationContext()).build();
                SimpleExoPlayer player3 = new SimpleExoPlayer.Builder(getApplicationContext()).build();
                videoView.setPlayer(player);
                videoView_left.setPlayer(player_left);
                videoView_right.setPlayer(player_right);
                videoView3.setPlayer(player3);

//                Button toggleview_button = videoView.findViewById(R.id.button_toggleview);
//                toggleview_button.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        SimpleExoPlayer player = (SimpleExoPlayer)videoView.getPlayer();
//                        player.stop();
//                        if(isFront) {
//                            player.prepare(backMediaSource);
//                        }
//                        else{
//                            player.prepare(frontMediaSource);
//                        }
//                        isFront = !isFront;
//                        isTouched = true;
//                        if(isPlaying)
//                            player.setPlayWhenReady(true);
//
//                        TextView tv = videoView.findViewById(R.id.textview_position);
//                        if(isFront) {
//                            ((Button)v).setText("Change to\nBack");
//                            tv.setText("FRONT");
//                        }
//                        else {
//                            ((Button)v).setText("Change to\nFront");
//                            tv.setText("BACK");
//                        }
//                    }
//                });
                Button encoding_button = videoView.findViewById(R.id.button_encoding);
                encoding_button.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button button = (Button)v;
                        button.setEnabled(false);
                        button.setText("WAIT!\nENCODING...");
                        if(isFront)
                            MainActivity.requestEncoding(getApplicationContext(), button, uris[FRONT_ID]);
                        else
                            MainActivity.requestEncoding(getApplicationContext(), button, uris[BACK_ID]);
                    }
                });
                Button encoding_button_left = videoView_left.findViewById(R.id.button_encoding);
                encoding_button_left.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button button = (Button)v;
                        button.setEnabled(false);
                        button.setText("WAIT!\nENCODING...");
                        MainActivity.requestEncoding(getApplicationContext(), button, uris[LEFT_ID]);
                    }
                });
                Button encoding_button_right = videoView_right.findViewById(R.id.button_encoding);
                encoding_button_right.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button button = (Button)v;
                        button.setEnabled(false);
                        button.setText("WAIT!\nENCODING...");
                        MainActivity.requestEncoding(getApplicationContext(), button, uris[RIGHT_ID]);
                    }
                });
                Button encoding_button3 = videoView3.findViewById(R.id.button_encoding);
                encoding_button3.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button button = (Button)v;
                        button.setEnabled(false);
                        button.setText("WAIT!\nENCODING...");
                        MainActivity.requestEncoding(getApplicationContext(), button, uris[BACK_ID]);
                    }
                });

                videoView3.setFastForwardIncrementMs(2000);
                videoView3.setRewindIncrementMs(2000);


                DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                DefaultDataSourceFactory mediaDataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), FRONT));
                DefaultDataSourceFactory mediaDataSourceFactory2 = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), BACK));
                DefaultDataSourceFactory mediaDataSourceFactory3 = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), RIGHT));
                DefaultDataSourceFactory mediaDataSourceFactory4 = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), LEFT));

                uris = getUris(getApplicationContext(), uri);

                String temp[] = uris[0].getPath().split("/");
                String filename = temp[temp.length-1];
                int last = filename.lastIndexOf("-");
                timeTextView.setText(filename.substring(0, last));
                frontMediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uris[FRONT_ID]);
                backMediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory2).createMediaSource(uris[BACK_ID]);
                rightMediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory3).createMediaSource(uris[RIGHT_ID]);
                leftMediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory4).createMediaSource(uris[LEFT_ID]);

                isFront = true;
                player.prepare(frontMediaSource);
                player_left.prepare(leftMediaSource);
                player_right.prepare(rightMediaSource);
                player3.prepare(backMediaSource);
                player.setPlayWhenReady(false);
                player_left.setPlayWhenReady(false);
                player_right.setPlayWhenReady(false);
                player3.setPlayWhenReady(false);
                player.addListener(new Player.EventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        Log.d(TAG, "ViewPlayer onPlayerStateChanged " + videoView.getPlayer().getPlaybackState());

                        if(videoView.getPlayer().getPlaybackState() == Player.STATE_READY
                                && videoView_left.getPlayer().getPlaybackState() == Player.STATE_READY
                                && videoView_right.getPlayer().getPlaybackState() == Player.STATE_READY
                                && videoView3.getPlayer().getPlaybackState() == Player.STATE_READY) {
                            if(isBuffering == false) return;
                            if(isPlaying)
                                videoView3.getPlayer().setPlayWhenReady(true);
                            if(isTouched) {
                                long pos = videoView3.getPlayer().getCurrentPosition();
                                videoView.getPlayer().seekTo(pos);
                                isTouched = false;
                            }
                            isBuffering = false;
                        }else{
                                isBuffering = true;
                        }
                    }
                });
                player_left.addListener(new Player.EventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        Log.d(TAG, "Player_left onPlayerStateChanged " + videoView_left.getPlayer().getPlaybackState());
                        if(videoView.getPlayer().getPlaybackState() == Player.STATE_READY
                                && videoView_right.getPlayer().getPlaybackState() == Player.STATE_READY
                                && videoView_left.getPlayer().getPlaybackState() == Player.STATE_READY
                                && videoView3.getPlayer().getPlaybackState() == Player.STATE_READY) {
                            if(isBuffering == false) return;
                            if(isPlaying)
                                videoView3.getPlayer().setPlayWhenReady(true);
                            isBuffering = false;
                        }else{
                            isBuffering = true;
                        }
                    }
                });
                player_right.addListener(new Player.EventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        Log.d(TAG, "Player_right onPlayerStateChanged " + videoView_left.getPlayer().getPlaybackState());
                        if(videoView.getPlayer().getPlaybackState() == Player.STATE_READY
                                && videoView_right.getPlayer().getPlaybackState() == Player.STATE_READY
                                && videoView_left.getPlayer().getPlaybackState() == Player.STATE_READY
                                && videoView3.getPlayer().getPlaybackState() == Player.STATE_READY) {
                            if(isBuffering == false) return;
                            if(isPlaying)
                                videoView3.getPlayer().setPlayWhenReady(true);
                            isBuffering = false;
                        }else{
                                isBuffering = true;
                        }
                    }
                });
                player3.addListener(new Player.EventListener() {
                    @Override
                    public void onPositionDiscontinuity(int reason) {
                        long pos = videoView3.getPlayer().getCurrentPosition();
                        videoView.getPlayer().seekTo(pos);
                        videoView_left.getPlayer().seekTo(pos);
                        videoView_right.getPlayer().seekTo(pos);
                    }
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        Log.d(TAG, "player3 onPlayerStateChanged " + videoView3.getPlayer().getPlaybackState());
//                        Log.d(TAG, "onPlayerStateChanged " + videoView2.getPlayer().getPlaybackState());
//                        Log.d(TAG, "onPlayerStateChanged " + videoView3.getPlayer().getPlaybackState());
                        if(videoView.getPlayer().getPlaybackState() == Player.STATE_READY
                        && videoView_left.getPlayer().getPlaybackState() == Player.STATE_READY
                        && videoView_right.getPlayer().getPlaybackState() == Player.STATE_READY
                        && videoView3.getPlayer().getPlaybackState() == Player.STATE_READY) {
                            if(isBuffering == false) return;
                            if(isPlaying)
                                videoView3.getPlayer().setPlayWhenReady(true);
                            isBuffering = false;
                        }else{
                                isBuffering = true;
                        }
                    }

                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        Log.d(TAG, "onIsPlayingChanged " + isPlaying);
                        videoView.getPlayer().setPlayWhenReady(isPlaying);
                        videoView_left.getPlayer().setPlayWhenReady(isPlaying);
                        videoView_right.getPlayer().setPlayWhenReady(isPlaying);
                        videoView3.getPlayer().setPlayWhenReady(isPlaying);
                        videoView.hideController();;
                        videoView_left.hideController();;
                        videoView_right.hideController();;
                        if(isBuffering == false)
                            PlayerActivity.this.isPlaying = isPlaying;
                    }

                });
            }
        }
    }

    public static final String FRONT = "front";
    public static final String BACK = "back";
    public static final String RIGHT = "right_repeater";
    public static final String LEFT = "left_repeater";

    public static final int FRONT_ID = 0;
    public static final int BACK_ID = 1;
    public static final int RIGHT_ID = 2;
    public static final int LEFT_ID = 3;

    public static Uri[] getUris(Context context, Uri uri){
        Uri[] uris = new Uri[4];

        String path = Utils.getRealPathFromURI(context, uri);
        if(path == null){
            if(uri.getPath().toLowerCase().endsWith(".mp4") == true) { // samba case
                path = uri.toString();
//                Uri newuri = Uri.parse(path);
//                newuri = newuri.buildUpon().appendQueryParameter("scheme", "content").build();
                int lastIndex = path.lastIndexOf("-");
//                uris[FRONT_ID] = Uri.parse(path.substring(0, lastIndex + 1) + FRONT + ".mp4");
//                uris[BACK_ID] = Uri.parse(path.substring(0, lastIndex + 1) + BACK + ".mp4");
//                uris[LEFT_ID] = Uri.parse(path.substring(0, lastIndex + 1) + LEFT + ".mp4");
//                uris[RIGHT_ID] = Uri.parse(path.substring(0, lastIndex + 1) + RIGHT + ".mp4");

                uris[FRONT_ID] = Uri.parse(path.substring(0, lastIndex + 1) + FRONT + ".mp4").buildUpon().authority(uri.getAuthority()).build();
                uris[BACK_ID] = Uri.parse(path.substring(0, lastIndex + 1) + BACK + ".mp4").buildUpon().authority(uri.getAuthority()).build();
                uris[LEFT_ID] = Uri.parse(path.substring(0, lastIndex + 1) + LEFT + ".mp4").buildUpon().authority(uri.getAuthority()).build();
                uris[RIGHT_ID] = Uri.parse(path.substring(0, lastIndex + 1) + RIGHT + ".mp4").buildUpon().authority(uri.getAuthority()).build();
                return uris;
            }
            else{ // googld drive
                uris[FRONT_ID] = uri;
                uris[BACK_ID] = uri;
                uris[RIGHT_ID] = uri;
                uris[LEFT_ID] = uri;
                return uris;
            }
        }

        String[] pathSplit = path.split("-");
        int lastIndex = path.lastIndexOf(pathSplit[pathSplit.length-1]);
        String lastPath = path.substring(0, lastIndex);

        uris[FRONT_ID] = Uri.parse(lastPath + FRONT + ".mp4");
        uris[BACK_ID] = Uri.parse(lastPath + BACK + ".mp4");
        uris[RIGHT_ID] = Uri.parse(lastPath + RIGHT + ".mp4");
        uris[LEFT_ID] = Uri.parse(lastPath + LEFT+ ".mp4");
        return uris;
    }

    PlayerView videoView;
    PlayerView videoView_left;
    PlayerView videoView_right;
    PlayerView videoView3;
    MediaSource frontMediaSource;
    MediaSource backMediaSource;
    MediaSource rightMediaSource;
    MediaSource leftMediaSource;
    TextView timeTextView;
    boolean isFront;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        isFront = true;
        isBuffering = true;
        isPlaying = true;
        setContentView(R.layout.activity_player);

        videoView = findViewById(R.id.videoView);
        videoView_left = findViewById(R.id.videoView_left);
        videoView_right = findViewById(R.id.videoView_right);
        videoView3 = findViewById(R.id.videoView3);

        timeTextView = findViewById(R.id.textview_time);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, MainActivity.REQUEST_PLAY);
    }



}
