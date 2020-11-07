import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.pre
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLImageElement
import kotlin.js.Promise

class REMgr(private val storage: Storage) {

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
                re.addREProperty(
                    AudioStereoPair(
                        left = AudioSocket(
                            "MainInLeft",
                            AudioSocketType.input,
                            centerX - margin - audioSocket.image.width,
                            centerY - margin - audioSocket.image.height,
                            audioSocket.key
                        ),
                        right = AudioSocket(
                            "MainInRight",
                            AudioSocketType.input,
                            centerX + margin,
                            centerY - margin - audioSocket.image.height,
                            audioSocket.key
                        )
                    )
                )

                // Main out
                re.addREProperty(
                    AudioStereoPair(
                        left = AudioSocket(
                            "MainOutLeft",
                            AudioSocketType.output,
                            centerX - margin - audioSocket.image.width,
                            centerY + margin,
                            audioSocket.key
                        ),
                        right = AudioSocket(
                            "MainOutRight",
                            AudioSocketType.output,
                            centerX + margin,
                            centerY + margin,
                            audioSocket.key
                        )
                    )
                )
            }
            // Instrument : stereo out
            RackExtension.Type.instrument -> {
                // Main out
                re.addREProperty(
                    AudioStereoPair(
                        left = AudioSocket(
                            "MainOutLeft",
                            AudioSocketType.output,
                            centerX - margin - audioSocket.image.width,
                            centerY + margin - audioSocket.image.height / 2,
                            audioSocket.key
                        ),
                        right = AudioSocket(
                            "MainOutRight",
                            AudioSocketType.output,
                            centerX + margin,
                            centerY + margin - audioSocket.image.height / 2,
                            audioSocket.key
                        )
                    )
                )
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
        Panel.values().forEach { panel ->
            val (x, y) = re.getTopLeft(panel)
            prop.addWidget(panel, REPropertyWidget.Type.device_name, img.key, x + margin, y + margin)
        }
        re.addREProperty(prop)
    }

    /**
     * Adds the placeholder on the back panel */
    private fun addPlaceholderProperty(re: RackExtension) {
        val img = storage.getPlaceholderImageResource()
        val prop = REPlaceholderProperty(
            "Placeholder",
            re.getWidth() - img.image.width,
            re.getHeight(Panel.back) - img.image.height,
            img.key
        )
        re.addREProperty(prop)
    }

    /**
     * Generates the preview */
    fun generatePreview(re: RackExtension, panel: Panel) = with(document.createElement("img")) {
        this as HTMLImageElement
        src = re.generateFullPanel(panel, storage).toDataURL(type = "image/png")
        width = re.getWidth() / GUI2D.lowResScalingFactor
        this
    }

    private fun generateTextContent(content: String) = document.create.pre { +content }

    private fun generateResourceContent(re: RackExtension, resource: StorageResource) = when (resource) {
        is FileResource -> document.create.pre { +re.processContent(resource.content) }
        else -> document.create.pre { +"not implemented yet" }
    }

    private fun generateStaticImgContent(name: String): HTMLElement {
        val img = storage.findImageResource(name)?.image
        if (img != null)
            return with(document.createElement("img")) {
                this as HTMLImageElement
                src = img.src
                width = img.width / GUI2D.lowResScalingFactor
                this
            }
        else
            return generateTextContent("N/A")
    }

    private fun generatePanelImgContent(re: RackExtension, panel: Panel) = with(document.createElement("img")) {
        this as HTMLImageElement
        src = re.generatePanel(panel).toDataURL("image/png")
        width = re.getWidth() / GUI2D.lowResScalingFactor
        this
    }

    fun generateFileTree(re: RackExtension): Map<String, () -> HTMLElement> {

        // we use common files and type specific files (not used at the moment)
        val resources = storage.resources
            .filter { it is FileResource &&
                    !it.path.endsWith("/") &&
                    (it.path.startsWith("skeletons/common/") || it.path.startsWith("skeleton/${re.info.type}/")) }
            .map { resource ->
                Pair(resource.path.removePrefix("skeletons/common/"), { generateResourceContent(re, resource) })
            }

        return mapOf(
            Pair("info.lua", { generateTextContent(re.infoLua()) }),
            Pair("motherboard_def.lua", { generateTextContent(re.motherboardLua()) }),
            Pair("realtime_controller.lua", { generateTextContent(re.realtimeControllerLua()) }),
            Pair("Resources/English/texts.lua", { generateTextContent(re.textsLua()) }),
            Pair("GUI2D/device_2d.lua", { generateTextContent(re.device2DLua()) }),
            Pair("GUI2D/hdgui_2D.lua", { generateTextContent(re.hdgui2DLua()) }),
            *Panel.values().map {
                Pair("GUI2D/${re.getPanelImageName(it)}", { generatePanelImgContent(re, it) })
            }.toTypedArray(),
            *re.getPropertyImages().map {
                Pair("GUI2D/${it}.png", { generateStaticImgContent(it) })
            }.toTypedArray(),
            *resources.toTypedArray()
        )
    }

    companion object {
        fun load(): Promise<REMgr> {
            return Storage.load().then { REMgr(it) }
        }
    }
}