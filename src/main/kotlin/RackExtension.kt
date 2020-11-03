import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLFormElement
import org.w3c.files.Blob
import org.w3c.xhr.FormData
import kotlin.js.Promise

/**
 * Represents the rack extension */
class RackExtension(val info: Info) {


    companion object {
        fun fromForm(form: HTMLFormElement): RackExtension {
            val data = FormData(form)
            val params = data.keys().asSequence().associateBy({ it }, { e -> data.get(e).toString() })

            return RackExtension(
                Info(
                    longName = params["long_name"] ?: "Blank Plugin",
                    mediumName = params["medium_name"] ?: "Blank Plugin",
                    shortName = params["short_name"] ?: "Blank",
                    manufacturer = params["manufacturer"] ?: "acme",
                    productId = params["product_id"] ?: "com.acme.Blank",
                    version = params["version"] ?: "1.0.0d1",
                    type = Type.valueOf(params["device_type"] ?: Type.studio_fx.toString()),
                    sizeInU = params["device_height_ru"]?.toInt() ?: 1
                )
            )
        }
    }

    enum class Type { instrument, creative_fx, studio_fx, helper, note_player }

    class Info(
        val longName: String,
        val mediumName: String,
        val shortName: String,
        val manufacturer: String,
        val productId: String,
        val version: String,
        val type: Type,
        val sizeInU: Int = 1
    )

    private val _gui2D: GUI2D = GUI2D(info)
    private val _reProperties: MutableCollection<IREProperty> = mutableListOf()

    /**
     * generateFrontPanelImgSrc */
    fun generatePanelImgSrc(panel: Panel) = _gui2D.generatePanelElement(panel).toDataURL(type = "image/png")

    fun renderPanel(panel: Panel, imageProvider: ImageProvider): HTMLCanvasElement {
        val canvas = _gui2D.generatePanelElement(panel)
        with(canvas.getContext("2d")) {
            this as CanvasRenderingContext2D
            _reProperties.forEach { prop -> prop.render(panel, this, imageProvider) }
        }
        return canvas
    }

    fun getWidth(panel: Panel) = _gui2D.getWidth()
    fun getHeight(panel: Panel) = _gui2D.getHeight(panel)

    fun getTopLeft(panel: Panel) = when (panel) {
        Panel.front, Panel.folded_front -> Pair(GUI2D.emptyMargin, GUI2D.emptyMargin)
        Panel.back, Panel.folded_back -> Pair(GUI2D.emptyMargin + GUI2D.hiResRailWidth, GUI2D.emptyMargin)
    }

    fun addREProperty(prop: IREProperty) = _reProperties.add(prop)

    fun motherboard(): String {
        return """
format_version = "1.0"
            
audio_outputs = {}
audio_inputs = {}

${_reProperties.map { it.motherboard() }.filter { it != "" }.joinToString(separator = "\n\n")}
"""
    }

    fun device2D(): String {
        val content = Panel.values().map { panel ->
            """
--------------------------------------------------------------------------
-- $panel
--------------------------------------------------------------------------
$panel = {}

-- Background graphic
$panel["${panelNodeName(panel)}"] = "${_gui2D.getPanelImageName(panel).removeSuffix(".png")}"

${_reProperties.map { it.device2D(panel) }.filter { it != "" }.joinToString(separator = "\n")}"""
        }.joinToString(separator = "\n")

        return """
format_version = "2.0"
            
$content"""
    }

    fun hdgui2D(): String {
        val content = Panel.values().map { panel ->
            """
--------------------------------------------------------------------------
-- $panel
--------------------------------------------------------------------------
${panel}_widgets = {}

${_reProperties.map { it.hdgui2D(panel) }.filter { it != "" }.joinToString(separator = "\n\n")}

$panel = jbox.panel{
  graphics = {
    node = "${panelNodeName(panel)}",
  },
  widgets = ${panel}_widgets
}"""
        }.joinToString(separator = "\n")

        return """
format_version = "2.0"
            
$content
"""
    }

    fun generateFileTree(): Map<String, String> {
        return mapOf(
            Pair("motherboard_def.lua", motherboard()),
            Pair("GUI2D/device_2d.lua", device2D()),
            Pair("GUI2D/hdgui_2D.lua", hdgui2D()),
        )
    }

    /**
     * Generates the (promise) of the zip file
     * @return a (promise of a) pair where the first element is the name of the zip file and the second is the content */
    fun generateZip(): Promise<Pair<String, Blob>> {

        val root = "plugin"

        val zip = JSZip()
        val rootDir = zip.folder(root)

        return Promise.all(_gui2D.generateBackgroundBlobs()).then { array ->
            // add the background images (async generation)
            array.forEach { (name, blob) ->
                rootDir.file("GUI2D/$name", blob)
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

    private fun panelNodeName(panel: Panel) = "Panel_${panel}_Bg"
}