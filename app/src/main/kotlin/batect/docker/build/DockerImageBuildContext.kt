/*
   Copyright 2017-2019 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package batect.docker.build

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

data class DockerImageBuildContext(val entries: Set<DockerImageBuildContextEntry>)
data class DockerImageBuildContextEntry(val localPath: Path, val contextPath: String)

class DockerImageBuildContextFactory(private val ignoreParser: DockerIgnoreParser) {
    fun createFromDirectory(contextDirectory: Path, dockerfilePath: String): DockerImageBuildContext {
        val ignoreList = ignoreParser.parse(contextDirectory.resolve(".dockerignore"))

        Files.walk(contextDirectory).use { stream ->
            val files = stream
                .filter { it != contextDirectory }
                .map { DockerImageBuildContextEntry(it, contextDirectory.relativize(it).toString()) }
                .filter { ignoreList.shouldIncludeInContext(it.contextPath, dockerfilePath) }
                .collect(Collectors.toSet())

            return DockerImageBuildContext(files)
        }
    }
}
