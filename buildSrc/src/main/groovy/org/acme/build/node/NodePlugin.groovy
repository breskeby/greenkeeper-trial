package org.acme.build.node

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class NodePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply plugin: com.moowork.gradle.node.NodePlugin

        project.extensions.getByType(NodeExtension).with {
            version = "6.2.2"
            npmVersion = "3.10.10"
            yarnVersion = "0.21.3"
            download = true
            nodeModulesDir = project.projectDir
        }

        [NodeTask, YarnTask, AbstractNodeScript].each { baseTaskType ->
            project.tasks.withType(baseTaskType) {
                if (it != project.tasks.yarn) {
                    it.dependsOn project.tasks.yarn
                }
            }
        }
    }

}
