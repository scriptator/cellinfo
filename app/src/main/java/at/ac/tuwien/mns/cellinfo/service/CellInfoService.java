package at.ac.tuwien.mns.cellinfo.service;

import android.telephony.CellInfo;

import java.util.List;

import at.ac.tuwien.mns.cellinfo.dto.Cell;
import at.ac.tuwien.mns.cellinfo.dto.CellDetails;
import io.reactivex.Observable;

/**
 * Created by Hann on 03.12.2017.
 */

public interface CellInfoService {

    /**
     * Retrieve the raw cell informations (active + neighbouring)
     *
     * @return
     */
    Observable<List<CellInfo>> getCellInfoList();

    /**
     * Retrieve the parsed cell informations (active + neighbouring) including location
     *
     * @return
     */
    Observable<List<CellDetails>> getCellDetailsList();

    /**
     * Retrieve the active cell information
     *
     * @return
     */
    Observable<CellDetails> getActiveCellDetails();

    /**
     * Retrieve all specific types of cell info object from the cell info list
     *
     * @param <T>
     */
//    <T extends CellInfo> List<Cell> getSpecificTypesOfCellInfo(Class<T> tClass);
}
