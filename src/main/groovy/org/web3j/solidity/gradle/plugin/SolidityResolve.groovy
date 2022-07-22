/*
 * Copyright 2020 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.solidity.gradle.plugin

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*

@CacheableTask
class SolidityResolve extends DefaultTask {

    private FileTree sources

    private Set<String> allowPaths

    private File packageJson

    @TaskAction
    void resolveSolidity() {
        final Set<String> libraries = []
        final File nodeProjectDir = project.node.nodeProjectDir.asFile.get()

        for (def contract in sources) {
            def imports = ImportsResolver.instance.resolveImports(contract, nodeProjectDir)
            for (provider in imports) {
                String key = provider.getKey();
                libraries.add(key)
                allowPaths.add("$nodeProjectDir.path/node_modules/$key")

                if(!project.solidity.pathRemappings.containsKey(key)) {
                    project.solidity.pathRemappings.put(key, imports.get(key))
                }
            }
        }

        def jsonMap = [
                "name"        : project.name,
                "description" : project.description == null ? " " : project.description,
                "repository"  : " ",
                "license"     : "UNLICENSED",
                "dependencies": libraries.collectEntries {
                    [(it): "latest"]
                }
        ]

        if (!packageJson.exists()) {
            packageJson.parentFile.mkdirs()
            packageJson.createNewFile()
            def jsonBuilder = new JsonBuilder()
            jsonBuilder jsonMap
            packageJson.append(JsonOutput.prettyPrint(jsonBuilder.toString()) + "\n")
        }
    }

    @InputFiles
    @PathSensitive(value = PathSensitivity.RELATIVE)
    @SkipWhenEmpty
    FileTree getSources() {
        return sources
    }

    void setSources(FileTree sources) {
        this.sources = sources
    }

    @OutputFile
    File getPackageJson() {
        return packageJson
    }

    void setPackageJson(File packageJson) {
        this.packageJson = packageJson
    }

    @Input
    @Optional
    Set<String> getAllowPaths() {
        return allowPaths
    }

    void setAllowPaths(Set<String> allowPaths) {
        this.allowPaths = allowPaths
    }
}
