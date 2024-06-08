package com.aldi.codestories.ui.maps

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.aldi.codestories.R
import com.aldi.codestories.ViewModelFactory
import com.aldi.codestories.data.local.pref.UserPreference
import com.aldi.codestories.databinding.ActivityMapsBinding
import com.aldi.codestories.repository.Result
import com.aldi.codestories.response.ListStoryItem
import com.aldi.codestories.ui.register.RegisterActivity
import com.aldi.codestories.viewmodel.maps.MapsViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val boundsBuilder = LatLngBounds.Builder()
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsViewModel: MapsViewModel

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(RegisterActivity.SESSION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapsViewModel = obtainViewModel(this)

        setupMapFragment()
        setupToolbar()
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configureMapSettings()
        applyCustomMapStyle()
        checkLocationPermissionAndSetup()
        setupStoryMarkers()
    }

    private fun configureMapSettings() {
        with(mMap.uiSettings) {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }
    }

    private fun applyCustomMapStyle() {
        try {
            val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            enableUserLocation()
        } else {
            showToast(getString(R.string.permission_denied))
        }
    }

    private fun checkLocationPermissionAndSetup() {
        if (isLocationPermissionGranted()) {
            enableUserLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableUserLocation() {
        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun setupStoryMarkers() {
        mapsViewModel.getStoriesWithLocation().observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> displayStories(result.data)
                is Result.Error -> showError(result.error)
            }
        }
    }

    private fun displayStories(stories: List<ListStoryItem>) {
        showLoading(false)
        stories.forEach { story ->
            addMarker(story)
        }
        adjustCameraBounds()
    }

    private fun addMarker(story: ListStoryItem) {
        story.lat?.let { lat ->
            story.lon?.let { lon ->
                val latLng = LatLng(lat, lon)
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(story.name)
                        .snippet(story.description)
                )
                boundsBuilder.include(latLng)
            }
        }
    }

    private fun adjustCameraBounds() {
        val bounds = boundsBuilder.build()
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels, 300))
    }

    private fun showError(errorMessage: String) {
        showLoading(false)
        showToast(errorMessage)
    }

    private fun setupToolbar() {
        with(binding.topAppBar) {
            title = getString(R.string.maps_activity)
            setNavigationIcon(R.drawable.baseline_arrow_back_24)
            setNavigationOnClickListener { finish() }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun obtainViewModel(activity: AppCompatActivity): MapsViewModel {
        val factory = ViewModelFactory.getInstance(activity.application, UserPreference.getInstance(dataStore))
        return ViewModelProvider(activity, factory)[MapsViewModel::class.java]
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}
