package com.robotdebris.ncaaps2scheduler.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AppConstants {
    private AppConstants() {
        // restrict instantiation
    }

    public static final List<String> INDEPENDENT_STRINGS = Collections
            .unmodifiableList(Arrays.asList("Independents",
                    "FBS Independents"));
}
