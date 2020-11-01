import org.w3c.dom.Document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
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

