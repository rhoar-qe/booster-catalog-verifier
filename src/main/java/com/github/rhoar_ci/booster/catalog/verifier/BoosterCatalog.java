package com.github.rhoar_ci.booster.catalog.verifier;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BoosterCatalog {
    private BoosterCatalog() {} // avoid instantiation

    public static Map<String, List<Booster>> boostersByRuntime() throws Exception {
        RhoarBoosterCatalogService boosterCatalog = new RhoarBoosterCatalogService.Builder()
                .filter(KnownRuntimes.INSTANCE)
                .build();

        try {
            boosterCatalog.index().get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }

        return boosterCatalog.getBoosters()
                .stream()
                .flatMap(booster -> {
                    if (booster.getEnvironments().isEmpty()) {
                        return Stream.of(convert(booster));
                    } else {
                        return booster.getEnvironments()
                                .keySet()
                                .stream()
                                .map(env -> convert((RhoarBooster) booster.forEnvironment(env)));
                    }
                })
                .collect(Collectors.groupingBy(Booster::getRuntime));
    }

    private static Booster convert(RhoarBooster booster) {
        String environmentDescription = "";
        if (booster.getAppliedEnvironment() != null) {
            environmentDescription = "/" + booster.getAppliedEnvironment();
        }
        String description = booster.getRuntime().getId() + "/" + booster.getMission().getId()
                + "/" + booster.getVersion().getId() + environmentDescription;
        return new Booster(booster.getRuntime().getId(), description, booster.getGitRepo(), booster.getGitRef());
    }
}
