package com.unimelb.swen90007.jspapp.domain;

/**
 * Represents the types of venues for events.
 */
public enum VenueType {
    /**
     * The event is held online.
     */
    ONLINE("online"),

    /**
     * The event is held in person.
     */
    IN_PERSON("in-person");

    private final String prettyString;

    VenueType(String prettyString) {
        this.prettyString = prettyString;
    }

    public static VenueType fromPretty(String type) {
        if (type.equalsIgnoreCase("online")) return ONLINE;
        return IN_PERSON;
    }

    public String getPrettyString() {
        return prettyString;
    }
}