package com.cs407.everywhereweather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.cs407.everywhereweather.api.Cords
import com.cs407.everywhereweather.api.CurrentWeatherResponse
import com.cs407.everywhereweather.api.GetSpotWeatherRequest
import com.cs407.everywhereweather.api.WeatherOnSpotDTO
import com.cs407.everywhereweather.api.WeatherOnSpotService
import com.cs407.everywhereweather.api.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainMenuScreen : Fragment() {

    private lateinit var newTripButton: Button
    private lateinit var savedTripsButton: Button
    private lateinit var pastTripsButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var conditionTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var feelsLikeTextView: TextView
    private lateinit var precipitationTextView: TextView
    private lateinit var windSpeedTextView: TextView

    private var weatherData: WeatherResponse? = null
    private var cancellationTokenSource: CancellationTokenSource? = null

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mainmenu, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Initialize buttons
        newTripButton = view.findViewById(R.id.newTripButton)
        savedTripsButton = view.findViewById(R.id.savedTripsButton)
        pastTripsButton = view.findViewById(R.id.pastTripsButton)

        // Initialize text views
        conditionTextView = view.findViewById(R.id.ConditionText)
        temperatureTextView = view.findViewById(R.id.TemperatureText)
        feelsLikeTextView = view.findViewById(R.id.FeelsLikeText)
        precipitationTextView = view.findViewById(R.id.PrecipitationText)
        windSpeedTextView = view.findViewById(R.id.WindSpeedText)

        // Set click listeners
        newTripButton.setOnClickListener {
            openNextFragment(MapsScreen())
        }

        savedTripsButton.setOnClickListener {
            openNextFragment(MapsScreen()) // TODO change to respective fragment
        }

        pastTripsButton.setOnClickListener {
            openNextFragment(MapsScreen()) // TODO change to respective fragment
        }

        // Request location permission when the fragment is created
        requestLocationPermission()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancellationTokenSource?.cancel()
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchCurrentLocation()
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

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                fetchUserWeather(location)
            } else {
                Toast.makeText(context, "Error retrieving location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserWeather(location: Location) {

        Toast.makeText(
            context,
            "Latitude: ${location.latitude}, Longitude: ${location.longitude}",
            Toast.LENGTH_SHORT
        ).show()

        CoroutineScope(Dispatchers.IO).launch {
            val service = WeatherOnSpotService()
            val request = GetSpotWeatherRequest(
                cords = Cords(location.latitude, location.longitude),
                timeOffset = 0
            )
            val response = service.fetchWeather(request)

            when (response) {
                is CurrentWeatherResponse -> {
                    updateWeatherUI(response)
                }
                else -> {
                    println("Failed to fetch weather data")
                }
            }
        }
    }

    private fun kelvinToFahrenheit(kelvin: Double): String {
        return  (-1 *((kelvin - 273.15) * 9 / 5 + 32)).let {
            String.format(Locale.US, "%.2f", it)
        }
    }

    private fun updateWeatherUI(weather: WeatherResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // Extract data from the WeatherResponse
            if (weather is CurrentWeatherResponse) { // Ensure type safety
                val condition = weather.weatherDescription
                val kelvinTemperature = weather.temp
                val kelvinFeelsLike = weather.feelsLike
                val precipitation = weather.precipitationChance.toInt().toString()
                val windSpeed = weather.windSpeed.toInt().toString()

                // Convert temperatures from Kelvin to Fahrenheit
                val temperature: String = kelvinToFahrenheit(kelvinTemperature)
                val feelsLike: String = kelvinToFahrenheit(kelvinFeelsLike)

                // Update UI elements with the extracted data
                conditionTextView.text = condition
                temperatureTextView.text = getString(R.string.temperature, temperature)
                feelsLikeTextView.text = getString(R.string.feels_like, feelsLike)
                precipitationTextView.text = getString(R.string.precipitation, precipitation)
                windSpeedTextView.text = getString(R.string.wind_speed, windSpeed)
            } else {
                println("Invalid weather type provided")
            }
        }
    }



    private fun openNextFragment(fragment: Fragment) {
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainerView, fragment)
            addToBackStack(null)
        }
    }
}
