package com.example.mymaps

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Transformations.map

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mymaps.databinding.ActivityCreateMapsBinding
import com.example.mymaps.models.Place
import com.example.mymaps.models.UserMap
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar

import android.location.Location
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.example.mymaps.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.example.mymaps.PermissionUtils.isPermissionGranted
import com.example.mymaps.PermissionUtils.requestPermission
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

private const val TAG="CreateMapsActivity"

class CreateMapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener,
    OnMyLocationClickListener, OnRequestPermissionsResultCallback {

    private lateinit var mMap: GoogleMap
    private var permissionDenied = false
    private lateinit var binding: ActivityCreateMapsBinding
    private val markers:MutableList<Marker> = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title=intent.getStringExtra(EXTRA_MAP_TITLE)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapFragment.view?.let {
            Snackbar.make(it,"Long Press to Add Marker",Snackbar.LENGTH_INDEFINITE)
                .setAction("OK") {}
                .setActionTextColor(Color.WHITE)
                .show()
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_map,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.miSave){
            Log.i(TAG,"Tapped on Saved")
            if (markers.isEmpty()) {
                Toast.makeText(this, "There should be atleast one Marker", Toast.LENGTH_SHORT).show()
                return true
            }
        val places= markers.map { marker ->Place(marker.title,marker.snippet,marker.position.latitude,marker.position.longitude)}
        val userMap= intent.getStringExtra(EXTRA_MAP_TITLE)?.let { UserMap(it,places) }
        val data= Intent()
        data.putExtra(EXTRA_USER_MAP,userMap)
        setResult(Activity.RESULT_OK,data)
        Log.i(TAG,"SENT the result to main Activity")
        finish()
        return true
        }
        return super.onOptionsItemSelected(item)
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

        mMap.setOnInfoWindowClickListener {
            markers.remove(it)
            it.remove()
        }
        mMap.setOnMapLongClickListener {
            showAlertDialog(it)

        }

        // Get the current location of the user and center the map around that area
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()


//        val delhi = LatLng(28.630088, 77.214306)
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi,10f))
    }

    private fun enableMyLocation() {
        if (!::mMap.isInitialized) return
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        // [END maps_check_location_permission]
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    private fun showAlertDialog(latLng: LatLng) {
        val placeFromView=LayoutInflater.from(this).inflate(R.layout.dialog_create_place,null)
        val dialog=
            AlertDialog.Builder(this)
            .setTitle("Create a new marker")
            .setView(placeFromView)
            .setNegativeButton("cancel",null)
            .setPositiveButton("Ok",null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {

            val title= placeFromView.findViewById<EditText>(R.id.etTitle).text.toString()
            val snippet=placeFromView.findViewById<EditText>(R.id.etDescription).text.toString()

            if(title.trim().isNotEmpty() && snippet.trim().isNotEmpty()){
                val marker=mMap.addMarker(MarkerOptions().position(latLng).title(title).snippet(snippet))
                markers.add(marker)
                dialog.dismiss()
            }else{

                Toast.makeText(this, " Please Fill Empty Fields!!", Toast.LENGTH_LONG).show()
            }
        }

    }
}