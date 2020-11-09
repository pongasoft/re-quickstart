import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.createElement
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.*
import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams
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
abstract class OptionEntry(
    val name: String,
    val label: String? = null,
    val defaultValue: String? = null,
    val desc: String? = null,
    val disabled: Boolean? = null
) {
    abstract fun render(tag: HtmlBlockTag)
}

class OptionInputEntry(
    name: String,
    label: String? = null,
    val type: InputType = InputType.text,
    val checked: Boolean? = null,
    defaultValue: String? = null,
    desc: String? = null,
    val maxLength: Int? = null,
    disabled: Boolean? = null
) : OptionEntry(name, label, defaultValue, desc, disabled) {

    override fun render(tag: HtmlBlockTag) {
        val entry = this
        with(tag) {
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
    }
}

class OptionSelectEntry(
    name: String,
    label: String? = null,
    val options: Collection<Pair<String, String>>? = null,
    defaultValue: String? = null,
    desc: String? = null,
    disabled: Boolean? = null
) : OptionEntry(name, label, defaultValue, desc, disabled) {

    override fun render(tag: HtmlBlockTag) {
        val entry = this
        with(tag) {
            select {
                name = entry.name

                entry.options?.forEach { o ->
                    option {
                        value = o.first
                        selected = o.first == entry.defaultValue
                        +o.second
                    }
                }
            }
        }
    }
}


/**
 * Extension function to handle `OptionEntry`
 */
fun TBODY.optionEntry(entry: OptionEntry): Unit = tr {
    td("name") { entry.label?.let { label { htmlFor = entry.name; +entry.label } } }
    td("control") { entry.render(this) }
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
        OptionInputEntry(
            name = "long_name",
            label = "Plugin Name (Long)",
            maxLength = 40,
            desc = "The name of the device (40 characters max). Shown in the device palette and Create menu in Reason. Also shown in the Shop. Must follow the conventions [product name] [short description] (ex: SubTractor Analog Synthesizer). See SDK documentation for more details.",
        ),
        OptionInputEntry(
            name = "manufacturer",
            label = "Manufacturer",
            desc = "The name of the manufacturer. Shown in the device palette in Reason."
        ),
        OptionInputEntry(
            name = "medium_name",
            label = "Plugin Name (Medium)",
            maxLength = 20,
            desc = "Shorter version of the device name (20 characters max). Used in situations where Reason \"talks\" about the device, e.g., \"Undo create Synthesizer\"."
        ),
        OptionInputEntry(
            name = "short_name",
            label = "Plugin Name (Short)",
            maxLength = 10,
            desc = "Short version of the device name (10 characters max). Used for auto-naming new instances of the device in Reason, e.g. \"Synth 1\", \"Synth 2\"."
        ),
        OptionInputEntry(
            name = "product_id",
            label = "Product identifier",
            desc = "Must be unique and follow the reverse domain notation convention, e.g., \"se.propellerheads.SimpleInstrument\". The only characters that are allowed are alphanumeric characters and the dot (.) and underscore (_) characters. The identifier is used in Reason Studios Rack Extension repository and databases. The identifier is never shown in the Shop. The identifier can not be changed once the product has been uploaded to the Reason Studios build server."
        ),
        OptionSelectEntry(
            name = "device_type",
            label = "Device Type",
            options = listOf(
                Pair("instrument", "Instrument"),
                Pair("creative_fx", "Creative FX"),
                Pair("studio_fx", "Studio FX"),
                Pair("helper", "Helper / Utility"),
                Pair("note_player", "Note Player")
            ),
            defaultValue = "studio_fx",
            desc = "Type of the device. Determines what kind of inputs/outputs are allowed, options for automatic routing and determines in which menu in the device palette that device is placed."
        ),
        OptionSelectEntry(
            name = "device_height_ru",
            label = "Device Size",
            options = (1..9).map { Pair("$it", "${it}U") },
            defaultValue = "1",
            desc = "Height of the device (in U)"
        ),
        OptionInputEntry(
            name = "submit",
            type = InputType.button,
            defaultValue = "Generate blank plugin",
            disabled = true
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

    val pluginVersion = document.findMetaContent("X-re-quickstart-plugin-version") ?: "1.0.0"
    val reMgrPromise = REMgr.load(pluginVersion)

    document.getElementById(reQuickStartFormID)
        ?.replaceWith(
            createHTML(
                entries.iterator(),
                elementId = reQuickStartFormID,
                classes = document.findMetaContent("X-re-quickstart-form-class")
            )
        )

    val elements = entries.filterIsInstance<OptionInputEntry>().associateBy({ it.name }) { entry ->
        document.getElementById(entry.name) as? HTMLInputElement
    }

    fun maybeEnableSubmit() {
        elements["submit"]?.disabled = elements.values.any { it?.value?.isEmpty() ?: false }
    }

    val notification = Notification("notification")

    notification.info("### Rack Extension Plugin Generator Output [v$pluginVersion] ###")

    document.findMetaContent("X-re-quickstart-notification-welcome-message")?.let { message ->
      message.split('|').forEach { notification.info(it) }
    }

    elements["submit"]?.addListener("click") {

        reMgrPromise.then { reMgr ->

            val re = reMgr.createRE(form!!)

            notification.info("Click")

            fun renderPreview(re: RackExtension, panel: Panel) {
                val preview = reMgr.generatePreview(re, panel)
                preview.id = "re-preview"
                document.getElementById("re-preview")?.replaceWith(preview)
            }

            // render the front panel
            renderPreview(re, Panel.front)

            document.getElementById("re-preview-links")?.replaceWith(
                document.create.div {
                    id = "re-preview-links"
                    re.availablePanels.forEach { panel ->
                        a {
                            onClickFunction = {
                                renderPreview(re, panel)
                            }
                            + panel.toString().capitalize()
                        }
                        + " | "
                    }
                })

            // add links to render all panels
            re.availablePanels.forEach { panel ->
                (document.getElementById("re-preview-$panel") as? HTMLAnchorElement)?.addEventListener("click", {
                    renderPreview(re, panel)
                })
            }

            val tree = reMgr.generateFileTree(re)

            // add links to preview all files included with the RE
            document.getElementById("re-files-preview-links")?.replaceWith(
                document.create.div {
                    id = "re-files-preview-links"
                    ul {
                        tree.keys.sortedBy { it.toLowerCase() }.forEach { path ->
                            li {
                                a {
                                    id = "preview-action-${path}"
                                    onClickFunction = {
                                        val content = tree[path]?.html?.invoke()!! // keys are from the map
                                        content.id = "re-files-preview-content"
                                        document.getElementById("re-files-preview-content")?.replaceWith(content)
                                    }
                                    +path
                                }
                            }

                        }
                    }
                }
            )

            // preview info
            (document.getElementById("preview-action-info.lua") as? HTMLAnchorElement)?.click()

            // generate zip file
            reMgr.generateZip("${re.info.productId}-plugin", tree).then { (name, blob) ->
                notification.info("generated $name")
                val downloadAnchor = generateDownloadAnchor(name, blob)
                document.findMetaContent("X-re-quickstart-download-link")?.let {
                    downloadAnchor.text = it
                    notification.info(downloadAnchor)
                }
//            downloadAnchor.click()
            }
        }
    }

    elements["long_name"]?.onChange {
        elements["medium_name"]?.setComputedValue(value.substring(0..19))
        elements["short_name"]?.setComputedValue(value.substring(0..9))
    }

    elements["manufacturer"]?.onChange {
        elements["short_name"]?.value?.let { shortName ->
            if(shortName.isNotEmpty())
            {
                val regex = Regex("[^A-Za-z0-9._]")
                elements["product_id"]?.setComputedValue("com.${regex.replace(value, "").toLowerCase()}.${regex.replace(shortName, "")}")
            }
        }
    }

    elements.forEach { (name, elt) ->
        if(name != "submit")
            elt?.onChange { maybeEnableSubmit() }
    }
}

fun main() {
    window.onload = { init() }
}
