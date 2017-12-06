package at.ac.tuwien.mns.cellinfo.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import at.ac.tuwien.mns.cellinfo.MainActivity
import at.ac.tuwien.mns.cellinfo.R
import at.ac.tuwien.mns.cellinfo.adapters.CellListViewAdapter
import at.ac.tuwien.mns.cellinfo.dto.CellDetails
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import android.content.Intent
import at.ac.tuwien.mns.cellinfo.DetailActivity
import java.util.*


/**
 * Created by johannesvass on 27.11.17.
 */
class CellListViewFragment : Fragment(), AdapterView.OnItemClickListener {

    private var currentCellSubscription: Disposable? = null
    private var listView: ListView? = null
    private var adapter: CellListViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.list_layout, container, false)
        listView = rootView.findViewById<ListView>(R.id.cell_list_view)
        adapter = CellListViewAdapter(context, ArrayList(0))
        listView?.adapter = adapter
        listView?.onItemClickListener = this
        currentCellSubscription = MainActivity.cellInfoService?.cellDetailsList
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { cellList ->
                    run {
                        Log.d(this.javaClass.canonicalName, "Adding new cells to list")
                        var cells = ArrayList<CellDetails>()
                        // find active cell and add it as first item to list
                        for (cell in cellList) {
                            if (cell.registered) {
                                cells.add(cell)
                                cellList.remove(cell)
                                break
                            }
                        }
                        var sortedCellList = cellList.sortedWith(compareBy(CellDetails::strength)).reversed()
                        cells.addAll(sortedCellList)
                        adapter?.addAll(cells)
                        adapter?.notifyDataSetChanged()
                    }
                }

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
        currentCellSubscription?.dispose()
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra(CELL_DETAILS, parent?.getItemAtPosition(pos) as CellDetails)
        startActivity(intent)
    }

    companion object {

        val CELL_DETAILS = "CELL_DETAILS"
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int): CellListViewFragment {
            val fragment = CellListViewFragment()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            fragment.arguments = args
            return fragment
        }
    }
}