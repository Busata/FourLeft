package io.busata.fourleft.api.models;

public record StageResultTo(String vehicle,

                            String stageTime,
                            String stageDiff,
                            Long stageRank,
                            Boolean isDnf) {
}
