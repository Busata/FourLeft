package io.busata.fourleft.club.championship.creator;

import io.busata.fourleft.domain.dirtrally2.options.models.VehicleClassGroups;
import io.busata.fourleft.application.dirtrally2.automated.service.CycleOptionsSelector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CycleOptionsSelectorTest {
    @Test
    public void testBagOccurence() {
        var occurenceSelector = new CycleOptionsSelector();

        var option = occurenceSelector.generate(Arrays.asList(VehicleClassGroups.values()),
                List.of(
                        VehicleClassGroups.CLASSIC,
                        VehicleClassGroups.MODERN_ALTERNATIVE,
                        VehicleClassGroups.UNPOPULAR,
                        VehicleClassGroups.MODERN,
                        VehicleClassGroups.GROUP_B
                        ));

        assertThat(option).isEqualTo(VehicleClassGroups.MODERN_CLASSICS);
    }
}