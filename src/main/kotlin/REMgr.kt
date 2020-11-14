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

import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.html.dom.create
import kotlinx.html.pre
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLImageElement
import org.w3c.files.Blob
import kotlin.js.Date
import kotlin.js.Promise

/**
 * A file entry for the tree:
 *
 * @param html a function that knows how to render the file as an html element (ex: an image or text) and is only
 *             invoked if the user request to preview the file by clicking on its name
 * @param zip a promise to the content for the zip file (`String` or `Blob`). It is a promise because the background
 *            images uses the [HTMLCanvasElement.toNamedBlob()] which return a promise
 * @param resource for all "static" entries coming from `plugin.zip` and stored in the storage (gives access to date
 *                 and unix permission) */
class FileTreeEntry(val html: () -> HTMLElement, val zip: Promise<Any>, val resource: StorageResource? = null)

/**
 * A file tree is a map indexed by the full path to the file (ex: `GUI2D/device_2D.lua`) */
typealias FileTree = Map<String, FileTreeEntry>

/**
 * The zip file that gets generated with the content of the rack extension */
class REZip(val filename: String, val content: Blob)

/**
 * The manager for rack extensions. In charge of creating them (based on the form) via [createRE] and managing the
 * file tree and zip file */
class REMgr(private val storage: Storage) {

    companion object {
        /**
         * Main API to create [REMgr]. Loads the `plugin-<version>.zip` file hence it returns a promise.*/
        fun load(version: String): Promise<REMgr> {
            return Storage.load(version).then { REMgr(it) }
        }
    }

    /**
     * Creates a properly defined Rack Extension based on the user input. Adds the necessary device name, placeholder,
     * sockets and routing required */
    fun createRE(form: HTMLFormElement): RackExtension {

        val re = RackExtension.fromForm(form)

        val margin = 10

        addDeviceNameProperty(re, margin)
        addPlaceholderProperty(re)

        val audioSocket = storage.getAudioSocketImageResource()

        val centerX = re.getWidth() / 2
        val centerY = re.getHeight(Panel.back) / 2

        when (re.info.type) {
            // Effect: stereo in / stereo out
            RackExtension.Type.studio_fx, RackExtension.Type.creative_fx -> {
                // Main in
                val mainInput =
                    AudioStereoPair(
                        left = AudioSocket(
                            "MainInLeft",
                            AudioSocketType.input,
                            centerX - margin - audioSocket.image.width,
                            centerY - margin - audioSocket.image.height,
                            audioSocket
                        ),
                        right = AudioSocket(
                            "MainInRight",
                            AudioSocketType.input,
                            centerX + margin,
                            centerY - margin - audioSocket.image.height,
                            audioSocket
                        )
                    )

                // Main out
                val mainOutput =
                    AudioStereoPair(
                        left = AudioSocket(
                            "MainOutLeft",
                            AudioSocketType.output,
                            centerX - margin - audioSocket.image.width,
                            centerY + margin,
                            audioSocket
                        ),
                        right = AudioSocket(
                            "MainOutRight",
                            AudioSocketType.output,
                            centerX + margin,
                            centerY + margin,
                            audioSocket
                        )
                    )

                re.addREProperty(mainInput)
                re.addREProperty(mainOutput)
                re.addREAutoRouting(StereoEffectRoutingHint(mainInput, mainOutput))
                re.addREAutoRouting(StereoAudioRoutingTarget(mainOutput))
                re.addREAutoRouting(StereoAudioRoutingTarget(mainInput))
                re.addREAutoRouting(EffectAutoBypassRouting(mainInput, mainOutput))
            }

            // Instrument : stereo out
            RackExtension.Type.instrument -> {
                // Main out
                val mainOutput = AudioStereoPair(
                    left = AudioSocket(
                        "MainOutLeft",
                        AudioSocketType.output,
                        centerX - margin - audioSocket.image.width,
                        centerY + margin - audioSocket.image.height / 2,
                        audioSocket
                    ),
                    right = AudioSocket(
                        "MainOutRight",
                        AudioSocketType.output,
                        centerX + margin,
                        centerY + margin - audioSocket.image.height / 2,
                        audioSocket
                    )
                )
                re.addREProperty(mainOutput)
                re.addREAutoRouting(StereoInstrumentRoutingHint(mainOutput))
                re.addREAutoRouting(StereoAudioRoutingTarget(mainOutput))
            }
            // Helper/Note player: no in or out
            RackExtension.Type.helper, RackExtension.Type.note_player -> {
            }
        }

        return re
    }

    /**
     * Add the device name (tape) to all panels on the top left (accounting for the rails in the back) */
    private fun addDeviceNameProperty(re: RackExtension, margin: Int) {
        val prop = REBuiltInProperty("DeviceName")
        val img = storage.getTapeHorizontalImageResource()
        re.availablePanels.forEach { panel ->
            val (x, y) = re.getTopLeft(panel)
            prop.addWidget(panel, REPropertyWidget.Type.device_name, img, x + margin, y + margin)
        }
        re.addREProperty(prop)
    }

    /**
     * Adds the placeholder on the back panel */
    private fun addPlaceholderProperty(re: RackExtension) {
        val img = storage.getPlaceholderImageResource()
        val prop = REPlaceholderProperty(
            "Placeholder",
            re.getWidth() - img.image.width - PanelGUI.emptyMargin,
            re.getHeight(Panel.back) - img.image.height - PanelGUI.emptyMargin,
            img
        )
        re.addREProperty(prop)
    }

