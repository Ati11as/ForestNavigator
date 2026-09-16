package com.ati11as.forestnavigator

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.*
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var map: MapView
    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech
    private val prefs by lazy { getSharedPreferences("forest", MODE_PRIVATE) }
    private val points = mutableListOf<WP>()
    private var selected: WP? = null
    private var lastLocation: Location? = null
    private val req = 42

    override fun onCreate(b: Bundle?) { super.onCreate(b)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        tts = TextToSpeech(this, this)
        loadPoints()
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        status = TextView(this).apply { text="ForestNavigator • GPS не запущен"; setPadding(16,12,16,12) }
        map = MapView(this).apply { setTileSource(TileSourceFactory.MAPNIK); setMultiTouchControls(true); controller.setZoom(13.0); controller.setCenter(GeoPoint(54.9,23.9)) }
        root.addView(status)
        root.addView(map, LinearLayout.LayoutParams(-1,0,1f))
        val bar=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        fun btn(text:String, action:()->Unit)=Button(this).apply{this.text=text;setOnClickListener{action()}}
        bar.addView(btn("Моя позиция"){locate()}); bar.addView(btn("Сохранить точку"){savePoint()})
        bar.addView(btn("Трек"){toggleTrack()}); bar.addView(btn("Точки"){choosePoint()})
        root.addView(bar); setContentView(root)
        if(!hasLocation()) ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),req)
        map.setOnTouchListener { _,_ -> false }
    }
    private fun hasLocation()=ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
    private fun locate(){ if(!hasLocation()){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),req);return}; val lm=getSystemService(LOCATION_SERVICE) as android.location.LocationManager; lastLocation=lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER); lastLocation?.let{map.controller.animateTo(GeoPoint(it.latitude,it.longitude)); status.text="GPS: %.5f, %.5f • ±%.0fm".format(it.latitude,it.longitude,it.accuracy)} }
    private fun savePoint(){ val l=lastLocation ?: run{Toast.makeText(this,"Сначала нажмите «Моя позиция»",Toast.LENGTH_SHORT).show();return}; val input=EditText(this); input.hint="Название точки"; AlertDialog.Builder(this).setTitle("Сохранить точку").setView(input).setPositiveButton("Сохранить"){_,_-> points.add(WP(input.text.toString().ifBlank{"Точка ${points.size+1}"},l.latitude,l.longitude)); persistPoints(); redraw() }.setNegativeButton("Отмена",null).show() }
    private fun choosePoint(){ if(points.isEmpty()){Toast.makeText(this,"Сохранённых точек нет",Toast.LENGTH_SHORT).show();return}; AlertDialog.Builder(this).setTitle("Куда идти?").setItems(points.map{it.name}.toTypedArray()){_,i-> selected=points[i]; navigate(points[i]) }.show() }
    private fun navigate(p:WP){ map.controller.animateTo(GeoPoint(p.lat,p.lon)); redraw(); speak("Точка ${p.name}. Расстояние до точки будет показано при движении") }
    private fun speak(s:String){if(::tts.isInitialized) tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"forest")}
    private fun toggleTrack(){ val i=Intent(this,TrackingService::class.java).setAction(if(prefs.getBoolean("tracking",false)) "STOP" else "START"); ContextCompat.startForegroundService(this,i); prefs.edit().putBoolean("tracking",!prefs.getBoolean("tracking",false)).apply(); status.text=if(prefs.getBoolean("tracking",false))"🔴 Запись трека" else "Трек остановлен" }
    private fun redraw(){ map.overlays.removeAll{it is Marker}; points.forEach{p-> Marker(map).apply{position=GeoPoint(p.lat,p.lon);title=p.name;snippet="%.5f, %.5f".format(p.lat,p.lon);setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);map.overlays.add(this)}}; map.invalidate() }
    private fun loadPoints(){ val raw=prefs.getString("points","") ?: ""; raw.split(";").filter{it.isNotBlank()}.forEach{val a=it.split("|");if(a.size==3)points.add(WP(a[0],a[1].toDouble(),a[2].toDouble()))} }
    private fun persistPoints(){prefs.edit().putString("points",points.joinToString(";"){"${it.name}|${it.lat}|${it.lon}"}).apply();redraw()}
    override fun onInit(status:Int){if(status==TextToSpeech.SUCCESS)tts.language=Locale("ru","RU")}
    override fun onResume(){super.onResume();map.onResume();redraw()}; override fun onPause(){map.onPause();super.onPause()}
    override fun onDestroy(){tts.shutdown();super.onDestroy()}
    data class WP(val name:String,val lat:Double,val lon:Double)
}
