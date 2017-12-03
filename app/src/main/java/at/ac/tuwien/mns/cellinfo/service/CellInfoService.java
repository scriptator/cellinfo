package at.ac.tuwien.mns.cellinfo.service;

import android.telephony.CellInfo;

import java.util.List;

/**
 * Created by Hann on 03.12.2017.
 */

public interface CellInfoService {

    /**
     * Retrieve the actual cell informations (active + neighbouring)
     *
     * @return
     */
    List<CellInfo> getAllCellInfo();

    /**
     * Retrieve the active cell information
     *
     * @return
     */
    CellInfo getActiveCellInfo();

    /**
     * Retrieve all specific types of cell info object from the cell info list
     *
     * @param <T>
     */
    <T extends CellInfo> List<T> getSpecificTypesOfCellInfo(Class<T> tClass);
}
