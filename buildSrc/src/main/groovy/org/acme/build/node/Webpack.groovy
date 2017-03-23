package org.acme.build.node

import org.gradle.api.tasks.*

@ParallelizableTask
@CacheableTask
class Webpack extends NodeScript {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    File configFile

    @OutputDirectory
    File outputDir

    @Input
    boolean noCompress

    @Input
    boolean debugJs

    @TaskAction
    void exec() {
        project.delete(outputDir)

        def args = [
            "--config", configFile.absolutePath,
            "--env.baseOutputPath=${outputDir.absolutePath}"
        ]

        if (noCompress) {
            args << "--env.noCompress=true"
        }

        if (debugJs) {
            args << "--env.debugJs=true"
        }

        run(script("webpack/bin/webpack.js"), *args)

        if (!outputDir.directory || outputDir.listFiles().size() == 0) {
            def logFile = project.file(logFileName)
            if (logFile.file) {
                throw new IllegalStateException("webpack did not produce any files, webpack output: \n" + logFile.text)
            } else {
                throw new IllegalStateException("webpack did not produce any files and no log output… good luck.")
            }
        }
    }

}
