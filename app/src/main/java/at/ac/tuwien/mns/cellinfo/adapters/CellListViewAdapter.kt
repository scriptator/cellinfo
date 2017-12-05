package at.ac.tuwien.mns.cellinfo.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import at.ac.tuwien.mns.cellinfo.R
import at.ac.tuwien.mns.cellinfo.dto.CellDetails

/**
 * Created by dominik on 30.11.2017.
 */
class CellListViewAdapter(context: Context, cells: List<CellDetails>) : BaseAdapter() {

    private val inflater: LayoutInflater
    private var cells : List<CellDetails>

    init {
        this.cells = cells
        this.inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return cells.size
    }

    override fun getItem(i: Int): Any {
        return cells[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    fun addAll(cells: List<CellDetails>) {
        this.cells = cells
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View?
        val rowHolder: ListRowHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.cell_list_row, parent, false)
            rowHolder = ListRowHolder(view)
            view.tag = rowHolder
        } else {
            view = convertView
            rowHolder = view.tag as ListRowHolder
        }
        val color: Int = if (cells[position].registered) {
            ContextCompat.getColor(rowHolder.icon?.context, R.color.colorPrimary)
        } else {
            ContextCompat.getColor(rowHolder.icon?.context, R.color.colorInactive)
        }
        rowHolder.icon?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        rowHolder.label?.text = cells[position].cid.toString()
        return view
    }

    private class ListRowHolder(row: View?) {

        val icon: ImageView? = row?.findViewById<ImageView>(R.id.icon)
        val label: TextView? = row?.findViewById<TextView>(R.id.label)

    }
}