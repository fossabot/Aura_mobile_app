package com.wearablesensor.aura.data_repository;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lecoucl on 29/03/17.
 */

public interface RemoteDataRepository {
    void connect() throws Exception;

    void saveRRSample(final ArrayList<SampleRRInterval> iRrSamples) throws Exception;

    Date queryLastSync() throws Exception;
    void saveLastSync(final Date iLastSync) throws Exception;
}
