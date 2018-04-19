/**
 * Mihir Parshionikar. 4/3/2018
 * Code based on https://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
 **/
package gt.sonifyrunning;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.SoundPool;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity /*implements SensorEventListener*/ {
    /*
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private TextView textView;
    private EditText editText;
    private Button startButton;
    private Date startTime;

    private String baseDir, fileName, filePath; */

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean active = false;

    private SoundPool soundPool;
    private int soundID = -1;
    private int streamID;
    private boolean soundsLoaded = false;
    private Button startButton;
    private void initSound() {
        Context mContext = getApplicationContext();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .build();
        soundID = soundPool.load(mContext, R.raw.test_1, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId,int status) {
                soundsLoaded = true;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                streamID = soundPool.play(soundID, 1, 1, 1, 1, 1f);
            }
        });

        /*
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        MainActivity.verifyStoragePermissions(this); //verify/get permission to write file


        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!active) {
                    active = true;
                    editText.setEnabled(false);
                    startButton.setEnabled(false);
                    //set file name
                    //if file exists already, delete
                    baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                    fileName = editText.getText() + ".csv";
                    filePath = baseDir + File.separator + fileName;
                    File f = new File(filePath);
                    if (f.exists()) {
                        f.delete();
                    }
                    startTime = Calendar.getInstance().getTime(); //start timer
                }
            }
        }); */
        initSound();
    }

    /*
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (active) {
            Sensor mySensor = sensorEvent.sensor;
            //Log.d("sensor", "Sensor changed");
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                float accel_mag = Math.abs(x) + Math.abs(y) + Math.abs(z);

                String line = ""; //Time(ms), x, y ,z
                double time = Calendar.getInstance().getTime().getTime() - startTime.getTime();
                line = String.format("%.00f, %.04f, %.04f, %.04f\n", time, x, y, z);
                textView.setText(line);
                writeToFile(line);
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void writeToFile(String line) {
        try {
            FileWriter writer = new FileWriter(filePath, true);
            writer.append(line);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onDestroy() {
        soundPool.release();
        super.onDestroy();
        soundPool = null;
    }

}
