package io.busata.fourleft.api.models;

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
    },
    COLOUR {
        @Override
        public String getDefaultValue() {
            return "0,0,0";
        }
    };



    public abstract String getDefaultValue();


}
