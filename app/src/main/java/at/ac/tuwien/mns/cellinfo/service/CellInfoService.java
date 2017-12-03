package at.ac.tuwien.mns.cellinfo.service;

import android.telephony.CellInfo;

import java.util.List;

import at.ac.tuwien.mns.cellinfo.dto.Cell;

/**
 * Created by Hann on 03.12.2017.
 */

public interface CellInfoService {

    /**
     * Retrieve the actual cell informations (active + neighbouring)
     *
     * @return
     */
    List<Cell> getAllCellInfo();

    /**
     * Retrieve the active cell information
     *
     * @return
     */
    Cell getActiveCellInfo();

    /**
     * Retrieve all specific types of cell info object from the cell info list
     *
     * @param <T>
     */
    <T extends CellInfo> List<Cell> getSpecificTypesOfCellInfo(Class<T> tClass);
}
