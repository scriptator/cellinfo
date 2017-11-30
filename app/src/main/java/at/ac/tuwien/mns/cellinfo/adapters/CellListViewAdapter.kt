package at.ac.tuwien.mns.cellinfo.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import at.ac.tuwien.mns.cellinfo.R

/**
 * Created by dominik on 30.11.2017.
 */
class CellListViewAdapter(private val activity: Activity) : BaseAdapter() {

    private var cellList = arrayOf("CellOne", "CellTwo", "CellThree", "CellFour", "CellFive", "CellSix")

    override fun getCount(): Int {
        return cellList.size
    }

    override fun getItem(i: Int): Any {
        return cellList[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View?
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowHolder: ListRowHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.cell_list_row, parent, false)
            rowHolder = ListRowHolder(view)
            view.tag = rowHolder
        } else {
            view = convertView
            rowHolder = view.tag as ListRowHolder
        }

        rowHolder.label?.text = cellList[position]
        return view
    }

    private class ListRowHolder(row: View?) {

        val label: TextView? = row?.findViewById<TextView>(R.id.label)

    }
}