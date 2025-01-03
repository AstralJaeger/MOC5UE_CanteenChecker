package com.example.canteenchecker.adminapp.ui

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.canteenchecker.adminapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var addressEditText: EditText
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        addressEditText = view.findViewById(R.id.etAddress)

        // Add a listener to handle user typing addresses
        addressEditText.setOnEditorActionListener { v, actionId, event ->
            val typedAddress = v.text.toString()
            if (typedAddress.isNotBlank()) {
                val latLng = geocodeAddress(typedAddress)
                if (latLng != null) {
                    // Move map to that location
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    // Place marker
                    googleMap.clear() // Clear previous marker
                    googleMap.addMarker(MarkerOptions().position(latLng).title(typedAddress))
                }
            }
            false
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.fcvGoogleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val initialLocation = LatLng(48.3682761, 14.5129057)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))
        googleMap.addMarker(MarkerOptions().position(initialLocation).title("Hagenberg"))

        // Listen for taps on the map
        googleMap.setOnMapClickListener { latLng ->
            // Reverse geocode the tapped location
            val addressText = reverseGeocode(latLng)
            addressEditText.setText(addressText ?: "Unknown location 🤷‍♂️")

            // Place marker
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng).title(addressText))
        }
    }

    private fun geocodeAddress(address: String): LatLng? {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val results = geocoder.getFromLocationName(address, 1)
            if (results.isNullOrEmpty()) {
                null
            } else {
                val location = results.first()
                LatLng(location.latitude, location.longitude)
            }
        } catch (e: Exception) {
            // It's possible geocoder might fail or be unavailable on some devices
            null
        }
    }

    private fun reverseGeocode(latLng: LatLng): String? {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (results.isNullOrEmpty()) {
                null
            } else {
                results.first().getAddressLine(0)
            }
        } catch (e: Exception) {
            null
        }
    }
}
