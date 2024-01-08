package com.example.ledcontrollmkii;

public class ScheduleEntry {

    // Private field representing the property
    private String row_id;
    private String mode;
    private String eventTime;

    public String getScheduleRow() {
        return row_id;
    }

    public String getMode() {
        return mode;
    }
    public String getEventTime() {
        return eventTime;
    }


    private ScheduleEntry(Builder builder) {
        this.row_id = builder.row_id;
        this.mode = builder.mode;
        this.eventTime = builder.eventTime;
    }
    public static class Builder {
        private String row_id;
        private String mode;
        private String eventTime;

        public Builder row_id(String row_id) {
            this.row_id = row_id;
            return this;
        }
        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public Builder eventTime(String eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public ScheduleEntry build() {
            return new ScheduleEntry(this);
        }
    }
}



