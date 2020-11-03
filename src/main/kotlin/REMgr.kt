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
                            audioSocket.name
                        ),
                        right = AudioSocket(
                            "MainInRight",
                            AudioSocketType.input,
                            centerX + margin,
                            centerY - margin - audioSocket.image.height,
                            audioSocket.name
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
                            audioSocket.name
                        ),
                        right = AudioSocket(
                            "MainOutRight",
                            AudioSocketType.output,
                            centerX + margin,
                            centerY + margin,
                            audioSocket.name
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
                            audioSocket.name
                        ),
                        right = AudioSocket(
                            "MainOutRight",
                            AudioSocketType.output,
                            centerX + margin,
                            centerY + margin - audioSocket.image.height / 2,
                            audioSocket.name
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
            prop.addWidget(panel, REPropertyWidget.Type.device_name, img.name, x + margin, y + margin)
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
            img.name
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
        return mapOf(
            Pair("motherboard_def.lua", { generateTextContent(re.motherboard()) }),
            Pair("GUI2D/device_2d.lua", { generateTextContent(re.device2D()) }),
            Pair("GUI2D/hdgui_2D.lua", { generateTextContent(re.hdgui2D()) }),
            *Panel.values().map {
                Pair("GUI2D/${re.getPanelImageName(it)}", { generatePanelImgContent(re, it) }) }.toTypedArray(),
            *re.getPropertyImages().map {
                Pair("GUI2D/${it}.png", {generateStaticImgContent(it)}) }.toTypedArray()
        )
    }

    companion object {
        fun load(): Promise<REMgr> {
            return Storage.load().then { REMgr(it) }
        }
    }
}