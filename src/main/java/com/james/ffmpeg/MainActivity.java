package com.james.ffmpeg;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.List;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_ACCESS_LOCATION = 1;
    public static String[] PERMISSIONS_ALL = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    Button encodingButton;
    Button playButton;
    Button oneplayButton;
    Button speedButton;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
//        requestPermissions();

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS_ALL,
                    REQUEST_EXTERNAL_STORAGE);
        }
        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS_ALL,
                    REQUEST_EXTERNAL_STORAGE);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, PERMISSIONS_ALL, REQUEST_ACCESS_LOCATION);
        }
//        StorageManager storageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
//        List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
//        StorageVolume primaryVolume = storageManager.getPrimaryStorageVolume();
//        Intent intent = primaryVolume.createOpenDocumentTreeIntent();
//        startActivityForResult(intent, 1);
        encodingButton = findViewById(R.id.encoding_button);
        encodingButton.setOnClickListener(encoding_listener);
        playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(play_listener);
        oneplayButton = findViewById(R.id.play_one_button);
        oneplayButton.setOnClickListener(oneplay_listener);
        speedButton = findViewById(R.id.speed_button);
        speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SpeedActivity.class);
                startActivity(intent);
            }
        });
        File dir = new File(teslaDashCamPath);
        if(!dir.exists()){
            dir.mkdir();
        }


    }


    public static final int REQUEST_ENCODING = 1;
    public static final int REQUEST_PLAY = 2;

    public static final String TAG = "james_ffmpeg";
    public static String teslaDashCamPath = Environment.getExternalStorageDirectory() + "/TeslaDashCam/";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENCODING && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if(data != null) {
                uri = data.getData();
                requestEncoding(getApplicationContext(), encodingButton, uri);
            }
        }
    }

    public static void requestEncoding(Context context, Button button,Uri uri){


        new EncodingTask(context, button).execute(uri);
    }

    private static class EncodingTask extends AsyncTask {
        Button encodingButton;
        Context mContext;

        public EncodingTask(Context context, Button button){
           encodingButton = button;
           mContext = context;
        }
        @Override
        protected void onProgressUpdate(Object[] values) {
            encodingButton.setEnabled(false);
            encodingButton.setText("Wait!\nEncoding...");
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            encodingButton.setEnabled(true);
            encodingButton.setText("ADD\nTIMESTAMP");
            Toast.makeText(mContext,o.toString(), Toast.LENGTH_LONG).show();
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            publishProgress(objects);
            Uri uri = (Uri)objects[0];
            String filePath = Utils.getRealPathFromURI(mContext,uri);
            if(filePath == null) {
                try {
//                        ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(uri, "r");
//                        FileInputStream fis = new FileInputStream(pfd.getFileDescriptor());
                    InputStream is = mContext.getContentResolver().openInputStream(uri);

//                    Toast.makeText(mContext.getApplicationContext(), uri.getPath(), Toast.LENGTH_LONG).show();
                    String[] temp = uri.getPath().split("/");
                    String fileName = temp[temp.length -1];
                    if(uri.getPath().toLowerCase().endsWith(".mp4") == false) {
                        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
                        int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        cursor.moveToFirst();
//                        Toast.makeText(mContext.getApplicationContext(), cursor.getString(columnIndex), Toast.LENGTH_LONG).show();
                        fileName = cursor.getString(columnIndex);
                    }
                    filePath = teslaDashCamPath + fileName;

                    FileOutputStream fos = new FileOutputStream(new File(filePath));
                    byte[] buf = new byte[1024];
                    int len = 0;
                    while((len = is.read(buf)) > 0 ){
                        fos.write(buf, 0, len);
                    }
                    fos.close();
                    is.close();
//
//                        FileChannel inFileChannel = fis.getChannel();
//                        FileChannel outFileChannel = fos.getChannel();
//                        long size = inFileChannel.size();
//                        inFileChannel.transferTo(0, size, outFileChannel);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.d("james_ffmpeg", filePath);
            return encoding(filePath);
        }
    }
    View.OnClickListener encoding_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            startActivityForResult(intent, REQUEST_ENCODING);
        }
    };
    View.OnClickListener play_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
            startActivity(intent);

        }
    };
    View.OnClickListener oneplay_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, OnePlayerActivity.class);
            startActivity(intent);

        }
    };

    public static String encoding(String path) {
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        String command = "-i " + path + "/Download/2019-10-14_17-47-41-back.mp4 -vf \"drawtext=fontfile=/system/fonts/DroidSans.ttf: timecode='17\\:47\\:41\\:00':rate=30:fontsize=30:fontcolor=white:x=10:y=32: box=1: boxcolor=0x00000000@1, drawtext=fontfile=/system/fonts/DroidSans.ttf:text='2019/10/14':fontsize=20:fontcolor=white:x=10:y=10: box=1: boxcolor=0x00000000@1\" -y -qscale:v 6 -f mp4 /sdcard/Download/output.mp4";
        Log.d("james_ffmpeg", path);
        String[] strings = path.split("/");
        String filename = strings[strings.length - 1];
        String[] temps = filename.split("_");
        String[] date = temps[0].split("-");
        String[] time = temps[1].split("-");

        String outFilename = teslaDashCamPath + "[time]" + filename.substring(0, filename.length()-4) +".mp4";
//        String outFilename = sdcardPath + "/TeslaDashCam/" + filename.substring(0, filename.length()-4) +"_time.mp4";
        String command = "-i " + path + " -vf \"drawtext=fontfile=/system/fonts/DroidSans.ttf: timecode='" + time[0] + "\\:" + time[1] + "\\:" + time[2] + "\\:00':rate=30:fontsize=30:fontcolor=white:x=10:y=32: box=1: boxcolor=0x00000000@1, drawtext=fontfile=/system/fonts/DroidSans.ttf:text='" + date[0] + "/" + date[1] + "/" + date[2] + "':fontsize=20:fontcolor=white:x=10:y=10: box=1: boxcolor=0x00000000@1\" -y -qscale:v 6 -f mp4 " + outFilename;
//        String command = "-i " + path + " -vf \"drawtext=fontfile=/system/fonts/DroidSans.ttf: timecode='" + time[0] + "\\:" + time[1] + "\\:" + time[2] + "\\:00':rate=30:fontsize=30:fontcolor=white:x=10:y=32: box=1: boxcolor=0x00000000@1, drawtext=fontfile=/system/fonts/DroidSans.ttf:text='" + date[0] + "/" + date[1] + "/" + date[2] + "':fontsize=20:fontcolor=white:x=10:y=10: box=1: boxcolor=0x00000000@1\" -y -qscale:v 6 -f mp4 " + path.substring(0, path.length()-4) +"_time.mp4";

//        String command = "-i " + path + " -vf \"drawtext=fontfile=/system/fonts/DroidSans.ttf: timecode='17\\:47\\:41\\:00':rate=30:fontsize=30:fontcolor=white:x=10:y=32: box=1: boxcolor=0x00000000@1, drawtext=fontfile=/system/fonts/DroidSans.ttf:text='2019/10/14':fontsize=20:fontcolor=white:x=10:y=10: box=1: boxcolor=0x00000000@1\" -y -qscale:v 6 -f mp4 " + path.substring(0, path.length()-4) +"_time.mp4";

        int rc = FFmpeg.execute(command);
        String result;
        if (rc == RETURN_CODE_SUCCESS) {
            result = "encoding done! " + outFilename;
            Log.i(Config.TAG, "Command execution completed successfully.");
        } else if (rc == RETURN_CODE_CANCEL) {
            result = "encoding failed!";
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            result = "encoding failed!";
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
        }
        return result;
    }





}
