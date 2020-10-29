import kotlinx.browser.window
import org.w3c.dom.Image
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import org.w3c.files.Blob
import kotlin.js.Promise

/**
 * Used in promise rejection when detecting error (status code != 200)
 */
open class HTTPException(val status: Short, val errorMessage: String) : Exception("[$status] $errorMessage") {
  constructor(response: Response) : this(response.status, response.statusText)
}

/**
 * Forces flattening the promise because Kotlin doesn't do it automatically
 */
inline fun <T> Promise<Promise<T>>.flatten(): Promise<T> {
  return this.then { it }
}

/**
 * Fetches the URL and processes the response (only when successful) via [onFulfilled]. If not successful or
 * rejection, then an exception is thrown (should be handled via [Promise.catch])
 */
fun <T> fetchURL(
    url: String,
    method: String = "GET",
    onFulfilled: (Response) -> Promise<T>
): Promise<T> {

  return window.fetch(
      url,
      RequestInit(method = method)
  )
      .then(
          onFulfilled = { response ->
            if (response.ok && response.status == 200.toShort()) {
              onFulfilled(response)
            } else {
              Promise.reject(HTTPException(response))
            }
          }
      ).flatten()
}

/**
 * Fetches the url as a blob
 */
fun fetchBlob(url: String, method: String = "GET"): Promise<Blob> {
  return fetchURL(url, method) { it.blob() }
}

class ImageResource(val blob: Blob, val image: Image)

fun fetchImageResource(url: String) : Promise<ImageResource> {
    return fetchBlob(url).then { blob ->
        Image.asyncLoad(org.w3c.dom.url.URL.Companion.createObjectURL(blob)).then { image ->
            ImageResource(blob, image)
        }
    }.flatten()
}

fun Image.Companion.asyncLoad(src: String) : Promise<Image> {
    val image = Image()
    val promise = Promise<Image> { resolve, reject ->
        image.addEventListener("error", {
            console.log("Error loading image [$src]")
            reject(Exception("Error loading image [$src]"))
        })
        image.addEventListener("load", {
            console.log("Image loaded [$src]")
            resolve(image)
        })
    }
    image.src = src
    return promise
}

class Storage(val images: Map<String, ImageResource>) {

    fun findImageResource(name: String) : ImageResource? = images[name]

    companion object {
        fun load() : Promise<Storage> {
            return Promise.all(arrayOf("Cable_Attachment_Audio_01_1frames", "Placeholder", "Tape_Horizontal_1frames").map { name ->
                fetchImageResource("images/BuiltIn/$name.png").then { ir -> Pair(name, ir)}
            }.toTypedArray()).then {
                Storage(mapOf(*it))
            }
        }
    }
}