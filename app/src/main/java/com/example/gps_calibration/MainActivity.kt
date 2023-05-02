package com.example.gps_calibration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object{
        // Declare longText and latText as properties
        lateinit var longText: TextView
        lateinit var latText: TextView
        lateinit var altText: TextView
        lateinit var notconText: TextView
        lateinit var conText: TextView
    }


    private lateinit var locationProvider: LocationProvider
    private lateinit var server : TcpServer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        server = TcpServer(this, 8005)

        val startBtn = findViewById<Button>(R.id.start)
        val stopBtn = findViewById<Button>(R.id.stop)

        longText = findViewById<TextView>(R.id.Longitude)
        latText = findViewById<TextView>(R.id.Latitude)
        altText = findViewById<TextView>(R.id.Altitude)
        notconText = findViewById<TextView>(R.id.not_connected)
        conText = findViewById<TextView>(R.id.connected)

        locationProvider = LocationProvider(this)

        startBtn.setOnClickListener {
            startBtn.visibility = View.INVISIBLE
            stopBtn.visibility = View.VISIBLE
            thread { server.start() }
        }

        stopBtn.setOnClickListener {
            server.running = false
            server.stop()
            startBtn.visibility = View.VISIBLE
            stopBtn.visibility = View.INVISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        server.stop()
    }
}
