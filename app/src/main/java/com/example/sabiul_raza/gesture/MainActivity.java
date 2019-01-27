package com.example.sabiul_raza.gesture;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    
    private SensorManager sensorManager;
    Sensor accelerometer;

    TextView xValue, yValue, zValue;
    List<Float> xval = new ArrayList <Float>();
    List<Float> yval = new ArrayList <Float>();
    List<Float> zval = new ArrayList <Float>();
    List <List <Float>> merge1 = new ArrayList <>();




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        List <List <Float>> testData = this.readFile(R.raw.c1);
//        this.readRawFolder(testData);
    }

    private static final float[] primitive(final List<Float> pList) {
        // Declare the Array.
        final float[] lT = new float[pList.size()];
        // Iterate the List.
        for(int i = 0; i < pList.size(); i++) {
            // Buffer the Element.
            lT[i] = pList.get(i);
        }
        // Return the Array.
        return lT;
    }


    public void readRawFolder(List <List <Float>> testData) {
        Field[] fields = R.raw.class.getFields();
        for (int count = 0; count < fields.length; count++) {
            int stringFIleName = getResources().getIdentifier(fields[count].getName(),
                    "raw", getPackageName()); // get dynamic files

            List <List <Float>> trainingData = this.readFile(stringFIleName);
            this.compare(testData, trainingData);
        }
    }


    public void compare(List <List <Float>> testData, List <List <Float>> training) {
            final DTW lDTW = new DTW();
            final double[] lAverages = new double[3];

            for (int i = 0; i < 3; i++) {
                final float[] lTraining = MainActivity.primitive(training.get(i));
                final float[] lRecognition = MainActivity.primitive(testData.get(i));
                lAverages[i] = lDTW.compute(lTraining, lRecognition).getDistance();

                final double checkr = (50.0/360.0);
                Toast.makeText(MainActivity.this, "D(X:" + lAverages[0] + ", Y:" + lAverages[1] + ", Z:" + lAverages[2] + ")", Toast.LENGTH_LONG).show();
                Log.d("lAverage ", "   "+i+"   "  + lAverages[i]);

//                if ((lAverages[0]<=checkr) && (lAverages[1]<=checkr) && (lAverages[2]<=checkr)) {
//                    Toast.makeText(MainActivity.this, "D(X:" + lAverages[0] + ", Y:" + lAverages[1] + ", Z:" + lAverages[2] + ")", Toast.LENGTH_LONG).show();
//                    Log.d("lAverage ", "   "+i+"   "  + lAverages[i]);
//
//
//                }
            }
        }


    public List <List <Float>> readFile (int c1) {
        // read file and store x, y, z into array list
        InputStream inputStream = getResources().openRawResource(c1);
        String[] ids;
        List<Float> xs = new ArrayList <Float>();
        List<Float> ys = new ArrayList <Float>();
        List<Float> zs = new ArrayList <Float>();
        List <List <Float>> merge = new ArrayList <>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                ids = csvLine.split(",");

                try {
                    xs.add(Float.parseFloat(ids[0])) ;// readind x-axis
                    ys.add(Float.parseFloat(ids[1]));// readind y-axis
                    zs.add(Float.parseFloat(ids[2]));// readind z-axis
                } catch (Exception e) {
                    Log.d("Unknown fuck", e.toString());
                }
            }

            merge.add(xs);
            merge.add(ys);
            merge.add(zs);
//            Log.d("lAverage ", "" + merge);

            return merge;

        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        xValue.setText("xValue:" + sensorEvent.values[0]);
        yValue.setText("yValue:" + sensorEvent.values[1]);
        zValue.setText("zValue:" + sensorEvent.values[2]);

        xval.add(Float.parseFloat(String.valueOf(sensorEvent.values[0]))) ;// readind x-axis
        yval.add(Float.parseFloat(String.valueOf(sensorEvent.values[1])));// readind y-axis
        zval.add(Float.parseFloat(String.valueOf(sensorEvent.values[2])));// readind z-axis

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action,keycode;

        action = event.getAction();
        keycode = event.getKeyCode();

        switch(keycode){

            case KeyEvent.KEYCODE_VOLUME_DOWN:
            {
                if (KeyEvent.ACTION_DOWN== action)
                {

                    this.make_test_file();

                }
            }
            break;

        }

        return super.dispatchKeyEvent(event);
    }

    public void make_test_file (){

        final SensorEventListener listener = this;

        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);

        Log.d(TAG, "onCreate: Initializing Sensor Service");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        Log.d(TAG, "onCreate: Registered accelerometer listener");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sensorManager.unregisterListener(listener);
                MainActivity.this.handleMergeValues();

            }
        }, 3000);

    }

    public void handleMergeValues () {
        merge1.add(xval);                              //putting all X values in merge1
        merge1.add(yval);                              //putting all Y values in merge1
        merge1.add(zval);

        Log.d("res", "res"+ merge1);

        this.readRawFolder(merge1);
        this.createCSVFile(merge1);
    }

    public void createCSVFile(List <List <Float>> merge1) {
        CSVWriter writer = null;
        File file = new File("data/data/test.csv");
        if (!file.exists()) {
            try {
                file.createNewFile();
                writer = new CSVWriter(new FileWriter("data/data/test.csv"), ',');
                String entries = merge1.get(0).toString(); // array of your values
                writer.writeNext(new String[]{entries});
                writer.close();
            } catch (IOException e) {
//                e.printStackTrace();
                Log.d(TAG, "createCSVFile: " + e);
            }
        }

    }


}
