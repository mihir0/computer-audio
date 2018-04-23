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

public class MainActivity extends AppCompatActivity /*implements SensorEventListener*/ {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private TextView textView;
    private EditText editText;
    private Date startTime;

    private String baseDir, fileName, filePath;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean active = false;

    private SoundPool soundPool;
    private int soundID = -1;
    private int streamID;
    private float volume = 50;
    private boolean soundsLoaded = false;
    private Button startButton;
    private Button soundButton;
    private SeekBar seekBar;
    private Handler mHandler;
    private Timer timer;
    private ArrayList<String[]> sensorReadings;
    private int sensorIndex = 0; //indicates where in sensorReadings we currently are
    private void initSound() {
        Context mContext = getApplicationContext();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .build();
        soundID = soundPool.load(mContext, R.raw.test_1, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundsLoaded = true;
            }
        });
    }

    private ArrayList<String[]> readCSV() throws IOException{
        ArrayList<String[]> data = new ArrayList<String[]>();
        baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName = editText.getText() + ".csv";
        filePath = baseDir + File.separator + fileName;
        Log.d("File path", filePath);
        try {
            CSVReader reader = new CSVReader(new FileReader(filePath));
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                data.add(nextLine);
            }
        } catch(IOException e) {
            throw e;
        }
        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.verifyStoragePermissions(this);
        textView = findViewById(R.id.textView);
        editText = findViewById(R.id.editText);
        startButton = findViewById(R.id.startButton);
        soundButton = findViewById(R.id.soundButton);
        soundButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                streamID = soundPool.play(soundID, 1, 1, 1, -1, 1f);
            }
        });
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                volume = (float) progress;
                soundPool.setVolume(streamID, volume / 100, volume / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    active = true;
                    timer = new Timer();
                    sensorReadings = readCSV();
                    startTime = Calendar.getInstance().getTime();
                    sensorIndex = 0;

                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (active) {
                                long currentTime = Calendar.getInstance().getTime().getTime() - startTime.getTime();
                                Log.d("Current Time:", String.valueOf(currentTime) + ", " + String.valueOf(sensorIndex));
                                while (Long.parseLong(sensorReadings.get(sensorIndex)[1].trim()) < currentTime) {
                                    sensorIndex++;
                                    if (sensorIndex == sensorReadings.size()) { //End of file
                                        timer = null;
                                        active = false;
                                        startTime = null;
                                        break;
                                    }
                                }
                                float x = Float.parseFloat(sensorReadings.get(sensorIndex)[2].trim());
                                float y = Float.parseFloat(sensorReadings.get(sensorIndex)[3].trim());
                                float z = Float.parseFloat(sensorReadings.get(sensorIndex)[4].trim());
                                sensorUpdate(x, y, z);
                            }
                        }
                    }, 0, 1000/20);
                } catch (IOException e) {
                    textView.setText("File not found!");
                }
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
                //writeToFile(line);
            }
        }
    }

    public void sensorUpdate(float x, float y, float z) { //called when SIMULATING real time sensor data from stored .csv
        float accel_mag = Math.abs(x) + Math.abs(y) + Math.abs(z);
        String line = ""; //Time(ms), x, y ,z
        double time = Calendar.getInstance().getTime().getTime() - startTime.getTime();
        line = String.format("%.00f, %.04f, %.04f, %.04f\n", time, x, y, z);
        Log.d("Sensor: ", line);
        //textView.setText(line);
    }
    /*
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
