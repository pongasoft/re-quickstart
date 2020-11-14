/*
 * Copyright (c) 2020 pongasoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @author Yan Pujante
 */

import org.w3c.dom.Image
import org.w3c.files.Blob
import kotlin.js.Date
import kotlin.js.Promise

/**
 * Maintains the information for each file in the zip archive. Will use permission and date to carry it over to the
 * generated zip file. */
sealed class StorageResource(val path: String, val date: Date?, val unixPermissions: Int?)

/**
 * A file resource (text file) */
class FileResource(path: String, date: Date?, unixPermissions: Int?, val content: String) :
    StorageResource(path, date, unixPermissions)

/**
 * An image resource (both blob and image) */
class ImageResource(path: String, date: Date?, unixPermissions: Int?, val blob: Blob, val image: Image) :
    StorageResource(path, date, unixPermissions) {

    val key : String  get() = path.split("/").last().removeSuffix(".png")
}

/**
 * The image provider is in charge of providing static / stock images */
interface ImageProvider {

    companion object {
        protected const val AUDIO_SOCKET_IMAGE = "images/BuiltIn/Cable_Attachment_Audio_01_1frames.png"
        protected const val PLACEHOLDER_IMAGE = "images/BuiltIn/Placeholder.png"
        protected const val TAPE_HORIZONTAL_IMAGE = "images/BuiltIn/Tape_Horizontal_1frames.png"
    }

    /**
     * @return the resource given its path or `null` if it doesn't exist */
    fun findImageResourceByPath(path: String): ImageResource?

    fun getAudioSocketImageResource() = findImageResourceByPath(AUDIO_SOCKET_IMAGE)!!
    fun getPlaceholderImageResource() = findImageResourceByPath(PLACEHOLDER_IMAGE)!!
    fun getTapeHorizontalImageResource() = findImageResourceByPath(TAPE_HORIZONTAL_IMAGE)!!
}

/**
 * Storage is in charge of keeping all the resources (text files and images) loaded from `plugin-<version>.zip` */
class Storage(val resources: Array<out StorageResource>) : ImageProvider {

    // findImageResourceByPath
    override fun findImageResourceByPath(path: String): ImageResource? =
        resources.find { it.path == path } as? ImageResource

    companion object {
        /**
         * Main API to create a storage. Loads the file `plugin-<version>.zip` asynchronously:
         *
         * - fetches the zip file
         * - read each entry in the zip file
         * - convert it into an `ImageResource` if necessary (thus converting the blob into an `Image`)
         */
        fun load(version: String): Promise<Storage> =
            fetchBlob("plugin-$version.zip").then { zipBlob ->
                val zip = JSZip()
                zip.loadAsync(zipBlob).then {
                    val promises = mutableListOf<Promise<StorageResource>>()
                    zip.forEach { path, file ->
                        // handle images
                        if (path.endsWith(".png")) {
                            promises.add(
                                file.async("blob").then { blob ->
                                    blob as Blob
                                    Image.asyncLoad(org.w3c.dom.url.URL.Companion.createObjectURL(blob)).then { image ->
                                        ImageResource(path, file.date, file.unixPermissions, blob, image)
                                    }
                                }.flatten()
                            )
                        } else {
                            // handle text files
                            if (!(path.startsWith("__MACOSX") ||
                                        path.startsWith(".idea") ||
                                        path.endsWith(".DS_Store"))
                            ) {
                                promises.add(
                                    file.async("string").then { content ->
                                        FileResource(path, file.date, file.unixPermissions, content.toString())
                                    }
                                )
                            }
                        }
                    }

                    Promise.all(promises.toTypedArray())
                }.flatten()
            }.flatten().then {
                Storage(it)
            }
    }
}
