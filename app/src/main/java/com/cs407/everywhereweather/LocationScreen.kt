package com.cs407.everywhereweather

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LocationScreen : Fragment() {

    private lateinit var searchButton: Button
    private lateinit var searchTextView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location, container, false)

        //searchButton = view.findViewById(R.id.locationSearchButton)
        //searchTextView = view.findViewById(R.id.locationSearchText)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: Get actual dataset of locations; determine dataset based on button pressed
        val dataset = arrayOf("Location 1", "Location 2", "Location 3")

        //val navController = findNavController()

        val locationAdapter = LocationAdapter(dataset)  { buttonText ->
            // Handle the button click, navigate to another fragment
            val action = R.id.action_locationFragment_to_mapsFragment
            //navController.navigate(action)
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        recyclerView.adapter = locationAdapter

        // TODO: Create search feature for locations



    }
}