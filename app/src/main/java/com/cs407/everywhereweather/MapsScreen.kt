package com.cs407.everywhereweather

import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cs407.everywhereweather.databinding.FragmentMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsScreen : Fragment() {

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var mDestinationLatLng: LatLng
    private val activityContext = requireActivity().applicationContext

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMapsBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync { googleMap: GoogleMap ->
            mMap = googleMap
            mDestinationLatLng = LatLng(43.0753, -89.4034)
            setLocationMarker(mDestinationLatLng, "Bascom Hall")
            checkLocationPermissionAndDrawPolyline(mDestinationLatLng)
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activityContext)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun setLocationMarker(destination: LatLng, destinationName: String) {
        mMap.addMarker(
            MarkerOptions()
                .title(destinationName)
                .position(destination)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15f))
    }

    fun checkLocationPermissionAndDrawPolyline(destination: LatLng) {

        if (ContextCompat.checkSelfPermission(activityContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }

        if (ContextCompat.checkSelfPermission(activityContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),1)
        }


        var currentLatLng = LatLng(43.0753, -89.4034)
        mFusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location : Location ->
                currentLatLng = LatLng(location.latitude , location.longitude)
                Log.i("Latitude","${location.latitude}")
                Log.i("Longitude", "${location.longitude}")
                Log.i("LatLng","$currentLatLng")
                setLocationMarker(currentLatLng, "Current Location")
                mMap.addPolyline(
                    PolylineOptions()
                        .clickable(true)
                        .add(destination,
                            currentLatLng))
            }
    }
}