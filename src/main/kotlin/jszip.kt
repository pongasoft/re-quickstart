@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION", "NESTED_CLASS_IN_EXTERNAL_INTERFACE")

import kotlin.js.*
import kotlin.js.Json
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*

external interface `T$0` {
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun `T$0`.get(key: String): JSZipObject? = asDynamic()[key]

@Suppress("NOTHING_TO_INLINE")
inline operator fun `T$0`.set(key: String, value: JSZipObject) {
    asDynamic()[key] = value
}

//@JsModule("jszip")
external interface JSZip {
    var files: `T$0`
    fun file(path: String): JSZipObject
    fun file(path: RegExp): Array<JSZipObject>
    fun file(path: String, data: Any, options: JSZipFileOptions? = definedExternally /* null */): JSZip
    fun folder(name: String): JSZip
    fun folder(name: RegExp): Array<JSZipObject>
    fun forEach(callback: (relativePath: String, file: JSZipObject) -> Unit)
    fun filter(predicate: (relativePath: String, file: JSZipObject) -> Boolean): Array<JSZipObject>
    fun remove(path: String): JSZip
    fun generate(options: JSZipGeneratorOptions? = definedExternally /* null */): Any
    fun generateAsync(options: JSZipGeneratorOptions? = definedExternally /* null */, onUpdate: Function<*>? = definedExternally /* null */): Promise<Any>
    fun load()
    fun loadAsync(data: Any, options: JSZipLoadOptions? = definedExternally /* null */): Promise<JSZip>
    companion object {
        var prototype: JSZip = definedExternally
        var support: JSZipSupport = definedExternally
    }
}

inline operator fun JSZip.Companion.invoke(): JSZip = js("JSZip()")

external interface JSZipObject {
    var name: String
    var dir: Boolean
    var date: Date
    var comment: String
    var options: JSZipObjectOptions
    var unixPermissions: Int?
    var dosPermissions: Int?
    fun async(type: String /* "string" */, onUpdate: Function<*>? = definedExternally /* null */): Promise<Any>
    fun asText()
    fun asBinary()
    fun asArrayBuffer()
    fun asUint8Array()
}
external interface JSZipFileOptions {
    var base64: Boolean? get() = definedExternally; set(value) = definedExternally
    var binary: Boolean? get() = definedExternally; set(value) = definedExternally
    var date: Date? get() = definedExternally; set(value) = definedExternally
    var compression: String? get() = definedExternally; set(value) = definedExternally
    var comment: String? get() = definedExternally; set(value) = definedExternally
    var optimizedBinaryString: Boolean? get() = definedExternally; set(value) = definedExternally
    var createFolders: Boolean? get() = definedExternally; set(value) = definedExternally
    var unixPermissions: Int? get() = definedExternally; set(value) = definedExternally
    var dosPermissions: Int? get() = definedExternally; set(value) = definedExternally
}

external interface JSZipObjectOptions {
    var base64: Boolean
    var binary: Boolean
    var dir: Boolean
    var date: Date
    var compression: String
    var unixPermissions: Int?
    var dosPermissions: Int?
}
external interface JSZipGeneratorOptions {
    var base64: Boolean? get() = definedExternally; set(value) = definedExternally
    var compression: String? get() = definedExternally; set(value) = definedExternally
    var type: String? get() = definedExternally; set(value) = definedExternally
    var comment: String? get() = definedExternally; set(value) = definedExternally
    var platform: String? get() = definedExternally; set(value) = definedExternally
}
external interface JSZipLoadOptions {
    var base64: Boolean? get() = definedExternally; set(value) = definedExternally
    var checkCRC32: Boolean? get() = definedExternally; set(value) = definedExternally
    var optimizedBinaryString: Boolean? get() = definedExternally; set(value) = definedExternally
    var createFolders: Boolean? get() = definedExternally; set(value) = definedExternally
}
external interface JSZipSupport {
    var arraybuffer: Boolean
    var uint8array: Boolean
    var blob: Boolean
    var nodebuffer: Boolean
}
