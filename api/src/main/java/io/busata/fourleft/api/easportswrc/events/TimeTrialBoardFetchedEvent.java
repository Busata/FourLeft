package io.busata.fourleft.api.easportswrc.events;

/**
 * A time-trial board's snapshot was just (re)fetched — {@code combinationId} identifies the board.
 * Relayed to {@code EA_SPORTS_WRC_TT_BOARD_FETCHED}, where it triggers the board's CSV re-export.
 */
public record TimeTrialBoardFetchedEvent(String combinationId) {
}
