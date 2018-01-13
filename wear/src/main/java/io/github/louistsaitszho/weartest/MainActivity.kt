package io.github.louistsaitszho.weartest

import android.arch.persistence.room.Room
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileWriter

class MainActivity : WearableActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var db : AppDatabase
    private var running = false
    private var rawDataBucket = ArrayList<RawDataEntity>(BUCKET_CAPACITY)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "wear-test").build()

        button_start.setOnClickListener {
            running = running.not()
            button_start.text = if (running) "Stop" else "Start"

            //clear out everything old from the queue
            if (running.not()) {
                val oldData = ArrayList(rawDataBucket)
                rawDataBucket.clear()
                Thread(Runnable {
                    db.rawDataDao().insertRawDatas(oldData)
                }).start()
            }
        }

        button_export.setOnClickListener {
            Thread(Runnable {
                val results = db.rawDataDao().getAll()

                //TODO runtime permission check

                val root = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "wear-test")
                if (!root.mkdirs()) {
                    Log.e(TAG, "Directory not created")
                }
                val toBeWrite = File(root, "session_${System.currentTimeMillis()}")
                val fileWriter = FileWriter(toBeWrite)
                results.forEach {
                    fileWriter.append("$it\n")
                }
                fileWriter.close()
                Log.d(TAG, "file written")
                db.rawDataDao().nuke()
            }).start()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        allSensors.forEach {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        TODO("not implemented")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (running) {
            val rde = RawDataEntity(event)
            if (rawDataBucket.size < BUCKET_CAPACITY) {
                rawDataBucket.add(rde)
            } else {
                //make a copy of data for insertion (no lock)
                val oldData = ArrayList(rawDataBucket)
                rawDataBucket.clear()
                rawDataBucket.add(rde)

                Thread(Runnable {
                    db.rawDataDao().insertRawDatas(oldData)
                    Log.d(TAG, "$BUCKET_CAPACITY raw data inserted")
                }).start()
            }
        }
    }

    private companion object {
        private const val TAG = "MainActivity"
        private const val BUCKET_CAPACITY = 1000
    }

    fun isEnternalStorageWritable() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}