package com.github.rhoar_ci.booster.catalog.verifier;

import org.apache.maven.shared.invoker.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class BoosterCatalogTest {
    @Parameters(name = "{0}")
    public static Iterable<?> data() throws Exception {
        return BoosterCatalog.list();
    }

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Parameter
    public Booster booster;

    @Test
    public void cloneAndBuildMavenProject() throws IOException, GitAPIException, MavenInvocationException {
        System.out.println("====================================================================================================");
        System.out.println(booster);
        System.out.println("====================================================================================================");

        File workDir = tmp.newFolder();
        Path workDirPath = workDir.toPath();

        Git.cloneRepository()
                .setURI(booster.gitUrl)
                .setBranch(booster.gitRef)
                .setCloneSubmodules(true)
                .setDirectory(workDir)
                .call();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(workDirPath.resolve("pom.xml").toFile());
        request.setGoals(Arrays.asList("clean", "install"));
        request.setMavenOpts("-DskipTests=true");
        request.setBatchMode(true);

        Invoker invoker = new DefaultInvoker();
        InvocationResult result = invoker.execute(request);

        assertThat(result.getExitCode())
                .as("%s should be buildable by `mvn clean install -DskipTests`", booster)
                .isEqualTo(0);
    }
}
