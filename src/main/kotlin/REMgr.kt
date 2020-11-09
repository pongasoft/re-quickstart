import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.pre
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLImageElement
import org.w3c.files.Blob
import kotlin.js.Promise

class FileTreeEntry(val html: () -> HTMLElement, val zip: Promise<Any>, val resource: StorageResource? = null)

typealias FileTree = Map<String, FileTreeEntry>

class REMgr(private val storage: Storage) {

    companion object {
        fun load(): Promise<REMgr> {
            return Storage.load().then { REMgr(it) }
        }
    }

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
            re.getWidth() - img.image.width - GUI2D.emptyMargin,
            re.getHeight(Panel.back) - img.image.height - GUI2D.emptyMargin,
            img
        )
        re.addREProperty(prop)
    }

    /**
     * Generates the preview */
    fun generatePreview(re: RackExtension, panel: Panel) = with(document.createElement("img")) {
        this as HTMLImageElement
        src = re.generateFullPanel(panel).toDataURL(type = "image/png")
        width = re.getWidth() / GUI2D.lowResScalingFactor
        this
    }

    private fun generateResourceContent(re: RackExtension, resource: StorageResource) = when (resource) {
        is FileResource -> document.create.pre { +re.processContent(resource.content) }
        is ImageResource -> generateStaticImgContent(resource)
    }

    private fun generateStaticImgContent(imageResource: ImageResource) = with(document.createElement("img")) {
        this as HTMLImageElement
        src = imageResource.image.src
        width = imageResource.image.width / GUI2D.lowResScalingFactor
        this
    }

    private fun generatePanelImgContent(re: RackExtension, panel: Panel) = with(document.createElement("img")) {
        this as HTMLImageElement
        src = re.generatePanel(panel).toDataURL("image/png")
        width = re.getWidth() / GUI2D.lowResScalingFactor
        this
    }

    fun generateFileTree(re: RackExtension): FileTree {

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
                            html = { generateResourceContent(re, resource) },
                            zip = when(resource) {
                                is FileResource -> Promise.resolve(re.processContent(resource.content))
                                is ImageResource -> Promise.resolve(resource.blob)
                            }
                        )
                    )
                }

        fun genDynamicImagePair(panel: Panel): Pair<String, FileTreeEntry> {
            val name = "GUI2D/${re.getPanelImageKey(panel)}.png"
            return Pair(name,
                FileTreeEntry(
                    html = { generatePanelImgContent(re, panel) },
                    zip = re.generatePanel(panel).toNamedBlob(name).then { it.second }
                )
            )
        }

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
     * Generates the (promise) of the zip file
     * @return a (promise of a) pair where the first element is the name of the zip file and the second is the content */
    fun generateZip(root: String, tree: FileTree): Promise<Pair<String, Blob>> {
        val zip = JSZip()
        val rootDir = zip.folder(root)

        class ZipEntry(val name: String, val resource: StorageResource?, val content: Any)

        return Promise.all(tree.map { (name, entry) ->
            entry.zip.then { ZipEntry(name, entry.resource, it) }
        }.toTypedArray()).then { array ->
            array.forEach { entry ->
                val fileOptions = object : JSZipFileOptions {}.apply {
                  date = entry.resource?.date
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
            Pair("$root.zip", it as Blob)
        }
    }

}