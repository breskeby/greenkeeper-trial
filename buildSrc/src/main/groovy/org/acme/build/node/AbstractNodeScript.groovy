package org.acme.build.node

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.exec.NodeExecRunner
import org.gradle.api.DefaultTask

abstract class AbstractNodeScript extends DefaultTask {

    AbstractNodeScript() {
        inputs.files({ nodeModulesDir() })
    }

    protected NodeExecRunner runner() {
        new NodeExecRunner(project)
    }

    String script(String path) {
        new File(nodeModulesDir(), path).absolutePath
    }

    protected File nodeModulesDir() {
        new File(project.extensions.getByType(NodeExtension).nodeModulesDir, "node_modules")
    }
}
