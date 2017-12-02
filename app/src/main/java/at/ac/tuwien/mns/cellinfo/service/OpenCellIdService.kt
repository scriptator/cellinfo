package at.ac.tuwien.mns.cellinfo.service

import at.ac.tuwien.mns.cellinfo.dto.CellDetails
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface OpenCellIdService {

    @GET("cell/get")
    fun getCellDetails(@Query("key") key: String,
                       @Query("mcc") mcc: Int,
                       @Query("mnc") mnc: Int,
                       @Query("lac") lac: Int,
                       @Query("cid") cid: Int,
                       @Query("radio") radio: String,
                       @Query("format") format: String = "json"): Call<CellDetails>
}
