package at.ac.tuwien.mns.cellinfo.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.ac.tuwien.mns.cellinfo.MainActivity
import at.ac.tuwien.mns.cellinfo.R
import at.ac.tuwien.mns.cellinfo.dto.CellDetails
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
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

    private var currentCellSubscription: Disposable? = null

    private var mClusterManager: ClusterManager<CellDetails>? = null


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

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = ClusterManager<CellDetails>(context, mMap)
        mClusterManager!!.renderer = CellRenderer()

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap?.setOnCameraIdleListener(mClusterManager)
        mMap?.setOnMarkerClickListener(mClusterManager)

        currentCellSubscription = MainActivity.cellInfoService?.cellDetailsList
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { cellList ->
                    run {
                        Log.d(this.javaClass.canonicalName, cellList.toString())
                        Log.d(this.javaClass.canonicalName, "Adding new cells to cluster manager")
                        // keep cluster manager's state up to date
                        mClusterManager?.clearItems()
                        mClusterManager?.addItems(cellList)
                        mClusterManager?.cluster()
                    }
                }

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

    private inner class CellRenderer : DefaultClusterRenderer<CellDetails>(context, mMap, mClusterManager) {
        private val mActiveCell: BitmapDescriptor
        private val mNeighboringCell: BitmapDescriptor

        init {
            // display all the current cells on the map
            val antennaDrawable = ContextCompat.getDrawable(context, R.drawable.ic_settings_input_antenna_black_24dp)
            mActiveCell = drawableToBitmapDescriptor(antennaDrawable, R.color.colorPrimary)
            mNeighboringCell = drawableToBitmapDescriptor(antennaDrawable, R.color.colorInactive)
        }

        override fun onBeforeClusterItemRendered(cell: CellDetails, markerOptions: MarkerOptions) {
            markerOptions
                    .icon(if (cell.registered) mActiveCell else mNeighboringCell)
                    .title(cell.cid.toString())
                    .snippet(cell.toString())
        }

        override fun shouldRenderAsCluster(cluster: Cluster<CellDetails>): Boolean {
            return cluster.getSize() > 1
        }

        private fun drawableToBitmapDescriptor(drawable: Drawable, color: Int = R.color.colorPrimary): BitmapDescriptor {
            val canvas = Canvas()
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, color))
            val bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0,
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight())
            drawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
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
}