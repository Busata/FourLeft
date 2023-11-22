package io.busata.fourleft.backendeasportswrc.infrastructure.configuration;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class FlywayConfiguration {
    private static final String DDL_FOLDER = "classpath:db/migration";

    private final DataSource dataSource;

    @Profile("test")
    @Bean(initMethod = "migrate")
    public Flyway integrationTestFlyway() {
        return createFlywayConfiguration(DDL_FOLDER);
    }

    @Nonnull
    private Flyway createFlywayConfiguration(final String... locations) {
        return Flyway.configure()
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("000"))
                .placeholderReplacement(false)
                .createSchemas(false)
                .dataSource(dataSource)
                .encoding(StandardCharsets.UTF_8)
                .locations(locations)
                .placeholderPrefix("##{")
                .placeholderSuffix("}#")
                .schemas("public", "envers")
                .table("schema_version")
                .load();
    }
}
