package at.ac.tuwien.mns.cellinfo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import at.ac.tuwien.mns.cellinfo.dto.Cell
import at.ac.tuwien.mns.cellinfo.dto.CellDetails
import at.ac.tuwien.mns.cellinfo.fragments.CellListViewFragment

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val intent = intent
        val cellDetails = intent.extras.getSerializable(CellListViewFragment.CELL_DETAILS) as CellDetails

        setCellDetailsInLayout(cellDetails)
    }

    private fun setCellDetailsInLayout(cellDetails: CellDetails) {

        val strength = findViewById<TextView>(R.id.strength)
        val radio = findViewById<TextView>(R.id.radio)
        val mcc = findViewById<TextView>(R.id.mcc)
        val mnc = findViewById<TextView>(R.id.mnc)
        val lac = findViewById<TextView>(R.id.lac)
        val cid = findViewById<TextView>(R.id.cid)

        strength.text = cellDetails.strength.toString()
        radio.text = cellDetails.radio
        mcc.text = cellDetails.mcc.toString()
        mnc.text = cellDetails.mnc.toString()
        lac.text = cellDetails.lac.toString()
        cid.text = cellDetails.cid.toString()

    }
}
