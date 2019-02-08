package com.github.rhoar_ci.booster.catalog.verifier;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

enum KnownRuntimes implements Predicate<RhoarBooster> { // enum-singleton
    INSTANCE;

    private static final Set<String> KNOWN_RUNTIMES = new HashSet<>(Arrays.asList(
            "spring-boot",
            "vert.x",
            "thorntail"
    ));

    @Override
    public boolean test(RhoarBooster rhoarBooster) {
        return rhoarBooster.getRuntime() != null && KNOWN_RUNTIMES.contains(rhoarBooster.getRuntime().getId());
    }
}
