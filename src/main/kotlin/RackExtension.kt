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
            val params = data.keys().asSequence().associateBy({it}, { e -> data.get(e).toString() })

            return RackExtension(
                Info(
                    type = Type.studio_fx,
                    sizeInU = params["sizeInU"]?.toInt() ?: 1)
            )
        }
    }

    enum class Type { studio_fx }

    class Info(val type: Type,
               val sizeInU: Int = 1)

    private val _gui2D: GUI2D = GUI2D(info)
    private val _reProperties: MutableCollection<IREProperty> = mutableListOf()

    /**
     * generateFrontPanelImgSrc */
    fun generatePanelImgSrc(panel: Panel) = _gui2D.generatePanelElement(panel).toDataURL(type = "image/png")

    fun renderPanel(panel: Panel, storage: Storage): HTMLCanvasElement {
        val canvas = _gui2D.generatePanelElement(panel)
        with(canvas.getContext("2d")) {
            this as CanvasRenderingContext2D
            _reProperties.forEach { prop -> prop.render(panel, this, storage) }
        }
        return canvas
    }

    fun getWidth(panel: Panel) = _gui2D.getWidth(panel)
    fun getHeight(panel: Panel) = _gui2D.getHeight(panel)

    fun addREProperty(prop: IREProperty) = _reProperties.add(prop)

    fun motherboard() : String {
        return """
format_version = "1.0"
            
audio_outputs = {}
audio_inputs = {}
${_reProperties.map { it.motherboard() }.joinToString(separator = "\n")}
"""
    }

    fun device2D() : String {
        val content = Panel.values().map { panel ->
            """
----
-- $panel
----
$panel = {}
${_reProperties.map { it.device2D(panel) }.joinToString(separator = "\n")}
"""
        }.joinToString(separator = "\n")

        return """
format_version = "2.0"
            
$content
"""
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
}