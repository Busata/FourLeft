package io.busata.fourleft.backendeasportswrc.application.importer;

import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;

public class BaseDataSet implements DataSetProvider {
    @Override
    public IDataSet provide() throws DataSetException {
        DataSetBuilder builder = new DataSetBuilder();

        return builder.table("club_configuration")
                .row()
                .column("id", 1L)
                .column("club_id", "11")
                .column("keep_synced", true)
                .build();
    }
}
