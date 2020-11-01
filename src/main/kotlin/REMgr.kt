import kotlinx.browser.document
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLImageElement
import kotlin.js.Promise

class REMgr(private val storage: Storage) {

    fun createRE(form: HTMLFormElement) : RackExtension {

        val re = RackExtension.fromForm(form)

        val margin = 10

        val audioSocket = storage.getAudioSocketImageResource()

        val centerX = re.getWidth(Panel.back) / 2
        val centerY = re.getHeight(Panel.back) / 2

        // Main in
        re.addREProperty(
            AudioStereoPair(
                left = AudioSocket("MainInLeft", AudioSocketType.input, centerX - margin - audioSocket.image.width, centerY - margin - audioSocket.image.height, audioSocket.name),
                right = AudioSocket("MainInRight", AudioSocketType.input, centerX + margin, centerY - margin - audioSocket.image.height, audioSocket.name)
            )
        )

        // Main out
        re.addREProperty(
            AudioStereoPair(
                left = AudioSocket("MainOutLeft", AudioSocketType.output, centerX - margin - audioSocket.image.width, centerY + margin, audioSocket.name),
                right = AudioSocket("MainOutRight", AudioSocketType.output, centerX + margin, centerY + margin, audioSocket.name)
            )
        )

        return re
    }

    /**
     * Render a preview */
    fun renderPreview(re: RackExtension, panel: Panel) {
        val preview = document.getElementById("re-preview")
        val img = with(document.createElement("img")) {
            this as HTMLImageElement
            id = "re-preview"
            src = re.renderPanel(panel, storage).toDataURL(type = "image/png")
            width = re.getWidth(panel) / GUI2D.lowResScalingFactor
            this
        }
        preview?.parentNode?.replaceChild(img, preview)
    }



    companion object {
        fun load() : Promise<REMgr> {
            return Storage.load().then { REMgr(it) }
        }
    }
}