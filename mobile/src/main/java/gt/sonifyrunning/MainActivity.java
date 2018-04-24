/**
 * Mihir Parshionikar. 4/3/2018
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
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private float[] accelBuffer = {0f, 0f, 0f}; //stores old accelerometer values
    private TextView textView, textView2, cadenceTextView;
    private EditText editText;
    private Date startTime;
    private SeekBar seekBar, cadenceSeekBar;

    private String baseDir, fileName, filePath;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean active = false;

    private SoundPool soundPool;
    private int soundID[] = {-1, -1, -1};
    private int streamID[] = {-1, -1, -1};
    private float volume[] = {1f, 0f, 1f};
    private boolean soundsLoaded = false;
    private Button startButton;
    private int sensorIndex = 0; //indicates where in sensorReadings we currently are

    private int stepLength = 300; //the amount of time each step takes (ms)
    private long stepStartTime;
    private long stepEndTime;
    private int numSteps = 0;
    boolean checkForStep = true;
    private float cadenceBuffer = 0; //steps per minute
    private int idealCadence = 90;
    private float rateBuffer = 1f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.verifyStoragePermissions(this);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        editText = findViewById(R.id.editText);
        startButton = findViewById(R.id.startButton);
        seekBar = findViewById(R.id.seekBar);
        cadenceSeekBar = findViewById(R.id.cadenceSeekBar);
        cadenceTextView = findViewById(R.id.cadenceTextView);

        initSound();
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!active) {
                    active = true;
                    editText.setEnabled(false);
                    startButton.setEnabled(false);
                    cadenceSeekBar.setEnabled(false);
                    cadenceTextView.setEnabled(false);

                    idealCadence = cadenceSeekBar.getProgress();

                    //set file name
                    //if file exists already, delete
                    baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                    fileName = editText.getText() + ".csv";
                    filePath = baseDir + File.separator + fileName;
                    File f = new File(filePath);
                    if (f.exists()) {
                        f.delete();
                    }
                    startTime = Calendar.getInstance().getTime();
                    streamID[0] = soundPool.play(soundID[0], 0, 0, 1, -1, 1f); //MUTED
                    streamID[2] = soundPool.play(soundID[2], 1, 1, 1, -1, 1f);
                }
            }
        });
        cadenceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cadenceTextView.setText(String.valueOf(i));
                idealCadence = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initSound() {
        Context mContext = getApplicationContext();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .build();
        soundID[0] = soundPool.load(mContext, R.raw.synth1, 1); //root
        soundID[1] = soundPool.load(mContext, R.raw.beep1, 1); //beep sound
        soundID[2] = soundPool.load(mContext, R.raw.interval1, 1); //5th interval sound
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundsLoaded = true;
            }
        });
    }
    private float map(float value, float lowerInit, float upperInit, float lowerFinal, float upperFinal) {
        float lower_shift = lowerFinal - lowerInit;
        float scale = (upperFinal - lowerFinal) / (upperInit - lowerInit);
        return (value + lower_shift) * scale;
    }
    /**
        Takes in new sensor readings and updates SoundPool stream, and saves readings to file
     **/
    private void update(float accel_x, float accel_y, float accel_z) {
        float x = accelBuffer[0];
        float y = accelBuffer[1];
        float z = accelBuffer[2];

        String line = ""; //Time(ms), x, y ,z
        double time = Calendar.getInstance().getTime().getTime() - startTime.getTime();
        line = String.format("%.00f, %.04f, %.04f, %.04f\n", time, x, y, z);
        textView.setText(line);
        writeToFile(line);
        textView2.setText(String.format("Cadence: %.04f", cadenceBuffer));
        if (soundsLoaded) {
            //volume[0] = Math.max(Math.min(1, volume[0] + map(y, 9.4f, 13f, -1, 1)), 0);
            volume[0] = map(y, 9.4f, 13f, 0f, 1);
            //Log.d("Gain", String.valueOf(volume[0]));
            soundPool.setVolume(streamID[0], volume[0], volume[0]);
            seekBar.setProgress((int) (volume[0] * 100));
            if (checkForStep) {
                float dx = accel_x - accelBuffer[0];
                float dy = accel_y - accelBuffer[1];
                float dz = accel_z - accelBuffer[2];
                if (dy > 2f) {
                    //STEP HAS BEGUN
                    stepEndTime = stepStartTime;
                    stepStartTime = Calendar.getInstance().getTime().getTime();
                    checkForStep = false;
                    //Log.d("Step", "Start");
                    //streamID[1] = soundPool.play(soundID[1], 1, 1, 1, 0, 1f); //step sound
                }
            } else {
                if (Calendar.getInstance().getTime().getTime() - stepStartTime >= stepLength) {
                    checkForStep = true;
                    //Log.d("Step", "Stop");
                }
            }
            //SONIFY CADENCE
            //float rate = map(cadenceBuffer, idealCadence - 5, idealCadence + 5, .75f, 2f);
            if (cadenceBuffer < idealCadence - 5) {
                rateBuffer = .99f * (rateBuffer) + .01f * (rateBuffer * .99f);
            } else if (cadenceBuffer > idealCadence + 5) {
                rateBuffer = .99f * (rateBuffer) + .01f * (rateBuffer * 1.01f);
            } else {
                rateBuffer = .99f * (rateBuffer) + .01f * (1f);
            }
            rateBuffer = Math.max(Math.min(1.25f, rateBuffer), .75f); //ensure rateBuffer stays within .75 to 1.25
            Log.d("rate", String.valueOf(rateBuffer));
            soundPool.setRate(streamID[2], rateBuffer);
        }
        //float interval = stepStartTime - stepEndTime; //problem this line causes cadence buffer to get stuck if there are no steps
        float interval = Calendar.getInstance().getTime().getTime() - stepEndTime;
        cadenceBuffer = cadenceBuffer * .99f + .01f * (((1f) * 60 * 1000)/(interval));
        //Log.d("interval", String.valueOf(interval));

        accelBuffer[0] = accelBuffer[0] * .8f + .2f * accel_x;
        accelBuffer[1] = accelBuffer[1] * .8f + .2f * accel_y;
        accelBuffer[2] = accelBuffer[2] * .8f + .2f * accel_z;
    }
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (active) {
            Sensor mySensor = sensorEvent.sensor;
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                update(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
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
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
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
