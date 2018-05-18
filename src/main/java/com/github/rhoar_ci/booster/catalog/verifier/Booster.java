package com.github.rhoar_ci.booster.catalog.verifier;

import java.util.Objects;

public final class Booster {
    public final String runtime;
    public final String description;
    public final String gitUrl;
    public final String gitRef;

    public Booster(String runtime, String description, String gitUrl, String gitRef) {
        this.runtime = runtime;
        this.description = description;
        this.gitUrl = gitUrl;
        this.gitRef = gitRef;
    }

    public String getRuntime() {
        return runtime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booster)) return false;
        Booster booster = (Booster) o;
        return Objects.equals(runtime, booster.runtime) &&
                Objects.equals(description, booster.description) &&
                Objects.equals(gitUrl, booster.gitUrl) &&
                Objects.equals(gitRef, booster.gitRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runtime, description, gitUrl, gitRef);
    }

    @Override
    public String toString() {
        return "booster " + description + " from " + gitUrl + "@" + gitRef;
    }
}
