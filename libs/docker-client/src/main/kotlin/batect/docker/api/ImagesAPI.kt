/*
   Copyright 2017-2020 Charles Korn.

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

package batect.docker.api

import batect.docker.DockerHttpConfig
import batect.docker.DockerImage
import batect.docker.DockerImageReference
import batect.docker.ImageBuildFailedException
import batect.docker.ImagePullFailedException
import batect.docker.Json
import batect.docker.Tee
import batect.docker.build.BuildComplete
import batect.docker.build.BuildError
import batect.docker.build.BuildProgress
import batect.docker.build.ImageBuildContext
import batect.docker.build.ImageBuildContextRequestBody
import batect.docker.build.ImageBuildResponseBody
import batect.docker.build.LegacyImageBuildResponseBody
import batect.docker.pull.DockerRegistryCredentials
import batect.docker.toJsonObject
import batect.logging.LogMessageBuilder
import batect.logging.Logger
import batect.os.SystemInfo
import batect.primitives.CancellationContext
import batect.primitives.executeInCancellationContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import okio.Sink
import okio.sink
import java.io.ByteArrayOutputStream
import java.util.Base64

class ImagesAPI(
    httpConfig: DockerHttpConfig,
    systemInfo: SystemInfo,
    logger: Logger,
    private val buildResponseBodyFactory: () -> ImageBuildResponseBody = ::LegacyImageBuildResponseBody
) : APIBase(httpConfig, systemInfo, logger) {
    fun build(
        context: ImageBuildContext,
        buildArgs: Map<String, String>,
        dockerfilePath: String,
        imageTags: Set<String>,
        forcePull: Boolean,
        registryCredentials: Set<DockerRegistryCredentials>,
        outputSink: Sink?,
        cancellationContext: CancellationContext,
        onProgressUpdate: (BuildProgress) -> Unit
    ): DockerImage {
        logger.info {
            message("Building image.")
            data("context", context)
            data("buildArgs", buildArgs)
            data("imageTags", imageTags)
            data("forcePull", forcePull)
        }

        val request = createBuildRequest(context, buildArgs, dockerfilePath, imageTags, forcePull, registryCredentials)

        clientWithNoTimeout()
            .newCall(request)
            .executeInCancellationContext(cancellationContext) { response ->
                checkForFailure(response) { error ->
                    logger.error {
                        message("Could not build image.")
                        data("error", error)
                    }

                    throw ImageBuildFailedException("Building image failed: ${error.message}")
                }

                val image = processBuildResponse(response, outputSink, onProgressUpdate)

                logger.info {
                    message("Image built.")
                    data("image", image.id)
                }

                return image
            }
    }

    private fun createBuildRequest(
        context: ImageBuildContext,
        buildArgs: Map<String, String>,
        dockerfilePath: String,
        imageTags: Set<String>,
        forcePull: Boolean,
        registryCredentials: Set<DockerRegistryCredentials>
    ): Request {
        val url = baseUrl.newBuilder()
            .addPathSegment("build")
            .addQueryParameter("buildargs", buildArgs.toJsonObject().toString())
            .addQueryParameter("dockerfile", dockerfilePath)
            .addQueryParameter("pull", if (forcePull) "1" else "0")

        imageTags.forEach { url.addQueryParameter("t", it) }

        return Request.Builder()
            .post(ImageBuildContextRequestBody(context))
            .url(url.build())
            .addRegistryCredentialsForBuild(registryCredentials)
            .build()
    }

    private fun Request.Builder.addRegistryCredentialsForBuild(registryCredentials: Set<DockerRegistryCredentials>): Request.Builder {
        if (registryCredentials.isNotEmpty()) {
            val jsonCredentials = buildJsonObject {
                registryCredentials.forEach { put(it.serverAddress, it.toJSON()) }
            }

            val credentialBytes = jsonCredentials.toString().toByteArray()
            val encodedCredentials = Base64.getEncoder().encodeToString(credentialBytes)

            this.header("X-Registry-Config", encodedCredentials)
        }

        return this
    }

    private fun processBuildResponse(response: Response, outputStream: Sink?, onProgressUpdate: (BuildProgress) -> Unit): DockerImage {
        var builtImage: DockerImage? = null
        val outputBuffer = ByteArrayOutputStream()
        val sink = if (outputStream == null) { outputBuffer.sink() } else { Tee(outputBuffer.sink(), outputStream) }

        sink.use {
            val body = buildResponseBodyFactory()

            body.readFrom(response.body!!.charStream(), sink) { event ->
                when (event) {
                    is BuildProgress -> onProgressUpdate(event)
                    is BuildError -> throw ImageBuildFailedException("Building image failed: ${event.message}. Output from build process was:" + systemInfo.lineSeparator + outputBuffer.toString().trim().correctLineEndings())
                    is BuildComplete -> builtImage = event.image
                }
            }

            if (builtImage == null) {
                throw ImageBuildFailedException("Building image failed: daemon never sent built image ID.")
            }

            return builtImage!!
        }
    }

    fun pull(
        imageReference: DockerImageReference,
        registryCredentials: DockerRegistryCredentials?,
        cancellationContext: CancellationContext,
        onProgressUpdate: (JsonObject) -> Unit
    ) {
        logger.info {
            message("Pulling image.")
            data("originalImageReference", imageReference.originalReference)
            data("normalizedImageReference", imageReference.normalizedReference)
        }

        val url = urlForImages.newBuilder()
            .addPathSegment("create")
            .addQueryParameter("fromImage", imageReference.normalizedReference)
            .build()

        val request = Request.Builder()
            .post(emptyRequestBody())
            .url(url)
            .addRegistryCredentialsForPull(registryCredentials)
            .build()

        clientWithNoTimeout()
            .newCall(request)
            .executeInCancellationContext(cancellationContext) { response ->
                checkForFailure(response) { error ->
                    logger.error {
                        message("Could not pull image.")
                        data("error", error)
                    }

                    throw ImagePullFailedException("Pulling image '${imageReference.normalizedReference}' failed: ${error.message}")
                }

                response.body!!.charStream().forEachLine { line ->
                    val parsedLine = Json.default.parseToJsonElement(line).jsonObject

                    if (parsedLine.containsKey("error")) {
                        val message = parsedLine.getValue("error").jsonPrimitive.content
                            .correctLineEndings()

                        throw ImagePullFailedException("Pulling image '${imageReference.normalizedReference}' failed: $message")
                    }

                    onProgressUpdate(parsedLine)
                }
            }

        logger.info {
            message("Image pulled.")
        }
    }

    private fun Request.Builder.addRegistryCredentialsForPull(registryCredentials: DockerRegistryCredentials?): Request.Builder {
        if (registryCredentials != null) {
            val credentialBytes = registryCredentials.toJSON().toString().toByteArray()
            val encodedCredentials = Base64.getEncoder().encodeToString(credentialBytes)

            this.header("X-Registry-Auth", encodedCredentials)
        }

        return this
    }

    fun hasImage(imageReference: DockerImageReference): Boolean {
        logger.info {
            message("Checking if image has already been pulled.")
            data("originalImageReference", imageReference.originalReference)
            data("normalizedImageReference", imageReference.normalizedReference)
        }

        val url = urlForImages.newBuilder()
            .addPathSegment(imageReference.normalizedReference)
            .addPathSegment("json")
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        httpConfig.client.newCall(request).execute().use { response ->
            if (response.code == 404) {
                return false
            }

            checkForFailure(response) { error ->
                logger.error {
                    message("Could not check if image has already been pulled.")
                    data("error", error)
                }

                throw ImagePullFailedException("Checking if image '${imageReference.normalizedReference}' has already been pulled failed: ${error.message}")
            }

            return true
        }
    }

    private val urlForImages: HttpUrl = baseUrl.newBuilder()
        .addPathSegment("images")
        .build()

    private fun LogMessageBuilder.data(key: String, value: ImageBuildContext) = this.data(key, value, ImageBuildContext.serializer())
}
