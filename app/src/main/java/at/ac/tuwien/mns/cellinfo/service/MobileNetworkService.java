package at.ac.tuwien.mns.cellinfo.service;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import at.ac.tuwien.mns.cellinfo.dto.Cell;
import at.ac.tuwien.mns.cellinfo.dto.CellDetails;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by johannesvass on 02.12.17.
 */

public class MobileNetworkService {

    private final OpenCellIdService openCellIdService;
    private final String opencellidApiKey;

    private final CellInfoService cellInfoService;

    // Observables
    private Observable<CellDetails> currentCellObs = null;

    public MobileNetworkService(String opencellidApiKey, CellInfoService cellInfoService) {
        this.opencellidApiKey = opencellidApiKey;
        this.cellInfoService = cellInfoService;
        this.openCellIdService = ServiceFactory.getOpenCellIdService();

        this.currentCellObs = Observable.interval(10, TimeUnit.SECONDS, Schedulers.io())
                .map(tick -> fetchCellDetails())
                .doOnError(err -> System.err.println("Error retrieving messages: " + err))
                .retry()
                .distinct();
        this.currentCellObs.subscribe(System.out::println);
    }

    // blocking
    private CellDetails fetchCellDetails() throws IOException {
        Cell activeCellInfo = cellInfoService.getActiveCellInfo();
        Call<CellDetails> call = openCellIdService.getCellDetails(
                this.opencellidApiKey,
                activeCellInfo.getMcc(),
                activeCellInfo.getMnc(),
                activeCellInfo.getLac(),
                activeCellInfo.getCellId(),
                activeCellInfo.getRadio(),
                "json"
        );
        Response<CellDetails> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            throw new IOException("Fetching cell details failed", e);
        }
        if (response.isSuccessful()) {
            return response.body();
        } else {
            throw new IOException("API Error: " + response.errorBody().string());
        }
    }

    @NotNull
    public Observable<CellDetails> currentCell() {
        return currentCellObs;
    }
}
