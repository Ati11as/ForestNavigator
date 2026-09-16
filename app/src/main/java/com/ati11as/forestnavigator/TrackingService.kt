package com.ati11as.forestnavigator

import android.app.*
import android.content.Intent
import android.location.*
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class TrackingService : Service(), LocationListener {
    private lateinit var lm: LocationManager
    private val points=mutableListOf<Location>()
    private var running=false
    override fun onCreate(){super.onCreate(); createChannel(); startForeground(7,notification("Подготовка GPS")); lm=getSystemService(LOCATION_SERVICE) as LocationManager}
    override fun onStartCommand(i:Intent?,flags:Int,id:Int):Int{if(i?.action=="STOP"){running=false;try{lm.removeUpdates(this)}catch(_:Exception){};stopForeground(STOP_FOREGROUND_REMOVE);stopSelf()}else if(i?.action=="START"){running=true;try{lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,5f,this)}catch(_:SecurityException){};update("Запись GPS-трека")};return START_STICKY}
    override fun onLocationChanged(l:Location){if(running){points.add(l);update("Запись трека • ${points.size} точек")}}
    override fun onDestroy(){try{lm.removeUpdates(this)}catch(_:Exception){};saveGpx();super.onDestroy()}
    private fun saveGpx(){if(points.isEmpty())return;val dir=getExternalFilesDir(null) ?: filesDir;val f=java.io.File(dir,"track-${SimpleDateFormat("yyyyMMdd-HHmmss",Locale.US).format(Date())}.gpx");f.writeText(buildString{append("<?xml version=\"1.0\"?><gpx version=\"1.1\" creator=\"ForestNavigator\"><trk><name>Forest track</name><trkseg>");points.forEach{append("<trkpt lat=\"${it.latitude}\" lon=\"${it.longitude}\"><ele>${it.altitude}</ele><time>${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).format(Date(it.time))}</time></trkpt>")};append("</trkseg></trk></gpx>")})}
    private fun createChannel(){getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("gps","GPS tracking",NotificationManager.IMPORTANCE_LOW))}
    private fun notification(s:String)=NotificationCompat.Builder(this,"gps").setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("ForestNavigator").setContentText(s).setOngoing(true).build()
    private fun update(s:String)=getSystemService(NotificationManager::class.java).notify(7,notification(s))
    override fun onBind(i:Intent?):IBinder?=null
}
