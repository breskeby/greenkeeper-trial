package org.acme.build.node

import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.tasks.*

@ParallelizableTask
class Test extends YarnTask {
    @InputDirectory
    File testSrc

    @InputDirectory
    File mainSrc

    @OutputDirectory
    File outputDir = project.file("$project.buildDir/reports/unit-tests")

    Test() {
        group = 'Node'
        description = 'Run unit tests with Jest.'
        setYarnCommand('run')
        args = ['test']
    }
}
