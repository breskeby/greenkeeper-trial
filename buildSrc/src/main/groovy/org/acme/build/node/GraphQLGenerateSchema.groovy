package org.acme.build.node

import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.ParallelizableTask

@ParallelizableTask
@CacheableTask
class GraphQLGenerateSchema extends YarnTask {
    @InputFile
    File schemaInputFile

    @OutputFile
    File schemaOutputFile

    GraphQLGenerateSchema() {
        group = 'Node'
        description = 'Generate the GraphQL schema.'
        setYarnCommand('run')
        args = ['generate-graphql-schema']
    }
}
