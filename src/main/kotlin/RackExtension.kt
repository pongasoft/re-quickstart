import org.w3c.files.Blob
import kotlin.js.Promise

/**
 * Represents the rack extension */
class RackExtension(val sizeInU: Int = 1) {

    private val _gui2D: GUI2D = GUI2D(sizeInU)
    private val _reProperties: MutableCollection<IREProperty> = mutableListOf()

    /**
     * generateFrontPanelImgSrc */
    fun generatePanelImgSrc(panel: Panel) = _gui2D.generatePanelElement(panel).toDataURL(type = "image/png")

    fun getWidth(panel: Panel) = _gui2D.getWidth(panel)

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