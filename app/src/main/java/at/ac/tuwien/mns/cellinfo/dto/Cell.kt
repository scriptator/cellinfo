package at.ac.tuwien.mns.cellinfo.dto

import java.io.Serializable

/**
 * Created by johannesvass on 02.12.17.
 */
open class Cell: Serializable{

    var mcc: Int = 0
    var mnc: Int = 0
    var lac: Int = 0
    var cid: Int = 0
    var strength: Int = 0
    var radio: String = ""
    var registered: Boolean = false

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

    // registered state or strength are not important for equality
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cell

        if (mcc != other.mcc) return false
        if (mnc != other.mnc) return false
        if (lac != other.lac) return false
        if (cid != other.cid) return false
        if (radio != other.radio) return false
        return true
    }

    // registered state or strength are not important for equality
    override fun hashCode(): Int {
        var result = mcc
        result = 31 * result + mnc
        result = 31 * result + lac
        result = 31 * result + cid
        result = 31 * result + radio.hashCode()
        return result
    }
}
