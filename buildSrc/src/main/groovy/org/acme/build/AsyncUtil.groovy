package org.acme.build

import org.gradle.internal.exceptions.Contextual

import java.time.Duration
import java.time.Instant

import static java.time.Instant.now

abstract class AsyncUtil {
    static <T> T waitFor(Duration timeout, Duration interval, String description, Closure<T> closure) {
        Instant stopAt = now().plus(timeout)
        Throwable error = null

        while (now() < stopAt) {
            try {
                return closure.call()
            } catch (Exception e) {
                error = e
            } catch (AssertionError e) {
                error = e
            }

            if (now().plus(interval) < stopAt) {
                Thread.sleep(interval.toMillis())
            } else {
                break
            }
        }

        throw new TimeoutException("'$description' did not succeed after $timeout.seconds", error)
    }

    @Contextual
    static class TimeoutException extends RuntimeException {
        TimeoutException(String var1, Throwable var2) {
            super(var1, var2)
        }
    }
}
