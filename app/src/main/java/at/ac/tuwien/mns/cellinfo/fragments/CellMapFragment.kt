package at.ac.tuwien.mns.cellinfo.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import at.ac.tuwien.mns.cellinfo.MainActivity
import at.ac.tuwien.mns.cellinfo.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


/**
 * Created by johannesvass on 27.11.17.
 */
class CellMapFragment :
        Fragment(),
        OnMapReadyCallback {
    private var mapView: MapView? = null
    private var mMap: GoogleMap? = null
    private var mLocationPermission: Boolean = false

    private var currentCellSubscription: Disposable? = null;
    private var markers: List<Marker> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.map_layout, container, false)

        // Gets the MapView from the XML layout and creates it
        mapView = rootView.findViewById<MapView>(R.id.map_view)
        mapView?.onCreate(savedInstanceState)

        mapView?.getMapAsync(this)

        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // display all the current cells on the map
        currentCellSubscription = MainActivity.cellInfoService?.cellDetailsList
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { cellList ->
                    run {
                        Log.d(this.javaClass.canonicalName, "Replacing cell markers")
                        markers.forEach(Marker::remove)
                        markers = cellList.map{c -> mMap?.addMarker(MarkerOptions()
                                .position(c.getLocation())
                                .rotation(if (c.registered) 90F else 0F)    // TODO make different icons instead
                                .title(c.cid.toString())
                                .snippet("Some Details"))!!}
                    }
                }

        mMap?.setInfoWindowAdapter(CellTowerMarkerPopupAdapter())

        // try to zoom to current position
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            this.mLocationPermission = false
        } else {
            this.mLocationPermission = true
            mMap?.setMyLocationEnabled(true)
            this.zoomToCurrentLocation()
        }
    }

    override fun onResume() {
        mapView?.onResume()
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentCellSubscription?.dispose()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int): CellMapFragment {
            val fragment = CellMapFragment()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        // location permission request
        if (requestCode == 1) {
            if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.mLocationPermission = true
                    mMap?.isMyLocationEnabled = true
                    this.zoomToCurrentLocation()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun zoomToCurrentLocation() {
        if (this.mLocationPermission) {
            val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            locationManager!!.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                    ZoomToLocationListener(), null)
            val location = locationManager.getLastKnownLocation(
                    locationManager.getBestProvider(Criteria(), false))
            if (location != null) {
                zoomToLocation(location)
            }
        }
    }

    private fun zoomToLocation(location: Location) {
        mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        14f))
    }

    private inner class ZoomToLocationListener : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (location != null) {
                zoomToLocation(location)
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }

    internal inner class CellTowerMarkerPopupAdapter : GoogleMap.InfoWindowAdapter {

        private val mWindow: View = layoutInflater.inflate(R.layout.cell_tower_marker_popup, null)
        private val mContents: View = layoutInflater.inflate(R.layout.cell_tower_marker_popup, null)

        override fun getInfoWindow(marker: Marker): View {
            render(marker, mWindow)
            return mWindow
        }

        override fun getInfoContents(marker: Marker): View {
            render(marker, mContents)
            return mContents
        }

        private fun render(marker: Marker, view: View) {
            val title = marker.title
            val titleUi: TextView = view.findViewById(R.id.title)
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                val titleText = SpannableString(title)
                titleText.setSpan(ForegroundColorSpan(Color.RED), 0, titleText.length, 0)
                titleUi.setText(titleText)
            } else {
                titleUi.setText("")
            }

            val snippet = marker.snippet
            val snippetUi: TextView = view.findViewById(R.id.snippet)
            if (snippet != null && snippet.length > 12) {
                val snippetText = SpannableString(snippet)
                snippetText.setSpan(ForegroundColorSpan(Color.MAGENTA), 0, 10, 0)
                snippetText.setSpan(ForegroundColorSpan(Color.BLUE), 12, snippet.length, 0)
                snippetUi.setText(snippetText)
            } else {
                snippetUi.setText("")
            }
        }
    }
}