package at.ac.tuwien.mns.cellinfo.dto

import com.google.android.gms.maps.model.LatLng

/**
 * Created by johannesvass on 02.12.17.
 */
class CellDetails : Cell() {
    val lat: Double = Double.NaN
    val lon: Double = Double.NaN

    fun getLocation(): LatLng {
        return LatLng(lat, lon)
    }
}
