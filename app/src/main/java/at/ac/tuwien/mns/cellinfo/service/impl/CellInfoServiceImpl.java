package at.ac.tuwien.mns.cellinfo.service.impl;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.ac.tuwien.mns.cellinfo.dto.Cell;
import at.ac.tuwien.mns.cellinfo.service.CellInfoService;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Hann on 03.12.2017.
 */

public class CellInfoServiceImpl implements CellInfoService {

    private final static String LOG_TAG = "CellInfoServiceImpl";

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
    public synchronized Cell getActiveCellInfo() {
        Cell result = new Cell();
        int mcc = 0, mnc = 0, lac = 0, cid = 0;
        String radio = "";

        if (activeCellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) activeCellInfo;
            mcc = cellInfoWcdma.getCellIdentity().getMcc();
            mnc = cellInfoWcdma.getCellIdentity().getMnc();
            lac = cellInfoWcdma.getCellIdentity().getLac();
            cid = cellInfoWcdma.getCellIdentity().getCid();
            radio = "wcdma";
        } else if (activeCellInfo instanceof CellInfoLte) {
            CellInfoLte cellInfoLte = (CellInfoLte) activeCellInfo;
            mcc = cellInfoLte.getCellIdentity().getMcc();
            mnc = cellInfoLte.getCellIdentity().getMnc();
            lac = cellInfoLte.getCellIdentity().getTac();
            cid = cellInfoLte.getCellIdentity().getCi();
            radio = "lte";
        } else if (activeCellInfo instanceof CellInfoGsm) {
            CellInfoGsm cellInfoGsm = (CellInfoGsm) activeCellInfo;
            mcc = cellInfoGsm.getCellIdentity().getMcc();
            mnc = cellInfoGsm.getCellIdentity().getMnc();
            lac = cellInfoGsm.getCellIdentity().getLac();
            cid = cellInfoGsm.getCellIdentity().getCid();
            radio = "gsm";
        }
        result.setMcc(mcc);
        result.setMnc(mnc);
        result.setLac(lac);
        result.setCellId(cid);
        result.setRadio(radio);
        return result;
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
        Log.i(LOG_TAG, "Setting active cell info.");
        Log.i(LOG_TAG, activeCellInfo.toString());
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
        Log.i(LOG_TAG, "Setting cell info list.");
        Log.i(LOG_TAG, cellInfoList.toString());
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
