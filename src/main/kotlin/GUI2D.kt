import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.files.Blob
import kotlin.js.Promise

fun HTMLCanvasElement.toNamedBlob(name: String) : Promise<Pair<String, Blob>> {
    return Promise { resolve, reject ->
        toBlob(_callback = { blob : Blob? ->
            if(blob != null)
                resolve(Pair(name, blob))
            else
                reject(Exception("Cannot generate blob"))
        })
    }
}

class GUI2D(val sizeInU : Int = 1) {
    private val hiResWidth = 3770
    private val hiResHeight1U = 345

    fun generateFrontPanelElement() = with(document.createElement("canvas")) { this as HTMLCanvasElement
        width = hiResWidth
        height = hiResHeight1U * sizeInU
        this
    }

    fun generateBackgroundBlobs() : Array<Promise<Pair<String, Blob>>> {
        return arrayOf(generateFrontPanelElement().toNamedBlob("front.png"))
    }

    fun generateFrontPanel() = generateFrontPanelElement().toDataURL(type = "image/png")
}