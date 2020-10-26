import org.w3c.files.Blob
import kotlin.js.Promise

/**
 * Represents the rack extension */
class RackExtension(val sizeInU : Int = 1) {

    private val gui2D : GUI2D = GUI2D(sizeInU)

    /**
     * generateFrontPanelImgSrc */
    fun generateFrontPanelImgSrc() = gui2D.generateFrontPanelElement().toDataURL(type = "image/png")

    /**
     * frontPanelImgWidth */
    val frontPanelImgWidth get() = gui2D.width

    /**
     * Generates the (promise) of the zip file
     * @return a (promise of a) pair where the first element is the name of the zip file and the second is the content */
    fun generateZip(): Promise<Pair<String, Blob>> {

        val root = "plugin"

        val zip = JSZip()
        val rootDir = zip.folder(root)

        return Promise.all(gui2D.generateBackgroundBlobs()).then { array ->
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