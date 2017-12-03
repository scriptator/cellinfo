package at.ac.tuwien.mns.cellinfo.service;

import org.jetbrains.annotations.NotNull;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ServiceFactory {

    private static OpenCellIdService openCellIdService;

    @NotNull
    public static OpenCellIdService getOpenCellIdService() {
        if (openCellIdService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://www.opencellid.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            openCellIdService = retrofit.create(OpenCellIdService.class);
        }
        return openCellIdService;
    }
}
