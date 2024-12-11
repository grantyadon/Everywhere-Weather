package com.cs407.everywhereweather


import GoogleDirectionsAPI
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.cs407.everywhereweather.api.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsScreen : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var currentLocation: LatLng? = null
    private lateinit var placesClient: PlacesClient
    private var routeWeather: List<RouteWeatherDTO>? = null
    private val apiKey = "API KEY"

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        enableMyLocation()
        setUpCurrentLocationMarker()
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

        // Initialize Places API
        Places.initialize(requireContext(), apiKey)
        placesClient = Places.createClient(requireContext())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        val searchBar = view.findViewById<AutoCompleteTextView>(R.id.search_bar)
        val searchButton = view.findViewById<ImageButton>(R.id.search_button)
        val backButton = view.findViewById<ImageButton>(R.id.back_button)

        // Handle search bar autocomplete
        setUpSearchBarAutocomplete(searchBar)

        // Handle search button click
        searchButton.setOnClickListener {
            val query = searchBar.text.toString()
            if (query.isNotEmpty() && currentLocation != null) {
                searchForRoutes(query)
            } else {
                Snackbar.make(view, "Please enter a destination", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Handle back button click
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    //TODO make this display the predictions. They are being made just not displayed.
    private fun setUpSearchBarAutocomplete(searchBar: AutoCompleteTextView) {
        val predictionList = mutableListOf<String>()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            predictionList
        )
        searchBar.setAdapter(adapter)

        searchBar.addTextChangedListener { text ->
            val query = text?.toString() ?: ""
            Log.d("SearchBar", "User typed: $query")

            if (query.isNotEmpty()) {
                val token = AutocompleteSessionToken.newInstance()
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .setSessionToken(token)
                    .build()

                Log.d("SearchBar", "Fetching predictions for query: $query")

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        Log.d(
                            "SearchBar",
                            "Predictions fetched: ${response.autocompletePredictions.size}"
                        )
                        predictionList.clear()
                        for (prediction in response.autocompletePredictions) {
                            val fullText = prediction.getFullText(null).toString()
                            Log.d("SearchBar", "Prediction: $fullText")
                            predictionList.add(fullText)
                        }
                        adapter.notifyDataSetChanged()
                        searchBar.showDropDown()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("SearchBar", "Error fetching predictions", exception)
                    }
            } else {
                Log.d("SearchBar", "Query is empty, skipping predictions fetch")
            }
        }
    }


    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpCurrentLocationMarker() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = LatLng(it.latitude, it.longitude)
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(currentLocation!!)
                        .title("Current Location")
                )
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
            } ?: run {
                Snackbar.make(
                    requireView(),
                    "Unable to fetch current location",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun searchForRoutes(destination: String) {
        val origin = "${currentLocation?.latitude},${currentLocation?.longitude}"

        val googleApi = RetrofitClient.getGoogleApiClient().create(GoogleDirectionsAPI::class.java)
        googleApi.getDirections(origin, destination, apiKey).enqueue(object : Callback<GoogleRoutesResponse> {
            override fun onResponse(call: Call<GoogleRoutesResponse>, response: Response<GoogleRoutesResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { directionsResponse ->
                        Log.d("MapsScreen", "Directions fetched successfully.")

                        getWeatherOnRoute(directionsResponse, 0) { routeWeatherList ->
                            routeWeather = routeWeatherList
                            //TODO draw the objects from this route
                            Log.d("MapsScreen", "Weather on route: $routeWeather")
                        }
                    } ?: run {
                        Log.e("MapsScreen", "Response body is null.")
                    }
                } else {
                    Log.e("MapsScreen", "Failed to fetch directions: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GoogleRoutesResponse>, t: Throwable) {
                Log.e("MapsScreen", "Error fetching directions", t)
            }
        })
    }


//    private fun sendRouteToBackend(routeResponse: GoogleRoutesResponse, timeOffset: Int) {
//        Log.d("Backend", "Preparing to send route to backend with timeOffset: $timeOffset")
//        val gson = Gson()
//        val serializedRequest =
//            gson.toJson(GetRouteWeatherRequest(route = routeResponse, timeOffset = 0))
//        Log.d("MapsScreen", "Serialized Request: $serializedRequest")
//
//
//        Log.d("MapsScreen", "Preparing request: $routeResponse")
//        routeResponse.routes.forEach { route ->
//            Log.d("MapsScreen", "Route Summary: ${route.summary}")
//            route.legs.forEach { leg ->
//                Log.d("MapsScreen", "Leg: Start=${leg.startLocation}, End=${leg.endLocation}")
//                leg.steps?.forEach { step ->
//                    Log.d(
//                        "MapsScreen",
//                        "Step: Start=${step.startLocation}, End=${step.endLocation}"
//                    )
//                }
//            }
//        }
//        val request = GetRouteWeatherRequest(
//            route = routeResponse,
//            timeOffset = timeOffset
//        )
//
//
//        val backendAPI = RetrofitClient.getClient().create(WeatherOnRouteAPI::class.java)
//
//        backendAPI.getWeatherOnRoute(request.route)
//            .enqueue(object : Callback<List<RouteWeatherDTO>> {
//                override fun onResponse(
//                    call: Call<List<RouteWeatherDTO>>,
//                    response: Response<List<RouteWeatherDTO>>
//                ) {
//                    if (response.isSuccessful) {
//                        routeWeather = response.body()
//                        Log.d("Backend", "Weather info successfully received: $routeWeather")
//                    } else {
//                        val errorBody = response.errorBody()?.string()
//                        Log.e("Backend", "Failed to fetch weather info. Error body: $errorBody")
//                    }
//                }
//
//                override fun onFailure(call: Call<List<RouteWeatherDTO>>, t: Throwable) {
//                    Log.e("Backend", "Error sending route to backend", t)
//                }
//            })
//    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

    }


    fun getWeatherOnRoute(
        request: GoogleRoutesResponse,
        timeOffset: Int,
        onComplete: (List<RouteWeatherDTO>) -> Unit
    ) {
        val backendAPI = RetrofitClient.getClient().create(WeatherOnSpotAPI::class.java)
        val routesWeather = mutableListOf<RouteWeatherDTO>()

        request.routes.forEach { route ->
            val routeWeather = mutableMapOf<Cords, MinutelyWeatherDTO>()
            var totalDuration = 0

            route.legs.forEachIndexed { index, leg ->
                val startCords = Cords(leg.startLocation.latitude, leg.startLocation.longitude)
                val startWeatherRequest = GetSpotWeatherRequest(startCords, timeOffset)

                backendAPI.getSpotWeather(startWeatherRequest).enqueue(object : retrofit2.Callback<WeatherResponse> {
                    override fun onResponse(
                        call: Call<WeatherResponse>,
                        response: Response<WeatherResponse>
                    ) {
                        response.body()?.let { weather ->
                            val precipitationChance = when (weather) {
                                is CurrentWeatherResponse -> weather.precipitationChance
                                is HourlyWeatherResponse -> weather.precipitationChance
                                is DailyWeatherResponse -> weather.precipitationChance
                                else -> 0.0
                            }
                            val temperatureInCelsius = when (weather) {
                                is CurrentWeatherResponse -> weather.temp - 273.15
                                is HourlyWeatherResponse -> weather.temp - 273.15
                                is DailyWeatherResponse -> weather.tempDay - 273.15
                                else -> 0.0
                            }

                            // Add marker for the leg start
                            if (precipitationChance > 0.0) {
                                googleMap?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(startCords.latitude, startCords.longitude))
                                        .title("Precipitation: ${precipitationChance * 100}%, Temp: ${"%.1f".format(temperatureInCelsius)}°C")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                        Log.e("WeatherAPI", "Failed to fetch weather for start location: $startCords", t)
                    }
                })

                leg.steps.forEach { step ->
                    totalDuration += parseDuration(step.staticDuration)

                    val endCords = Cords(step.endLocation.latitude, step.endLocation.longitude)
                    val stepWeatherRequest = GetSpotWeatherRequest(endCords, timeOffset + totalDuration)

                    backendAPI.getSpotWeather(stepWeatherRequest).enqueue(object : retrofit2.Callback<WeatherResponse> {
                        override fun onResponse(
                            call: Call<WeatherResponse>,
                            response: Response<WeatherResponse>
                        ) {
                            response.body()?.let { weather ->
                                val precipitationChance = when (weather) {
                                    is CurrentWeatherResponse -> weather.precipitationChance
                                    is HourlyWeatherResponse -> weather.precipitationChance
                                    is DailyWeatherResponse -> weather.precipitationChance
                                    else -> 0.0
                                }
                                val temperatureInCelsius = when (weather) {
                                    is CurrentWeatherResponse -> weather.temp - 273.15
                                    is HourlyWeatherResponse -> weather.temp - 273.15
                                    is DailyWeatherResponse -> weather.tempDay - 273.15
                                    else -> 0.0
                                }

                                googleMap?.addPolyline(
                                    PolylineOptions()
                                        .add(
                                            LatLng(step.startLocation.latitude, step.startLocation.longitude),
                                            LatLng(step.endLocation.latitude, step.endLocation.longitude)
                                        )
                                        .width(10f)
                                        .color(ContextCompat.getColor(requireContext(), R.color.black))
                                )

                                if (precipitationChance > 0.0) {
                                    googleMap?.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(endCords.latitude, endCords.longitude))
                                            .title("Precipitation: ${precipitationChance * 100}%, Temp: ${"%.1f".format(temperatureInCelsius)}°C")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                    )
                                }
                            }
                        }

                        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                            Log.e("WeatherAPI", "Failed to fetch weather for step location: $endCords", t)
                        }
                    })
                }

                // Add marker for the destination (end location of the last leg)
                if (index == route.legs.size - 1) {
                    val destinationCords = Cords(leg.endLocation.latitude, leg.endLocation.longitude)
                    val destinationWeatherRequest = GetSpotWeatherRequest(destinationCords, timeOffset + totalDuration)

                    backendAPI.getSpotWeather(destinationWeatherRequest).enqueue(object : retrofit2.Callback<WeatherResponse> {
                        override fun onResponse(
                            call: Call<WeatherResponse>,
                            response: Response<WeatherResponse>
                        ) {
                            response.body()?.let { weather ->
                                val temperatureInCelsius = when (weather) {
                                    is CurrentWeatherResponse -> weather.temp - 273.15
                                    is HourlyWeatherResponse -> weather.temp - 273.15
                                    is DailyWeatherResponse -> weather.tempDay - 273.15
                                    else -> 0.0
                                }
                                googleMap?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(leg.endLocation.latitude, leg.endLocation.longitude))
                                        .title("Destination, Temp: ${"%.1f".format(temperatureInCelsius)}°C")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                )
                            }
                        }

                        override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                            Log.e("WeatherAPI", "Failed to fetch weather for destination: $destinationCords", t)
                        }
                    })
                }
            }

            routesWeather.add(RouteWeatherDTO(listOf(route.summary), routeWeather))
        }

        onComplete(routesWeather)
    }




    private fun parseDuration(duration: String?): Int {
        if (duration.isNullOrEmpty()) {
            Log.e("MapsScreen", "Duration is null or empty.")
            return 0 // Default to 0 if duration is null or empty
        }

        val regex = Regex("(\\d+)\\s*(s|m|h)")
        val matchResult = regex.find(duration)

        return if (matchResult != null) {
            val (value, unit) = matchResult.destructured
            when (unit) {
                "s" -> value.toInt()
                "m" -> value.toInt() * 60
                "h" -> value.toInt() * 3600
                else -> 0
            }
        } else {
            Log.e("MapsScreen", "Failed to parse duration: $duration")
            0 // Default to 0 if regex parsing fails
        }
    }



}