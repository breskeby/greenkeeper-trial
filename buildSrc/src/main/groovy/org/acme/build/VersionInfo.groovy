package org.acme.build
import groovy.transform.Immutable

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Immutable
class VersionInfo {

    final String commitId
    final String buildNumber
    final String buildId
    final String timestamp

    static dev() {
        new VersionInfo([timestamp: Instant.now().truncatedTo(ChronoUnit.DAYS).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)])
    }
}
