package com.github.rhoar_ci.booster.catalog.verifier;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BoosterCatalogTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void verifyBoosterCatalog() throws Exception {
        Map<String, List<Booster>> catalog = BoosterCatalog.boostersByRuntime();

        ExecutorService executor = Executors.newFixedThreadPool(catalog.size());
        List<Future<List<Result>>> results = new ArrayList<>();

        for (Map.Entry<String, List<Booster>> entry : catalog.entrySet()) {
            String runtime = entry.getKey();
            List<Booster> boostersForSingleRuntime = entry.getValue();

            results.add(executor.submit(
                    new BoostersForRuntimeVerifier(boostersForSingleRuntime, tmp.newFolder(runtime).toPath())
            ));
        }

        executor.shutdown();

        if (executor.awaitTermination(1, TimeUnit.HOURS)) {
            for (Future<List<Result>> future : results) {
                List<Result> resultsForRuntime = future.get();

                for (Result result : resultsForRuntime) {
                    softly.assertThat(result.exception)
                            .as("No exception when %s expected", result.description)
                            .isNull();

                    if (result.exception == null) {
                        softly.assertThat(result.exitCode)
                                .as("%s should be buildable by `mvn clean install -Popenshift -DskipTests`", result.description)
                                .isEqualTo(0);
                        if (result.exitCode != 0) {
                            softly.fail(""
                                    + "Maven build log\n"
                                    + "===============\n"
                                    + new String(Files.readAllBytes(result.log.toPath()), StandardCharsets.UTF_8)
                            );
                        }
                    }
                }
            }
        } else {
            softly.fail("Failed to check all boosters in 1 hour");
        }
    }
}
