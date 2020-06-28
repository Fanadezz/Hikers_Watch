package com.androidshowtime.hikerswatch

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    //static properties inside the Companion Object
    companion object {
        //request code for us on onRequestPermissionsResult
        private const val PERMISSION_REQUEST_CODE = 1

        //location priority specification
        private const val LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY

    }

    private var locationPermissionGranted = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var newLocation: Location

    //callback
    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)


            //null check
            if (locationResult == null) {
                return
            }

            //once the location is changed update and set the newLocation
            locationResult.locations.forEach { newLocation = it }


            val geoCoder = Geocoder(applicationContext, Locale.getDefault())

            try {
                val listAddresses =
                        geoCoder.getFromLocation(newLocation.latitude, newLocation.longitude, 1)

                //null check on the listAddresses using let{}
                listAddresses?.let {

                    if (it.size > 0) {

                        getListAddressInfo(listAddresses, newLocation)
                    }
                }
            }
            catch (e: Exception) {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //request for permissions
        getLocationPermission()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().setFastestInterval(3000).setInterval(5000)
                .setPriority(LOCATION_PRIORITY)

        //starting location updates
        startLocationUpdates()

    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
                                           ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true

                    //Explain to the user that the feature is unavailable
                }
                else {

                    Toast.makeText(
                            this, "App won't work with this permission", Toast.LENGTH_LONG
                                  ).show()
                }

                return
            }
        }

    }


    /* Request location permission, so that we can get the location of the
       device. The result of the permission request is handled by a callback,
       onRequestPermissionsResult */

    private fun getLocationPermission() {
        //Determine whether you have been granted a particular permission.
        when {
            ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                                             ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                locationPermissionGranted = true
            }
            //check if the system recommends a rationale dialog before asking permission
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "Location Permission Needed", Toast.LENGTH_SHORT)
                        .show()
            }

            // Show the permission request dialog
            else -> {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_REQUEST_CODE

                                                 )
            }
        }
    }

    //update TextViews with values
    private fun populateTextViews(latitude: Double, longitude: Double, accuracy: Float, altitude:
    Double, address: String) {

        latitudeTextView.text = getString(R.string.lat_label, latitude)
        longitudeTextView.text = getString(R.string.lng_label, longitude)
        accuracyTextView.text = getString(R.string.acc_label, accuracy)
        altitudeTextView.text = getString(R.string.alt_label, altitude)
        addressTextView.text = getString(R.string.add_label, address)

    }

    //get properties from both the address and loction
    private fun getListAddressInfo(list: List<Address>?, location: Location) {

        val lat = location.latitude
        val lng = location.longitude
        val acc = location.accuracy
        val alt = location.altitude

        var address = ""
        //get address from addressLine
        address += list?.get(0)?.getAddressLine(0)

        //calling method to update the TextViews
        populateTextViews(lat, lng, acc, alt, address)

    }

    //starting location updates method
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        if (locationPermissionGranted) {

            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.getMainLooper())

        }

    }
}