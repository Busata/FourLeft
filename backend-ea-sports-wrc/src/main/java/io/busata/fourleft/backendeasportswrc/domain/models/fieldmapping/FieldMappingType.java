package io.busata.fourleft.backendeasportswrc.domain.models.fieldmapping;

public enum FieldMappingType {
    HUMAN_READABLE {
        @Override
        public String getDefaultValue(FieldMappingContext context) {
            return "Unknown";
        }
    },
    FLAG {
        @Override
        public String getDefaultValue(FieldMappingContext context) {
            return "fi-xx";
        }
    },
    EMOTE {
        @Override
        public String getDefaultValue(FieldMappingContext context) {
            return switch (context) {
                case DISCORD -> ":grey_question:";
                case FRONTEND -> "";
            };
        }
    },
    IMAGE {
        @Override
        public String getDefaultValue(FieldMappingContext context) {
            return "";
        }
    },
    COLOUR {
        @Override
        public String getDefaultValue(FieldMappingContext context) {
            return "0,0,0";
        }
    };



    public abstract String getDefaultValue(FieldMappingContext context);

}