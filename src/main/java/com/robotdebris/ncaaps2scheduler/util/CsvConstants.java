package com.robotdebris.ncaaps2scheduler.util; // Or constants package

public final class CsvConstants { // final prevents inheritance

    private CsvConstants() {
    } // private constructor prevents instantiation

    // --- Team CSV Header Names ---
    public static final String HEADER_TEAM_ID = "TGID";
    public static final String HEADER_TEAM_NAME = "TDNA";
    public static final String HEADER_TEAM_NICKNAME = "TMNA";
    public static final String HEADER_CONFERENCE_ID = "CGID";
    public static final String HEADER_DIVISION_ID = "DGID";
    public static final String HEADER_PRESTIGE = "TPRS";
    public static final String HEADER_STADIUM_ID = "SGID";
    public static final String HEADER_ABBREVIATION = "TMAA";
    // Add others used by parser or exporter

    // --- Team CSV Export Structure ---
    // Define the export order and columns using the constants above
    public static final String[] TEAM_EXPORT_HEADERS = {
            HEADER_TEAM_ID,     // "TGID"
            HEADER_TEAM_NAME,   // "TDNA"
            HEADER_TEAM_NICKNAME, // "TMNA"
            HEADER_ABBREVIATION, // "TMAA"
            HEADER_CONFERENCE_ID, // "CGID"
            HEADER_DIVISION_ID, // "DGID"
            HEADER_PRESTIGE,    // "TPRS"
            HEADER_STADIUM_ID   // "SGID"
    };

    // You could add constants for other CSV files here too if desired
    // public static final String SCHED_HEADER_AWAY_ID = "GATG";
    // public static final String[] SCHEDULE_EXPORT_HEADERS = { ... };
}