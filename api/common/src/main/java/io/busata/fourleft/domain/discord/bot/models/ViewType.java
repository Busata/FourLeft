package io.busata.fourleft.domain.discord.bot.models;


import lombok.Getter;

@Getter
public enum ViewType {
    STANDARD("Show times") {
        @Override
        public ViewType next() {
            return ViewType.EXTRA;
        }
    },
    EXTRA("Show cars/platforms") {
        @Override
        public ViewType next() {
            return ViewType.STANDARD;
        }
    };

    private final String buttonLabel;

    ViewType(String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }

    public abstract ViewType next();
}
