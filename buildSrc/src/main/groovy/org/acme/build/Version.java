package org.acme.build;

public abstract class Version {
    private Version() {
    }

    public static VersionInfo info;

    public static void set(VersionInfo info) {
        Version.info = info;
    }

}
