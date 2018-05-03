package com.github.rhoar_ci.booster.catalog.verifier;

import io.fabric8.launcher.booster.catalog.rhoar.RhoarBooster;
import io.fabric8.launcher.booster.catalog.rhoar.RhoarBoosterCatalogService;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BoosterCatalog {
    private BoosterCatalog() {} // avoid instantiation

    public static List<Booster> list() throws Exception {
        // TODO configure catalog URL and ref
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
                .flatMap(b -> {
                    if (b.getEnvironments().isEmpty()) {
                        return Stream.of(convert(b));
                    } else {
                        return b.getEnvironments()
                                .keySet()
                                .stream()
                                .map(env -> convert((RhoarBooster) b.forEnvironment(env)));
                    }
                })
                .collect(Collectors.toList());
    }

    private static Booster convert(RhoarBooster booster) {
        String environmentDescription = "";
        if (booster.getAppliedEnvironment() != null) {
            environmentDescription = "/" + booster.getAppliedEnvironment();
        }
        String description = booster.getRuntime().getId() + "/" + booster.getMission().getId()
                + "/" + booster.getVersion().getId() + environmentDescription;
        return new Booster(description, booster.getGitRepo(), booster.getGitRef());
    }
}
