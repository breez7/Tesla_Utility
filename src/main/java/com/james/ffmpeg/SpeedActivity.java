package com.james.ffmpeg;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;

public class SpeedActivity extends AppCompatActivity implements LocationListener, Runnable {
    LocationManager locationManager;
    TextView speed_tv;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
        speed_tv = findViewById(R.id.speed_text);
        keep= true;
        initSound();
    }

    byte[] buffer;
    int bufSize;
    int samplerate = 44100;
    int encoding = AudioFormat.ENCODING_PCM_16BIT;
    int channel = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    Thread playThread;
    AudioTrack audioTrack;
    PlaybackParams params;
    private void initSound(){
        InputStream is = null;
        try {
            is = getAssets().open("engine.wav");
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        params = new PlaybackParams();
        bufSize = AudioTrack.getMinBufferSize(samplerate, channel, encoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, samplerate, channel, encoding, buffer.length, AudioTrack.MODE_STREAM);
        playThread = new Thread(this);
        playThread.start();

        setSpeed(0.1f);
    }


    @Override
    public void onLocationChanged(Location location) {
        float speed_km = location.getSpeed() * 3600 / 1000;
        setSpeed(speed_km);
        Log.d("james_ffmpeg", String.format("%f", location.getSpeed()));
//TODO
    }

    private void setSpeed(float speed_km){
        if(speed_km < 0) return;
        speed_tv.setText(String.format("%d km/h", (int)speed_km));
        float speed = 1 + speed_km / 50;
        float pitch = 1 + speed_km / 50;
        audioTrack.setPlaybackParams(params.setSpeed(speed));
        audioTrack.setPlaybackParams(params.setPitch(pitch));

    }

    float fake_speed = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                fake_speed += 1;
                setSpeed(fake_speed);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                fake_speed -= 1;
                setSpeed(fake_speed);
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onDestroy() {
        keep = false;
        audioTrack.release();
        super.onDestroy();
    }

    boolean keep;
    @Override
    public void run() {
        if(audioTrack != null) {
            audioTrack.play();
            while(keep){
                audioTrack.write(buffer, 0, buffer.length);
            }
        }
    }
}
