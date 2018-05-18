package com.github.rhoar_ci.booster.catalog.verifier;

import java.io.File;

public final class Result {
    public final String description; // never `null`

    // either `exception != null`, in which case `exitCode` and `log` are irrelevant,
    // or `exception == null`, in which case `exitCode` and `log` are the real result

    public final Exception exception;

    public final int exitCode;
    public final File log;

    Result(String description, Exception exception) {
        this.description = description;
        this.exception = exception;
        this.exitCode = -1;
        this.log = null;
    }

    Result(String description, int exitCode, File log) {
        this.description = description;
        this.exception = null;
        this.exitCode = exitCode;
        this.log = log;
    }
}
