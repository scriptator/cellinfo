package at.ac.tuwien.mns.cellinfo.service.impl;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.ac.tuwien.mns.cellinfo.dto.Cell;
import at.ac.tuwien.mns.cellinfo.dto.CellDetails;
import at.ac.tuwien.mns.cellinfo.service.CellInfoService;
import at.ac.tuwien.mns.cellinfo.service.ServiceFactory;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Hann on 03.12.2017.
 */

public class CellInfoServiceImpl implements CellInfoService {

    private final static String LOG_TAG = "CellInfoServiceImpl";

    private final Context context;
    private final String opencellidApiKey;

    // Observables
    private Observable<List<CellInfo>> cellInfoObs;
    private Observable<CellDetails> activeCellObs;
    private Observable<List<CellDetails>> cellListObs;

    public CellInfoServiceImpl(Context context, String opencellidApiKey) {
        this.context = context;
        this.opencellidApiKey = opencellidApiKey;

        this.cellInfoObs = Observable.interval(0,10, TimeUnit.SECONDS, Schedulers.io())
                .map(tick -> fetchAllCellInfo())
                .doOnError(err -> System.err.println("Error retrieving messages: " + err))
                .retry()
                .distinct();

        // observable which converts CellInfo to CellDetails (including location query)
        this.cellListObs = this.cellInfoObs
                .map(this::parseCellInfoList)
                .map(this::fetchCellDetails);

        // observable which emits the currently active cell from cellListObs
        this.activeCellObs = this.cellListObs
                .map(list -> {
                    for (CellDetails c: list) {
                        if (c.getRegistered()) {
                            Log.i(LOG_TAG, "Active cell: " + c);
                            return c;
                        }
                    }
                    return null;
                });
    }

    @Override
    public Observable<List<CellInfo>> getCellInfoList() {
        return this.cellInfoObs;
    }

    @Override
    public Observable<List<CellDetails>> getCellDetailsList() {
        return this.cellListObs;
    }

    @Override
    public Observable<CellDetails> getActiveCellDetails() {
        return this.activeCellObs;
    }

//    @Override
//    public <T extends CellInfo> List<Cell> getSpecificTypesOfCellInfo(Class<T> tClass) {
//        List<CellInfo> result = new ArrayList<>();
//        if (cellInfoList == null) {
//            return new ArrayList<>();
//        }
//        for (CellInfo cellInfo : cellInfoList) {
//            if (tClass.equals(cellInfo.getClass())) {
//                result.add(cellInfo);
//            }
//        }
//        return parseCellInfoList(result);
//    }

    // blocking call --> if it takes too long: refactor
    private List<CellDetails> fetchCellDetails(List<Cell> cellList) throws IOException {
        List<CellDetails> cellDetailsList = new ArrayList<>();
        for (Cell c: cellList) {
            Call<CellDetails> call = ServiceFactory.getOpenCellIdService().getCellDetails(
                    this.opencellidApiKey,
                    c.getMcc(),
                    c.getMnc(),
                    c.getLac(),
                    c.getCid(),
                    c.getRadio(),
                    "json"
            );
            Response<CellDetails> response;
            try {
                response = call.execute();
            } catch (IOException e) {
                throw new IOException("Fetching cell details failed", e);
            }
            if (response.isSuccessful()) {
                CellDetails enrichedCellDetails = new CellDetails(c);
                enrichedCellDetails.setLocation(response.body().getPosition());
                cellDetailsList.add(enrichedCellDetails);
            } else {
                throw new IOException("API Error: " + response.errorBody().string());
            }
        }
        return cellDetailsList;
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

    // parses the cell infos and removes duplicates
    // often the active cell exists a second time, with different strength
    @NonNull
    private List<Cell> parseCellInfoList(List<CellInfo> cellInfoList) {
        HashSet<Cell> result = new HashSet<>();
        for (CellInfo cellInfo : cellInfoList) {
            Cell parsed = parseCellInfo(cellInfo);
            boolean success = result.add(parsed);
            if (!success) {
                for (Cell other: result) {
                    if (other == parsed) {
                        Log.i(CellInfoServiceImpl.LOG_TAG, "Found duplicate cell, returning only one");
                        if (parsed.getRegistered() ||
                                parsed.getStrength() > other.getStrength()) {
                            result.remove(other);
                            result.add(parsed);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(result);
    }

    private Cell parseCellInfo(CellInfo cellInfo) {
        Cell result = new Cell();
        int mcc = 0, mnc = 0, lac = 0, cid = 0;
        boolean registered = false;
        String radio = "";
        CellSignalStrength cellSignalStrength = null;

        if (cellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
            mcc = cellInfoWcdma.getCellIdentity().getMcc();
            mnc = cellInfoWcdma.getCellIdentity().getMnc();
            lac = cellInfoWcdma.getCellIdentity().getLac();
            cid = cellInfoWcdma.getCellIdentity().getCid();
            registered = cellInfoWcdma.isRegistered();
            cellSignalStrength = cellInfoWcdma.getCellSignalStrength();
            radio = "wcdma";
        } else if (cellInfo instanceof CellInfoLte) {
            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
            mcc = cellInfoLte.getCellIdentity().getMcc();
            mnc = cellInfoLte.getCellIdentity().getMnc();
            lac = cellInfoLte.getCellIdentity().getTac();
            cid = cellInfoLte.getCellIdentity().getCi();
            registered = cellInfoLte.isRegistered();
            cellSignalStrength = cellInfoLte.getCellSignalStrength();
            radio = "lte";
        } else if (cellInfo instanceof CellInfoGsm) {
            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
            mcc = cellInfoGsm.getCellIdentity().getMcc();
            mnc = cellInfoGsm.getCellIdentity().getMnc();
            lac = cellInfoGsm.getCellIdentity().getLac();
            cid = cellInfoGsm.getCellIdentity().getCid();
            registered = cellInfoGsm.isRegistered();
            cellSignalStrength = cellInfoGsm.getCellSignalStrength();
            radio = "gsm";
        }
        result.setMcc(mcc);
        result.setMnc(mnc);
        result.setLac(lac);
        result.setCid(cid);
        result.setRadio(radio);
        result.setRegistered(registered);
        if (cellSignalStrength != null) {
            result.setStrength(cellSignalStrength.getDbm());
        }
        return result;
    }
}
