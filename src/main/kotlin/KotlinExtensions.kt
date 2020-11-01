import kotlinx.browser.window
import org.w3c.dom.Document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Image
import org.w3c.dom.events.Event
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import org.w3c.files.Blob
import org.w3c.xhr.FormData
import kotlin.js.Promise

/**
 * Finds (may not exist) the content of a meta entry whose name is provided */
fun Document.findMetaContent(name: String): String? {
    return querySelector("meta[name='$name']")?.getAttribute("content")
}

/**
 * Adding a listener where the element is passed back in the closure as "this" for convenience */
fun HTMLInputElement.addListener(type: String, block: HTMLInputElement.(event: Event) -> Unit) {
    addEventListener(type, { event -> block(event) })
}

/*
 * Defines the api used by js iterators (which can be used in for..of construct) */
external interface JSIteratorNextResult<T> {
    val done: Boolean?
    val value: T
}

/**
 * Defines the api used by js iterators (which can be used in for..of construct) */
external interface JSIterator<T> {
    fun next(): JSIteratorNextResult<T>
}

/**
 * This wraps a javascript [iterator](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Iteration_protocols)
 * into a Kotlin [Iterator]
 */
fun <T> jsIterator(jsObject: JSIterator<T>): Iterator<T> {
    return object : AbstractIterator<T>() {
        override fun computeNext() {
            val n = jsObject.next()
            if (n.done == null || n.done == false)
                setNext(n.value)
            else
                done()
        }
    }
}

/**
 * Extension function to [FormData] to add a `keys` api
 */
fun FormData.keys(): Iterator<String> = jsIterator(this.asDynamic().keys())

/**
 * Extension function that converts the `toBlob` callback API into a `Promise` API. */
fun HTMLCanvasElement.toNamedBlob(name: String): Promise<Pair<String, Blob>> {
    return Promise { resolve, reject ->
        toBlob(_callback = { blob: Blob? ->
            if (blob != null)
                resolve(Pair(name, blob))
            else
                reject(Exception("Cannot generate blob"))
        })
    }
}

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

/**
 * Extension for Image.asyncLoad */
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
