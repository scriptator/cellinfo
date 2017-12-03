package at.ac.tuwien.mns.cellinfo.dto

import android.telephony.CellSignalStrength

/**
 * Created by johannesvass on 02.12.17.
 */
open class Cell {
    var mcc: Int = 0
    var mnc: Int = 0
    var lac: Int = 0

    var cid: Int = 0
    var radio: String = ""

    var registered: Boolean = false
    var strength: CellSignalStrength? = null

    constructor()
    constructor(other: Cell) {
        this.mcc = other.mcc
        this.mnc = other.mnc
        this.lac = other.lac
        this.cid = other.cid
        this.radio = other.radio
        this.registered = other.registered
        this.strength = other.strength
    }
}
