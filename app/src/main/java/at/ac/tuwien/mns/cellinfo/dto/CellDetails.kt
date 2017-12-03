package at.ac.tuwien.mns.cellinfo.dto

import com.google.android.gms.maps.model.LatLng

/**
 * Created by johannesvass on 02.12.17.
 */
class CellDetails(c: Cell) : Cell(c) {
    var lat: Double = Double.NaN
    var lon: Double = Double.NaN

    constructor() : this(Cell())

    fun getLocation(): LatLng {
        return LatLng(lat, lon)
    }

    fun setLocation(latlon: LatLng) {
        lat = latlon.latitude
        lon = latlon.longitude
    }

    override fun toString(): String {
        return "CellDetails(lat=$lat, lon=$lon)"
    }


}
