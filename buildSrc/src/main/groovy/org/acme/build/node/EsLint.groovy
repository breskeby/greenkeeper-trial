package org.acme.build.node

import org.gradle.api.GradleException
import org.gradle.api.tasks.*

@ParallelizableTask
class EsLint extends NodeScript {

    @InputDirectory
    File sourceDir

    @Input
    List<String> exts = [".js", ".jsx"]

    @OutputFile
    File reportFile = project.file("$project.buildDir/reports/${name}.txt")

    @TaskAction
    void exec() {
        reportFile.delete()
        def args = []
        args.addAll exts.collect { ['--ext', it] }.flatten()
        def configFile = new File(sourceDir, ".eslintrc")
        if (configFile.exists()) {
            args << "-c" << configFile.absolutePath
        }
        def ignoreFile = new File(sourceDir, ".eslintignore")
        if (ignoreFile.exists()) {
            args << "--ignore-path" << ignoreFile.absolutePath
        }
        args.addAll(['-f', 'compact', sourceDir.absolutePath, "-o", reportFile.absolutePath])
        try {
            run(script("eslint/bin/eslint.js"), *args)
        } catch (GradleException ge) {
            if (reportFile.file && reportFile.length() > 0) {
                throw new GradleException("Error linting javascript:\n\n${reportFile.text}", ge)
            } else {
                reportFile.createNewFile()
                throw ge
            }
        }
    }
}

