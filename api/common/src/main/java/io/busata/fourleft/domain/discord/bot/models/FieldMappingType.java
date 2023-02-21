package io.busata.fourleft.domain.discord.bot.models;

public enum FieldMappingType {
    HUMAN_READABLE {
        @Override
        public String getDefaultValue() {
            return "Unknown";
        }
    },
    EMOTE {
        @Override
        public String getDefaultValue() {
            return ":grey_question:";

        }
    },
    IMAGE {
        @Override
        public String getDefaultValue() {
            return "";
        }
    };



    public abstract String getDefaultValue();


}
