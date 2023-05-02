package com.example    .gps_calibration

import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class TcpServer(private val context: MainActivity, private val port: Int) {

    private lateinit var serverSocket: ServerSocket
    private lateinit var threadPool: ThreadPoolExecutor
    @Volatile var running: Boolean = true
    @Volatile var sendPosition: Boolean = true
    private var locationProvider: LocationProvider = LocationProvider(context)
    private var longitude = 0.0
    private var latitude = 0.0
    private var altitude = 0.0



    fun start() {
        running = true
        serverSocket = ServerSocket(port)
        threadPool = Executors.newFixedThreadPool(10) as ThreadPoolExecutor
        println("Server running on port $port")

        Thread {
            while (running) {
                try {
                    val clientSocket = serverSocket.accept()
                    println("Client connected: ${clientSocket.remoteSocketAddress}")
                    threadPool.execute {
                        handleClient(clientSocket)
                    }
                } catch (e: Exception) {
                    println("Connection closed")
                }
            }
            serverSocket.close()
            threadPool.shutdown()
        }.start()
    }

    fun stop() {
        running = false
        sendPosition = false // stop sending GPS position updates
        try {
            println("Server closed on port $port")
            // Close all client connections
            threadPool.shutdownNow()
            threadPool.awaitTermination(2, TimeUnit.SECONDS)

        } catch (e: InterruptedException) {
            println("Error while stopping server: ${e.message}")
        }
        serverSocket.close()
    }

    private fun handleClient(socket: Socket) {
        try {
            val input = socket.getInputStream()
            val output = socket.getOutputStream()

            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (running && !socket.isClosed) {
                bytesRead = input.read(buffer)
                if (bytesRead == -1) {
                    break
                }
                val message = String(buffer, 0, bytesRead)
                println("Received message from ${socket.remoteSocketAddress}: $message")
                processMessage(message, socket, output)
                locationProvider.getCurrentLocation {  }

            }
        } catch (e: SocketException) {
            println("SocketException: ${e.message}")
        } finally {
            if(!sendPosition){
                socket.close()
            }
        }
    }


    private fun processMessage(message: String, socket : Socket, output : OutputStream): String {
        if(message == "hello"){
            try {
                output.write("hello!".toByteArray())
                MainActivity.notconText.visibility = View.INVISIBLE
                MainActivity.conText.visibility = View.VISIBLE
            }
            catch(e : Exception){
                println("There was an error proceeding the hello message: $e")
            }

        }
        if(message == "!POS"){
            sendPosition = true
            threadPool.execute{sendPosition(output, socket)}
        }
        return "Response to $message"
    }

    private fun sendPosition(output: OutputStream, socket: Socket) {
        var counter = 0
        while(sendPosition && !socket.isClosed){
            try {
                locationProvider.getCurrentLocation { location ->
                    longitude = location?.longitude ?: 0.0
                    latitude = location?.latitude ?: 0.0
                    altitude = location?.altitude ?: 0.0
                    MainActivity.longText.text = "Longitude: $longitude"
                    MainActivity.latText.text = "Latitude: $latitude"
                    MainActivity.altText.text = "Altitude: $altitude"
                }
                if (longitude != 0.0 || latitude != 0.0){
                    output.write(("$longitude : $latitude").toByteArray())
                    println("message sent!")
                    output.flush()
                    Thread.sleep(1000)
                }
            }
            catch(e : Exception){
                println("No connection available")
                counter++

                if(counter > 1){
                    sendPosition = false
                    break
                }
            }
        }
        try {
            output.write("!".toByteArray())
            socket.close()
        }
        catch (e : Exception){
            println("Socket already closed")
        }
    }
}