package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.domain.clubs.models.BoardEntry;

public interface BoardTimeSupplier {

    String getTime(BoardEntry entry);
}
