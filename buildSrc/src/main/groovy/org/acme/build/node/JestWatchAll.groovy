package org.acme.build.node

import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.tasks.InputDirectory

// This runs continuously, so it shouldn't be parallelizable or have outputs (it should never be considered up to date)
class JestWatchAll extends YarnTask {
    @InputDirectory
    File testSrc

    @InputDirectory
    File mainSrc

    JestWatchAll() {
        group = 'Node'
        description = 'Run all unit tests with Jest continuously.'
        setYarnCommand('run')
        args = ['watch-all']
    }
}
