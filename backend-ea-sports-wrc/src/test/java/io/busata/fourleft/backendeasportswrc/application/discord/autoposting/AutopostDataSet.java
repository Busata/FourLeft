package io.busata.fourleft.backendeasportswrc.application.discord.autoposting;

import com.github.database.rider.core.api.dataset.DataSetProvider;
import com.github.database.rider.core.dataset.builder.DataSetBuilder;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;

public class AutopostDataSet implements DataSetProvider {
    @Override
    public IDataSet provide() throws DataSetException {
        DataSetBuilder builder = new DataSetBuilder();

        return builder.table("club_configuration")
                .row()
                .column("id", 1L)
                .column("club_id", "11")
                .column("keep_synced", true)
                .table("discord_club_configuration")
                .row()
                .column("id", "a1477f6e-106d-4281-9714-8a2bc5e2091b")
                .column("club_id", "11")
                .column("channel_id", 1098171035259506718L)
                .column("enabled", true)
                .column("autoposting_enabled", true)
                .column("requires_tracking", false)
                .build();
    }
}
