package org.acme.build.node

import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskAction
import org.gradle.deployment.internal.DeploymentHandle
import org.gradle.deployment.internal.DeploymentRegistry
import org.gradle.process.ExecSpec

class ContinuousNodeScript extends AbstractNodeScript {

    List<String> args = []

    Runnable onStop = {
        // nothing
    }

    @TaskAction
    void start() {
        if (project.gradle.startParameter.continuous) {
            def deploymentRegistry = services.get(DeploymentRegistry)
            def deploymentHandle = deploymentRegistry.get(Handle, getPath())
            if (deploymentHandle == null) {
                deploymentRegistry.register(getPath(), new Handle(runner(), args, onStop))
            }
        } else {
            run(runner(), args)
        }
    }

    private static run(NodeExecRunner runner, List<String> args) {
        runner.setArguments(args)
        runner.execOverrides = { ExecSpec execSpec ->
            execSpec.errorOutput = System.err
            execSpec.standardOutput = System.out
            execSpec.ignoreExitValue = true
        }
        runner.execute()
    }

    private static class Handle implements DeploymentHandle {

        private final Runnable onStop
        boolean stopped

        Handle(NodeExecRunner runner, List<String> args, Runnable onStop) {
            this.onStop = onStop
            Thread.start { run(runner, args) }

            // Gradle won't shut down deployments on SIGINT
            // Under some circumstances, the child process could detach
            Runtime.runtime.addShutdownHook {
                onStop.run()
            }
        }

        @Override
        boolean isRunning() {
            !stopped
        }

        @Override
        void onNewBuild(Gradle gradle) {

        }

        @Override
        void stop() {
            onStop.run()
            stopped = true
        }
    }
}
