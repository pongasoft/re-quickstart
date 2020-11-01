import org.w3c.dom.Image
import org.w3c.files.Blob
import kotlin.js.Promise

val AUDIO_SOCKET_IMAGE = "Cable_Attachment_Audio_01_1frames"

class ImageResource(val name: String, val blob: Blob, val image: Image)

fun fetchImageResource(name: String, url: String): Promise<ImageResource> {
    return fetchBlob(url).then { blob ->
        Image.asyncLoad(org.w3c.dom.url.URL.Companion.createObjectURL(blob)).then { image ->
            ImageResource(name, blob, image)
        }
    }.flatten()
}

interface ImageProvider {
    fun findImageResource(name: String): ImageResource?

    fun getAudioSocketImageResource() = findImageResource(AUDIO_SOCKET_IMAGE)!! // return N/A image if not found instead
}

class Storage(val images: Map<String, ImageResource>) : ImageProvider {

    override fun findImageResource(name: String): ImageResource? = images[name]

    companion object {
        fun load(): Promise<Storage> {
            return Promise.all(arrayOf(AUDIO_SOCKET_IMAGE, "Placeholder", "Tape_Horizontal_1frames").map { name ->
                fetchImageResource(name, "images/BuiltIn/$name.png").then { ir -> Pair(name, ir) }
            }.toTypedArray()).then {
                Storage(mapOf(*it))
            }
        }
    }
}