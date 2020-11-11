import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import org.w3c.dom.*
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
 * Adds the `hidden` class to element which (via css) will hide it */
fun Element.hide() {
    addClass("hidden")
}

/**
 * Removes the `hidden` class to element which (via css) will show it */
fun Element.show() {
    this.removeClass("hidden")
}

/**
 * Shortcut to hide an element by its id */
fun Document.hide(id: String) = getElementById(id)?.hide()

/**
 * Shortcut to show an element by its id */
fun Document.show(id: String) = getElementById(id)?.show()

/**
 * Shortcut to click an element by its id */
fun Document.click(id: String) = (getElementById(id) as? HTMLElement)?.click()

/**
 * Shortcut to add a `click` listener to an element by its id */
fun Document.addClickListener(id: String, block: (event: Event) -> Unit) =
    getElementById(id)?.addEventListener("click", {event -> block(event)})

/**
 * Look for an element with the given id and replace it with the new element. If the new element does not have an
 * id, it will be set automatically to the old one */
fun Document.replaceElement(id: String, element: Element) {
    getElementById(id)?.let {
        if(element.id.isEmpty())
            element.id = id
        it.replaceWith(element)
    }
}

/**
 * Adding a listener where the element is passed back in the closure as "this" for convenience */
fun HTMLInputElement.addListener(type: String, block: HTMLInputElement.(event: Event) -> Unit) {
    addEventListener(type, { event -> block(event) })
}

/**
 * Shortcut for change event */
fun HTMLInputElement.onChange(block: HTMLInputElement.(event: Event) -> Unit) {
  addListener("change", block)
}

/**
 * Add a __computedValue field to the element to store the value that was computed so that when it gets
 * recomputed it can be updated but ONLY in the event the user has not manually modified it
 */
fun HTMLInputElement.setComputedValue(computedValue: String) {
  val dynElt: dynamic = this
  if (value.isEmpty() || value == dynElt.__computedValue)
    value = computedValue
  dynElt.__computedValue = computedValue
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
            resolve(image)
        })
    }
    image.src = src
    return promise
}

