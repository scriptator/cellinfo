package at.ac.tuwien.mns.cellinfo.service.impl;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.ac.tuwien.mns.cellinfo.service.CellInfoService;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Hann on 03.12.2017.
 */

public class CellInfoServiceImpl implements CellInfoService {

    private final Context context;
    private List<CellInfo> cellInfoList;
    private CellInfo activeCellInfo;

    public CellInfoServiceImpl(Context context) {
        this.context = context;

        Observable.timer(0, TimeUnit.SECONDS, Schedulers.io())
                .map(tick -> fetchAllCellInfo())
                .doOnError(err -> System.err.println("Error retrieving messages: " + err))
                .retry()
                .distinct()
                .subscribe(this::postProcessCellInfoList);

        Observable.interval(10, TimeUnit.SECONDS, Schedulers.io())
                .map(tick -> fetchAllCellInfo())
                .doOnError(err -> System.err.println("Error retrieving messages: " + err))
                .retry()
                .distinct()
                .subscribe(this::postProcessCellInfoList);
    }

    @Override
    public synchronized List<CellInfo> getAllCellInfo() {
        return cellInfoList;
    }

    @Override
    public synchronized CellInfo getActiveCellInfo() {
        return activeCellInfo;
    }

    @Override
    public <T extends CellInfo> List<T> getSpecificTypesOfCellInfo(Class<T> tClass) {
        List<T> result = new ArrayList<>();
        if (cellInfoList == null) {
            return result;
        }
        for (CellInfo cellInfo : cellInfoList) {
            if (tClass.equals(cellInfo.getClass())) {
                result.add((T) cellInfo);
            }
        }
        return result;
    }

    private synchronized void setActiveCellInfo(CellInfo activeCellInfo) {
        System.out.println("Setting active cell info.");
        System.out.println(activeCellInfo);
        this.activeCellInfo = activeCellInfo;
    }

    private void postProcessCellInfoList(List<CellInfo> cellInfoList) {
        for (CellInfo cellInfo : cellInfoList) {
            if (cellInfo.isRegistered()) {
                setActiveCellInfo(cellInfo);
                break;
            }
        }
        setCellInfoList(cellInfoList);
    }

    private synchronized void setCellInfoList(List<CellInfo> cellInfoList) {
        System.out.println("Setting cell info list.");
        System.out.println(cellInfoList);
        this.cellInfoList = cellInfoList;
    }

    private List<CellInfo> fetchAllCellInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (PackageManager.PERMISSION_GRANTED == permissionCheck) {
                return telephonyManager.getAllCellInfo();
            }
        }
        return new ArrayList<>();
    }
}
