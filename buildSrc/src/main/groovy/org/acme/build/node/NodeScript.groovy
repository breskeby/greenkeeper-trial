package org.acme.build.node

import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecSpec

abstract class NodeScript extends AbstractNodeScript {

    String logFileName
    boolean captureLog = true

    @Input
    @Optional
    String nodeEnv

    NodeScript() {
        logFileName = "${project.buildDir}/logs/${name}.log"
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void run(String... args) {
        run { arguments.addAll(args.toList()) }
    }

    void run(@DelegatesTo(NodeExecRunner) Closure<?> closure) {
        def runner = runner()
        runner.with(closure)
        if (captureLog) {
            project.file(logFileName).parentFile.mkdirs()
            new FileOutputStream(logFileName).withStream { out ->
                runner.execOverrides = { ExecSpec it ->
                    it.standardOutput = out
                    it.errorOutput = out
                    if (this.nodeEnv) {
                        it.environment("NODE_ENV", this.nodeEnv)
                    }
                }
                try {
                    runner.execute()
                } catch (Exception e) {
                    throw new GradleException("An error occurred while running node, see output at: ${project.file(logFileName).toURI()}", e)
                }
            }
        } else {
            // useful for e.g. running tests in continuous mode and seeing the output
            try {
                runner.execOverrides = { ExecSpec it ->
                    if (this.nodeEnv) {
                        it.environment("NODE_ENV", this.nodeEnv)
                    }
                }
                runner.execute()
            } catch (Exception e) {
                throw new GradleException("An error occurred while running node", e)
            }
        }
    }

}
