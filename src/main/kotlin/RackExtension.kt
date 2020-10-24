import org.w3c.files.Blob
import kotlin.js.Promise

class RackExtension(val sizeInU : Int = 1) {
    fun generate(): Promise<Pair<String, Blob>> {

        val root = "plugin"

        val zip = JSZip()
        val rootDir = zip.folder(root)

        val gui2D = GUI2D(sizeInU)

        return Promise.all(gui2D.generateBackgroundBlobs()).then { array ->
            array.forEach { (name, blob) ->
                rootDir.file("GUI2D/$name", blob)
            }
        }.then {
            val options = object : JSZipGeneratorOptions {}.apply {
                type = "blob"
                platform = "UNIX"
            }

            zip.generateAsync(options)
        }.then {
            Pair("$root.zip", it as Blob)
        }
    }
}