    /**
     * Generates the panel preview which consists of the panel background and all properties rendered
     * (like audio sockets, display name, etc...). Returns an `<img>` element ready to be added to the DOM */
    fun generatePreview(re: RackExtension, panel: Panel) = with(document.createElement("img")) {
        this as HTMLImageElement
        src = re.generatePanelPreviewCanvas(panel).toDataURL(type = "image/png")
        document.findMetaContent("X-re-quickstart-re-preview-classes")?.let {
            it.split("|").forEach { c -> addClass(c) }
        }
        this
    }

    /**
     * Generates the `<img>` element for the static image resource */
    private fun generateStaticImgContent(imageResource: ImageResource) = with(document.createElement("img")) {
        this as HTMLImageElement
        src = imageResource.image.src
        document.findMetaContent("X-re-quickstart-re-files-preview-classes")?.let { addClass(it) }
        this
    }

    /**
     * Generates the `<img>` element for the dynamic image resource (background panel) */
    private fun generatePanelImgContent(re: RackExtension, panel: Panel) = with(document.createElement("img")) {
        this as HTMLImageElement
        src = re.generatePanelCanvas(panel).toDataURL("image/png")
        document.findMetaContent("X-re-quickstart-re-files-preview-classes")?.let { addClass(it) }
        this
    }

    /**
     * Generates the file tree for the rack extension. The file tree is generated by concatenating:
     *
     * - all (static) files included from `plugin-<version>.zip` and processing them through the content processor
     *   (files under `skeletons/common` and `skeletons/<re type>`)
     * - all relevant static images (like audio sockets if present, tape, etc...)
     * - background panels for all available panels (2 or 4)
     *
     * @see FileTree
     * @see FileTreeEntry
     */
    fun generateFileTree(re: RackExtension): FileTree {

        val contentProcessor = re.getContentProcessor()

        // we look for (static) files under skeletons/<plugin_type> and skeletons/common
        // files under skeletons/<plugin_type> overrides file under skeletons/common
        // if the file is a regular file, it will be token processed
        // if the file is an image, it is simply copied
        val resources =
            arrayOf("skeletons/${re.info.type}/", "skeletons/common/").flatMap { prefix ->
                storage.resources
                    .filter { !it.path.endsWith("/") && it.path.startsWith(prefix) }
                    .map { resource -> Pair(resource.path.removePrefix(prefix), resource) }
            }
                .distinctBy { it.first }
                .map { (path, resource) ->
                    Pair(path,
                        FileTreeEntry(
                            resource = resource,
                            html = {
                                when (resource) {
                                    is FileResource -> document.create.pre { +contentProcessor.processContent(resource.content) }
                                    is ImageResource -> generateStaticImgContent(resource)
                                }
                            },
                            zip = when(resource) {
                                is FileResource -> Promise.resolve(contentProcessor.processContent(resource.content))
                                is ImageResource -> Promise.resolve(resource.blob)
                            }
                        )
                    )
                }

        // helper for dynamic image (background panel)
        fun genDynamicImagePair(panel: Panel): Pair<String, FileTreeEntry> {
            val name = "GUI2D/${re.getPanelImageKey(panel)}.png"
            return Pair(name,
                FileTreeEntry(
                    html = { generatePanelImgContent(re, panel) },
                    zip = re.generatePanelCanvas(panel).toNamedBlob(name).then { it.second }
                )
            )
        }

        // helper for static images (ex: audio socket...)
        fun genStaticImagePair(imageResource: ImageResource): Pair<String, FileTreeEntry> {
            val name = "GUI2D/${imageResource.key}.png"
            return Pair(name,
                FileTreeEntry(
                    resource = imageResource,
                    html = { generateStaticImgContent(imageResource) },
                    zip = Promise.resolve(imageResource.blob)
                )
            )
        }

        return mapOf(
            *re.availablePanels.map { genDynamicImagePair(it) }.toTypedArray(),
            *re.getPropertyImages().map { genStaticImagePair(it) }.toTypedArray(),
            *resources.toTypedArray()
        )
    }

    /**
     * Generates the (promise) of the zip file */
    fun generateZip(root: String, tree: FileTree): Promise<REZip> {
        val zip = JSZip()
        val rootDir = zip.folder(root)

        // helper class to pass down the `then` chain
        class ZipEntry(val name: String, val resource: StorageResource?, val content: Any)

        return Promise.all(tree.map { (name, entry) ->
            entry.zip.then { ZipEntry(name, entry.resource, it) }
        }.toTypedArray()).then { array ->
            // addresses issue https://github.com/Stuk/jszip/issues/369# with date being UTC
            val now = Date().let { Date(it.getTime() - it.getTimezoneOffset() * 60000) }

            array.forEach { entry ->
                val fileOptions = object : JSZipFileOptions {}.apply {
                  date = entry.resource?.date ?: now
                  unixPermissions = entry.resource?.unixPermissions
                }
                rootDir.file(entry.name, entry.content, fileOptions)
            }
        }.then {
            // generate the zip
            val options = object : JSZipGeneratorOptions {}.apply {
                type = "blob"
                platform = "UNIX"
            }

            zip.generateAsync(options)
        }.then {
            // return as a pair
            REZip("$root.zip", it as Blob)
        }
    }

}