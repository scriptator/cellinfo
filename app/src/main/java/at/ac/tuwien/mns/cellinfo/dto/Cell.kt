package at.ac.tuwien.mns.cellinfo.dto

import com.google.android.gms.maps.model.LatLng

/**
 * Created by johannesvass on 02.12.17.
 */
open class Cell {
    val mcc: Int = 0
    val mnc: Int = 0
    val lac: Int = 0

    val cellId: Int = 0
    val radio: String = ""
}
