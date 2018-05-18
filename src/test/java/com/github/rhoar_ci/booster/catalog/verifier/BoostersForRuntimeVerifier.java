package com.github.rhoar_ci.booster.catalog.verifier;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public final class BoostersForRuntimeVerifier implements Callable<List<Result>> {
    private final List<Booster> boosters;
    private final Path workDir;

    public BoostersForRuntimeVerifier(List<Booster> boosters, Path workDir) {
        this.boosters = boosters;
        this.workDir = workDir;
    }

    @Override
    public List<Result> call() {
        File mavenRepoDir = null;
        try {
            mavenRepoDir = Files.createTempDirectory(workDir, "maven-repo").toFile();
        } catch (IOException e) {
            return Collections.singletonList(new Result("creating Maven repo dir in " + workDir, e));
        }

        List<Result> results = new ArrayList<>();
        for (Booster booster : boosters) {
            Result result;
            try {
                File boosterDir = Files.createTempDirectory(workDir, "booster").toFile();
                result = testBooster(booster, boosterDir, mavenRepoDir);
            } catch (Exception e) {
                result = new Result("building " + booster + " in " + workDir, e);
            }
            results.add(result);
        }
        return results;
    }

    private Result testBooster(Booster booster, File boosterDir, File mavenRepoDir) throws GitAPIException, FileNotFoundException, MavenInvocationException {
        System.out.println(""
                + "====================================================================================================\n"
                + booster + "\n"
                + "    " + boosterDir + "\n"
                + "====================================================================================================\n"
        );

        Git.cloneRepository()
                .setURI(booster.gitUrl)
                .setBranch(booster.gitRef)
                .setCloneSubmodules(true)
                .setDirectory(boosterDir)
                .call();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(boosterDir, "pom.xml"));
        request.setGoals(Arrays.asList("clean", "install"));
        request.setMavenOpts("-DskipTests=true");
        request.setBatchMode(true);
        // -Popenshift is intentionally _not_ enabled, because if it was, Fabric8 Maven plugin would try to connect
        // to OpenShift, upload the built image etc., and that just doesn't add any value

        File logFile = new File(boosterDir, "maven.log");
        PrintStream logStream = new PrintStream(logFile);
        PrintStreamHandler logHandler = new PrintStreamHandler(logStream, false);

        Invoker invoker = new DefaultInvoker();
        invoker.setLocalRepositoryDirectory(mavenRepoDir);
        invoker.setOutputHandler(logHandler);
        invoker.setErrorHandler(logHandler);
        InvocationResult result = invoker.execute(request);

        return new Result("" + booster, result.getExitCode(), logFile);
    }
}
