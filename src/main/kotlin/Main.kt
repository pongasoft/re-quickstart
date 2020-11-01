import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.createElement
import kotlinx.html.*
import kotlinx.html.dom.create
import org.w3c.dom.Element
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob

/**
 * Encapsulates a notification section where messages can be added */
class Notification(id: String? = null) {
    val element = id?.let { document.getElementById(it) }

    private fun addTextLine(message: String, status: String? = null) {
        if (element != null) {
            val div = document.createElement("div")
            if (status != null)
                div.classList.add(status)
            div.appendChild(document.createTextNode(message))
            element.appendChild(div)
        } else {
            println("[${status ?: "info"}] $message")
        }
    }

    private fun addElement(elt: Element, status: String? = null) {
        if (element != null) {
            val div = document.createElement("div")
            if (status != null)
                div.classList.add(status)
            div.appendChild(elt)
            element.appendChild(div)
        }
    }

    fun info(message: String) {
        addTextLine(message)
    }

    fun info(elt: Element) {
        addElement(elt)
    }

    fun success(message: String) {
        addTextLine(message, "success")
    }

    fun error(message: String) {
        addTextLine(message, "error")
    }
}

/**
 * Encapsulates the entries that the user fills out to customize the blank plugin
 */
data class OptionEntry(
    val name: String,
    val label: String? = null,
    val type: InputType = InputType.text,
    val checked: Boolean? = null,
    val defaultValue: String? = null,
    val desc: String? = null,
    val maxLength: Int? = null,
    val disabled: Boolean? = null
)

/**
 * Extension function to handle `OptionEntry`
 */
fun TBODY.optionEntry(entry: OptionEntry): Unit = tr {
    td("name") { entry.label?.let { label { htmlFor = entry.name; +entry.label } } }
    td("control") {
        input(type = entry.type, name = entry.name) {
            id = entry.name

            entry.maxLength?.let { maxLength = entry.maxLength.toString() }

            if (entry.type == InputType.checkBox) {
                checked = entry.checked ?: true
            }

            if (entry.defaultValue != null) {
                value = entry.defaultValue
                +entry.defaultValue
            }

            if (entry.disabled != null) {
                disabled = entry.disabled
            }
        }
    }
    td("desc") { entry.desc?.let { +entry.desc } }
}

/**
 * Creates the html form for the page
 */
fun createHTML(entries: Iterator<OptionEntry>, elementId: String? = null, classes: String? = null): HTMLElement {
    val form = document.create.form(method = FormMethod.post, classes = classes) {
        elementId?.let { id = elementId }
        table {
            tbody {
                entries.forEach { optionEntry(it) }
            }
        }
    }

    return form
}

/**
 * All entries
 */
val entries =
    arrayOf(
        OptionEntry(
            name = "long_name",
            label = "Plugin Name (Long)",
            desc = "Long name / 40 characters max"
        ),
        OptionEntry(
            name = "medium_name",
            label = "Plugin Name (Medium)",
            desc = "Medium name / 20 characters max"
        ),
        OptionEntry(
            name = "short_name",
            label = "Plugin Name (Short)",
            desc = "Short name / 10 characters max"
        ),
        OptionEntry(
            name = "enable_vst2",
            type = InputType.checkBox,
            label = "Enable VST2",
            checked = false,
            desc = "Makes the plugin compatible with both VST2 and VST3"
        ),
        OptionEntry(
            name = "submit",
            type = InputType.button,
            defaultValue = "Generate blank plugin",
            disabled = false
        )
    )

/**
 * Generate the download link. */
fun generateDownloadAnchor(filename: String, blob: Blob): HTMLAnchorElement {
    return document.createElement("a") {
        this as HTMLAnchorElement
        href = URL.createObjectURL(blob)
        target = "_blank"
        download = filename
    } as HTMLAnchorElement
}

/**
 * Main method called when the page loads.
 */
fun init() {
    val reQuickStartFormID = document.findMetaContent("X-re-quickstart-form-id") ?: "re-quickstart-form"

    val reMgrPromise = REMgr.load()

    document.getElementById(reQuickStartFormID)
        ?.replaceWith(
            createHTML(
                entries.iterator(),
                elementId = reQuickStartFormID,
                classes = document.findMetaContent("X-jamba-form-class")
            )
        )

    val elements = entries.associateBy({ it.name }) { entry ->
        document.getElementById(entry.name) as? HTMLInputElement
    }

    val notification = Notification("notification")

    notification.info("Welcome!")

    elements["submit"]?.addListener("click") {

        reMgrPromise.then { reMgr ->

            val re = reMgr.createRE(form!!)

            notification.info("Click")

            notification.info("// motherboard")
            notification.info(re.motherboard())

            notification.info("**************************")
            notification.info("// device2D")
            notification.info(re.device2D())

            reMgr.renderPreview(re, Panel.back)

            Panel.values().forEach { panel ->
                (document.getElementById("re-preview-$panel") as? HTMLAnchorElement)?.addEventListener("click", {
                    reMgr.renderPreview(re, panel)
                })
            }
        }

//
//        re.generateZip().then { (name, blob) ->
//            notification.info("generated $name")
//            val downloadAnchor = generateDownloadAnchor(name, blob)
//            document.findMetaContent("X-re-quickstart-download-link")?.let {
//                downloadAnchor.text = it
//                notification.info(downloadAnchor)
//            }
////            downloadAnchor.click()
//        }
    }
}

fun main() {
    window.onload = { init() }
}
