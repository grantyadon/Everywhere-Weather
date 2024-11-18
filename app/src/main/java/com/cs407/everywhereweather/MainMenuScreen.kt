package com.cs407.everywhereweather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainMenuScreen : Fragment() {

    private lateinit var newTripButton: Button
    private lateinit var savedTripsButton: Button
    private lateinit var pastTripsButton: Button

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchUserLocation()
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mainmenu, container, false)

        // Initialize buttons
        newTripButton = view.findViewById(R.id.newTripButton)
        savedTripsButton = view.findViewById(R.id.savedTripsButton)
        pastTripsButton = view.findViewById(R.id.pastTripsButton)

        // Set click listeners
        newTripButton.setOnClickListener {
            openNextFragment(MapsScreen())
        }

        savedTripsButton.setOnClickListener {
            openNextFragment(MapsScreen()) //TODO change to respective fragment
        }

        pastTripsButton.setOnClickListener {
            openNextFragment(MapsScreen()) //TODO change to respective fragment
        }

        // Request location permission when the fragment is created
        requestLocationPermission()

        return view
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchUserLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(
                    context,
                    "Location permission is needed to fetch weather data.",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchUserLocation() {
        CoroutineScope(Dispatchers.IO).launch {
            //TODO API REQUEST FOR CURRENT LOCATION WEATHER
        }

        Toast.makeText(context, "Fetching location...", Toast.LENGTH_SHORT).show()
    }

    private fun openNextFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainerView, fragment)
            addToBackStack(null)
        }
    }
}
