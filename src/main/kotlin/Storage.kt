import org.w3c.dom.Image
import org.w3c.files.Blob
import kotlin.js.Date
import kotlin.js.Promise

/**
 * Maintains the information for each file in the zip archive. Will use permission and date to generate the
 * outcome archive. */
sealed class StorageResource(val path: String, val date: Date?, val unixPermissions: Int?)

/**
 * A file resource (text file) */
class FileResource(path: String, date: Date?, unixPermissions: Int?, val content: String) :
    StorageResource(path, date, unixPermissions)

/**
 * An image resource (both blob and image) */
class ImageResource(path: String, date: Date?, unixPermissions: Int?, val blob: Blob, val image: Image) :
    StorageResource(path, date, unixPermissions) {

    val key : String  get() = path.split("/").last().removeSuffix(".png")
}

interface ImageProvider {

    companion object {
        protected const val AUDIO_SOCKET_IMAGE = "images/BuiltIn/Cable_Attachment_Audio_01_1frames.png"
        protected const val PLACEHOLDER_IMAGE = "images/BuiltIn/Placeholder.png"
        protected const val TAPE_HORIZONTAL_IMAGE = "images/BuiltIn/Tape_Horizontal_1frames.png"
    }

    fun findImageResourceByPath(path: String): ImageResource?

    fun getAudioSocketImageResource() = findImageResourceByPath(AUDIO_SOCKET_IMAGE)!!
    fun getPlaceholderImageResource() = findImageResourceByPath(PLACEHOLDER_IMAGE)!!
    fun getTapeHorizontalImageResource() = findImageResourceByPath(TAPE_HORIZONTAL_IMAGE)!!
}

class Storage(val resources: Array<out StorageResource>) : ImageProvider {

    override fun findImageResourceByPath(path: String): ImageResource? =
        resources.find { it.path == path } as? ImageResource

    companion object {
        fun load(version: String): Promise<Storage> =
            fetchBlob("plugin-$version.zip").then { zipBlob ->
                val zip = JSZip()
                zip.loadAsync(zipBlob).then {
                    val promises = mutableListOf<Promise<StorageResource>>()
                    zip.forEach { path, file ->
                        // handle images
                        if (path.endsWith(".png")) {
                            promises.add(
                                file.async("blob").then { blob ->
                                    blob as Blob
                                    Image.asyncLoad(org.w3c.dom.url.URL.Companion.createObjectURL(blob)).then { image ->
                                        ImageResource(path, file.date, file.unixPermissions, blob, image)
                                    }
                                }.flatten()
                            )
                        } else {
                            if (!(path.startsWith("__MACOSX") ||
                                        path.startsWith(".idea") ||
                                        path.endsWith(".DS_Store"))
                            ) {
                                promises.add(
                                    file.async("string").then { content ->
                                        FileResource(path, file.date, file.unixPermissions, content.toString())
                                    }
                                )
                            }
                        }
                    }

                    Promise.all(promises.toTypedArray())
                }.flatten()
            }.flatten().then {
                Storage(it)
            }
    }
}
