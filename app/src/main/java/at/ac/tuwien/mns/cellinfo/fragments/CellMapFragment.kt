package at.ac.tuwien.mns.cellinfo.fragments

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.ac.tuwien.mns.cellinfo.R
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng


/**
 * Created by johannesvass on 27.11.17.
 */
class CellMapFragment : Fragment(), OnMapReadyCallback {

    var mapView: MapView? = null
    var map: GoogleMap? = null


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
        map = googleMap
        map?.getUiSettings()?.isMyLocationButtonEnabled = false
        try {
            map?.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        // Updates the location and zoom of the MapView
        /*CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        map.animateCamera(cameraUpdate);*/
        map?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(48.0, 18.0)))

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
}