import org.w3c.dom.Image
import org.w3c.files.Blob
import kotlin.js.Promise

class ImageResource(val name: String, val blob: Blob, val image: Image) {
    companion object {
        fun asyncFetch(name: String, url: String): Promise<ImageResource> {
            return fetchBlob(url).then { blob ->
                Image.asyncLoad(org.w3c.dom.url.URL.Companion.createObjectURL(blob)).then { image ->
                    ImageResource(name, blob, image)
                }
            }.flatten()
        }
    }
}

interface ImageProvider {

    companion object {
        protected val AUDIO_SOCKET_IMAGE = "Cable_Attachment_Audio_01_1frames"
        protected val PLACEHOLDER_IMAGE = "Placeholder"
        protected val TAPE_HORIZONTAL_IMAGE = "Tape_Horizontal_1frames"
    }

    fun findImageResource(name: String): ImageResource?

    fun getAudioSocketImageResource() = findImageResource(AUDIO_SOCKET_IMAGE)!! // return N/A image if not found instead
    fun getPlaceholderImageResource() = findImageResource(PLACEHOLDER_IMAGE)!! // return N/A image if not found instead
    fun getTapeHorizontalImageResource() = findImageResource(TAPE_HORIZONTAL_IMAGE)!! // return N/A image if not found instead
}

class Storage(val images: Map<String, ImageResource>) : ImageProvider {

    override fun findImageResource(name: String): ImageResource? = images[name]

    companion object {
        fun load(): Promise<Storage> {
            return Promise.all(arrayOf(ImageProvider.AUDIO_SOCKET_IMAGE, ImageProvider.PLACEHOLDER_IMAGE, ImageProvider.TAPE_HORIZONTAL_IMAGE).map { name ->
                ImageResource.asyncFetch(name, "images/BuiltIn/$name.png").then { ir -> Pair(name, ir) }
            }.toTypedArray()).then {
                Storage(mapOf(*it))
            }
        }
    }
}