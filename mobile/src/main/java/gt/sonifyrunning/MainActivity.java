/**
 * Mihir Parshionikar. 4/3/2018
 * Code based on https://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
 **/
package gt.sonifyrunning;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import net.mabboud.android_tone_player.ContinuousBuzzer;
import net.mabboud.android_tone_player.OneTimeBuzzer;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private TextView textView;
    //private ContinuousBuzzer tonePlayer;
    private OneTimeBuzzer tonePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("tag1", "On Create.");
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        textView = (TextView) findViewById(R.id.textView);
        //tonePlayer = new ContinuousBuzzer();
        tonePlayer = new OneTimeBuzzer();

        //tonePlayer.setPauseTimeInMs(0);
        //tonePlayer.setPausePeriodSeconds(1);
        tonePlayer.setToneFreqInHz(800);
        tonePlayer.setDuration(1);
        //tonePlayer.play();
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        Log.d("sensor", "Sensor changed");
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            float accel_mag = Math.abs(x) + Math.abs(y) + Math.abs(z);
            String info = String.format("x: %.04f, y: %.04f, z: %.04f, Total Accel. Mag: %.04f", x, y, z, accel_mag);
            Log.d("myTag", info);
            textView.setText(info);
            //tonePlayer.setVolume(50);
            //tonePlayer.stop();
            tonePlayer.setToneFreqInHz(Math.abs(x) * 400);
            tonePlayer.play();

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
}
