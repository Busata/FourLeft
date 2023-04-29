package io.busata.fourleft.api.models;

import lombok.Getter;

public record ResultEntryTo(Long rank,
                                  String name,
                                  String nationality,
                                  String vehicle,
                                  String totalTime,
                                  String totalDiff,
                                  String stageTime,
                                  String stageDiff,
                                  long stageRank,
                                  Boolean isDnf,

                                  Platform platform,
                                  ControllerType controllerType
) {
    public static ResultEntryToBuilder resultEntryTo() {
        return new ResultEntryToBuilder();
    }

    @Getter
    public static class ResultEntryToBuilder {
        private Long rank;
        private String name;
        private String nationality;
        private String vehicle;
        private String totalTime;
        private String totalDiff;
        private String stageTime;
        private String stageDiff;
        private long stageRank;
        private Boolean isDnf;

        private Platform platform = Platform.UNKNOWN;
        private ControllerType controllerType = ControllerType.UNKNOWN;

        ResultEntryToBuilder() {
        }

        public ResultEntryToBuilder rank(Long rank) {
            this.rank = rank;
            return this;
        }

        public ResultEntryToBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ResultEntryToBuilder nationality(String nationality) {
            this.nationality = nationality;
            return this;
        }

        public ResultEntryToBuilder vehicle(String vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public ResultEntryToBuilder totalTime(String totalTime) {
            this.totalTime = totalTime;
            return this;
        }

        public ResultEntryToBuilder totalDiff(String totalDiff) {
            this.totalDiff = totalDiff;
            return this;
        }

        public ResultEntryToBuilder stageTime(String stageTime) {
            this.stageTime = stageTime;
            return this;
        }

        public ResultEntryToBuilder stageDiff(String stageDiff) {
            this.stageDiff = stageDiff;
            return this;
        }

        public ResultEntryToBuilder stageRank(long stageRank ) {
            this.stageRank = stageRank;
            return this;
        }

        public ResultEntryToBuilder isDnf(Boolean isDnf) {
            this.isDnf = isDnf;
            return this;
        }

        public ResultEntryToBuilder platform(Platform platform) {
            this.platform = platform;
            return this;
        }

        public ResultEntryToBuilder controllerType(ControllerType controllerType) {
            this.controllerType = controllerType;
            return this;
        }

        public ResultEntryTo build() {
            return new ResultEntryTo(rank, name, nationality, vehicle, totalTime, totalDiff, stageTime, stageDiff, stageRank, isDnf, platform, controllerType);
        }
    }
}
