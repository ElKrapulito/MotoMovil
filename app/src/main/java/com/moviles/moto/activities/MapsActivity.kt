package com.moviles.moto.activities

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import com.moviles.moto.R
import com.moviles.moto.api.ApiMoto
import com.moviles.moto.models.*
import io.nlopez.smartlocation.SmartLocation
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.runBlocking
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.concurrent.thread

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var user:Usuario
    private  lateinit var markerPickUp: Marker
    private  var markerDelivery: Marker? = null
    private  var currentOrder:Pedido? = null
    private lateinit var currentLocation:Ubicacion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        user = intent.getSerializableExtra("usuario") as Usuario

        btnDeliver.isEnabled = false
        setUpListener()
        //beginUpdatePosition()

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION
            )
            return
        }
        currentOrder = Pedido(0,0,0,0.0, "0","por ahi", "asdf","asdf","123", Restaurant(0,"asdf","asdf","asdf","asdf","123"), 0)
        setMyLocation()
    }

    private fun setMyLocation(){
        mMap.isMyLocationEnabled = true
        val myLocation = mFusedLocationClient.lastLocation
        myLocation.addOnCompleteListener {
            if (myLocation.isSuccessful){
                val position = LatLng(myLocation.result!!.latitude,myLocation.result!!.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16.0f))
            }
        }
    }

    private fun setUpListener(){
        btnGetOrder.setOnClickListener {

            if(user.moto != null){
                findOrder(user.moto!!.id)
            } else {
                Toast.makeText(this,"No tienes moto!", Toast.LENGTH_SHORT).show()
            }

            /*if(currentOrder != null && currentOrder is Pedido){
                val pickUp = LatLng(currentOrder!!.restaurante.latitud.toDouble(), currentOrder!!.restaurante.longitud.toDouble())
                val delivery = LatLng(currentOrder!!.latitud.toDouble(), currentOrder!!.longitud.toDouble())
                markerPickUp = mMap.addMarker(MarkerOptions().position(pickUp).title(currentOrder!!.restaurante.nombre))
                markerDelivery = mMap.addMarker(MarkerOptions().position(delivery).icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)))
            }*/

            beginUpdatePosition()
        }

        btnDeliver.setOnClickListener {
            if(currentOrder != null){
               changeOrderState(currentOrder!!.id)
                currentOrder = null
            }
        }

        SmartLocation.with(this).location()
            .start { location ->
                 currentLocation = Ubicacion(location.latitude.toString(), location.longitude.toString())
            }
    }

    private fun findOrder(bikeId:Int){
        val retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiMoto::class.java)
        val request:ResponseGeneric<Moto>
        runBlocking {
            Log.e(TAG,"I'm hereee $bikeId")
            request = api.getBike(bikeId)
            Log.e(TAG,"I'm hereee ${request.data.estado}")
        }
        if(request.data.estado == "1"){
            getOrder(request.data.pedidoAsignado!!.toInt())
        } else {
            Toast.makeText(this, "No hay orden!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOrder(orderId:Int){
        val retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiMoto::class.java)
        val request:ResponseGeneric<Pedido>
        runBlocking {
            request = api.getPedidoDetalle(orderId)
        }
        currentOrder = request.data
        val pickUp = LatLng(currentOrder!!.restaurante.latitud.toDouble(), currentOrder!!.restaurante.longitud.toDouble())
        val delivery = LatLng(currentOrder!!.latitud.toDouble(), currentOrder!!.longitud.toDouble())
        markerPickUp = mMap.addMarker(MarkerOptions().position(pickUp).title(currentOrder!!.restaurante.nombre))
        markerDelivery = mMap.addMarker(MarkerOptions().position(delivery).icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)))

    }

    private fun changeOrderState(orderId:Int){
        val retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiMoto::class.java)
        val request = api.changeState(orderId)
        request.enqueue(object : Callback<ResponseGeneric<Pedido>>{
            override fun onFailure(call: Call<ResponseGeneric<Pedido>>, t: Throwable) {

            }

            override fun onResponse(
                call: Call<ResponseGeneric<Pedido>>,
                response: Response<ResponseGeneric<Pedido>>
            ) {
                Toast.makeText(this@MapsActivity, "Successfully delivered!", Toast.LENGTH_SHORT).show()
                btnDeliver.isEnabled = false
                mMap.clear()
                // currentOrder = null
            }

        })
    }

    private fun updatePosition(bikeId: Int,ubi: Ubicacion?){
        Log.d(TAG,"${ubi!!.latitud} ${ubi.longitud}")
        val retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiMoto::class.java)
        val request = api.updateLocation(bikeId, ubi)
        request.enqueue(object : Callback<ResponseGeneric<Moto>>{
            override fun onFailure(call: Call<ResponseGeneric<Moto>>, t: Throwable) {
                Log.e(TAG, "Error updating position ${t.message}")
            }

            override fun onResponse(
                call: Call<ResponseGeneric<Moto>>,
                response: Response<ResponseGeneric<Moto>>
            ) {
                Log.d(TAG, "Location updated: ${response.body()?.data}")
            }
        })
    }
    private fun sendUpdateBikePosition(){
        val myLocation = mFusedLocationClient.lastLocation
        var position:Ubicacion?
            myLocation.addOnCompleteListener {
                if (myLocation.isSuccessful){
                    position = Ubicacion(myLocation.result!!.latitude.toString(),myLocation.result!!.longitude.toString())
                    Log.d(TAG, "myLocation was successful, latitud: ${myLocation.result!!.latitude}, longitud: ${myLocation.result!!.longitude}")
                    updatePosition(user.moto!!.id,position)
                    if (markerDelivery != null){
                        if(SphericalUtil.computeDistanceBetween(LatLng(position!!.latitud.toDouble(), position!!.longitud.toDouble()), markerDelivery!!.position) < 30){
                            btnDeliver.isEnabled = true
                        }
                    }


                }
            }
    }

    private fun beginUpdatePosition(){
        thread {
            repeat(Int.MAX_VALUE){
                sendUpdateBikePosition()
                Log.d(TAG, "Thread running...")
                Thread.sleep(10 * 1000)
            }
        }
    }

    companion object{
        const val LOCATION_PERMISSION = 1
        const val apiURL = "http://delivery.jmacboy.com/api/"
        const val TAG = "LocationUpdate"
    }

}
