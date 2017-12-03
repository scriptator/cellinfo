package at.ac.tuwien.mns.cellinfo.service;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    // Observables
    private Observable<CellDetails> currentCellObs = null;

    public MobileNetworkService(String opencellidApiKey) {
        this.opencellidApiKey = opencellidApiKey;
        this.openCellIdService = ServiceFactory.getOpenCellIdService();

        this.currentCellObs = Observable.interval(10, TimeUnit.SECONDS, Schedulers.io())
                .map(tick -> fetchCellDetails())
                .doOnError(err -> System.err.println("Error retrieving messages: " + err))
                .retry()
                .distinct();
    }

    // blocking
    private CellDetails fetchCellDetails() throws IOException {
        Call<CellDetails> call = openCellIdService.getCellDetails(
                this.opencellidApiKey,
                232,
                1,
                17004,
                51011,
                "gsm",
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
