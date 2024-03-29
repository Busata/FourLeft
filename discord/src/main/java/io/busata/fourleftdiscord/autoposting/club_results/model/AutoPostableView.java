package io.busata.fourleftdiscord.autoposting.club_results.model;

import io.busata.fourleft.api.models.views.ViewPropertiesTo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class AutoPostableView {
    private String eventKey;
    private ViewPropertiesTo viewProperties;
    private List<AutoPostResultList> multiListResults;
}
