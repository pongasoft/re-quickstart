/*
 * Copyright (c) 2020 pongasoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @author Yan Pujante
 */

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.createElement
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import org.w3c.dom.*
import org.w3c.dom.url.URL
import org.w3c.files.Blob

/**
 * Encapsulates a notification section where messages can be added */
class Notification(id: String? = null) {
    val element = id?.let { document.getElementById(it) }

    // add a text line
    private fun addTextLine(message: String, status: String? = null) {
        if (element != null) {
            val div = document.createElement("div")
            if (status != null)
                div.classList.add(status)
            div.appendChild(document.createTextNode(message))
            element.appendChild(div)
            // this makes sure that the last entry is visible if the notification has a scroll bar
            element.scrollTop = element.scrollHeight.toDouble()
        } else {
            println("[${status ?: "info"}] $message")
        }
    }

    // add an element (ex: span, link, etc...)
    private fun addElement(elt: Element, status: String? = null) {
        if (element != null) {
            val div = document.createElement("div")
            if (status != null)
                div.classList.add(status)
            div.appendChild(elt)
            element.appendChild(div)
            element.scrollTop = element.scrollHeight.toDouble()
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
 * Encapsulates the entries that the user fills out to customize the blank plugin */
abstract class OptionEntry(
    val name: String,
    val label: String? = null,
    val defaultValue: String? = null,
    val desc: String? = null,
    val disabled: Boolean? = null
) {
    abstract fun render(tag: HtmlBlockTag)
}

/**
 * Entries of type `input` (like text fields or checkboxes) */
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

/**
 * Entries of type `select` for drop down selection */
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
                id = entry.name
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
 * Extension function to handle `OptionEntry` */
fun TBODY.optionEntry(entry: OptionEntry): Unit = tr {
    td("name") { entry.label?.let { label { htmlFor = entry.name; +entry.label } } }
    td("control") { entry.render(this) }
    td("desc") { entry.desc?.let { +entry.desc } }
}

/**
 * Creates the html form for the page */
fun createHTML(entries: Iterator<OptionEntry>, classes: String? = null): HTMLElement {
    val form = document.create.form(method = FormMethod.post, classes = classes) {
        table {
            tbody {
                entries.forEach { optionEntry(it) }
            }
        }
    }

    return form
}

/**
 * All entries for the form */
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
 * Main method called when the page loads. */
fun init() {
    val reQuickStartFormID = document.findMetaContent("X-re-quickstart-form-id") ?: "re-quickstart-form"

    val pluginVersion = document.findMetaContent("X-re-quickstart-plugin-version") ?: "1.0.0"

    // we create the REMgr (which asynchronously loads the necessary resources, like images and the zip file)
    val reMgrPromise = REMgr.load(pluginVersion)

    // we generate the form
    document.replaceElement(reQuickStartFormID,
        createHTML(
            entries.iterator(),
            classes = document.findMetaContent("X-re-quickstart-form-class")
        )
    )

    // map of name -> HTMLInputElement from the form
    val inputElements = entries.filterIsInstance<OptionInputEntry>().associateBy({ it.name }) { entry ->
        document.getElementById(entry.name) as? HTMLInputElement
    }

    // map of name -> HTMLSelectElement from the form
    val selectElements = entries.filterIsInstance<OptionSelectEntry>().associateBy({ it.name }) { entry ->
        document.getElementById(entry.name) as? HTMLSelectElement
    }

    // sets the state of the submit button depending on whether all values have been filled out
    fun maybeEnableSubmit() {
        inputElements["submit"]?.disabled = inputElements.values.any { it?.value?.isEmpty() ?: false }
    }

    // create the notification area where messages will be displayed to the user (console style)
    val notification = Notification("notification")

    // print welcome message
    notification.info("### Rack Extension Plugin Generator Console [v$pluginVersion] ###")
    document.findMetaContent("X-re-quickstart-notification-welcome-message")?.let { message ->
        message.split('|').forEach { notification.info(it) }
    }

    // handles "Generate Blank Plugin" event
    inputElements["submit"]?.addListener("click") {

        reMgrPromise.then { reMgr ->

            // create the Rack Extension from the data in the form
            val re = reMgr.createRE(form!!)

            // function which generates the panel GUI preview and links (Step. 2)
            fun renderPreview(re: RackExtension, panel: Panel) {
                // the image itself
                document.replaceElement("re-preview-gui-content", reMgr.generatePreview(re, panel))

                // the links Front | Back | Folded Front | Folded Back (the current panel is not a link)
                document.replaceElement("re-preview-gui-links",
                    document.create.ul {
                        re.availablePanels.forEach { p ->
                            val panelName = p.toString().replace("_", " ").capitalize()
                            li(if(p == panel) "active" else null) {
                                if(p != panel) {
                                    a {
                                        onClickFunction = {
                                            renderPreview(re, p)
                                        }
                                        +panelName
                                    }
                                } else {
                                    +panelName
                                }
                            }
                        }
                    })
            }

            // we select the front panel
            renderPreview(re, Panel.front)

            val tree = reMgr.generateFileTree(re)

            // add links to preview all files included with the RE
            fun renderFilePreview(path: String) {
                // render the content
                tree[path]?.html?.invoke()?.let { content ->
                    document.replaceElement("re-preview-files-content", content)
                }

                // regenerate the list of links
                document.replaceElement("re-preview-files-links",
                    document.create.div {
                        ul {
                            tree.keys.sortedBy { it.toLowerCase() }.forEach { p ->
                                li(if(path == p) "active" else null) {
                                    if(p != path) {
                                        a {
                                            onClickFunction = {
                                                renderFilePreview(p)
                                            }
                                            +p
                                        }
                                    } else {
                                            +p
                                    }
                                }

                            }
                        }
                    }
                )
            }

            // we preview info.lua
            renderFilePreview("info.lua")

            // we reveal the rest of the page
            document.show("re-blank-plugin")

            // generate zip file and provide a link to download it (no automatic download)
            reMgr.generateZip("${re.info.productId}-plugin", tree).then { zip ->
                notification.info("Generated ${zip.filename}. Go to step 4 to download it.")
                val downloadAnchor = generateDownloadAnchor(zip.filename, zip.content)
                downloadAnchor.text = zip.filename
                document.replaceElement("re-download-link", downloadAnchor)
            }
        }.catch {
            println(it)
            notification.error("Error detected - ${it.message}. Try refreshing the page...")
        }

    }

    // infer a medium and short name when long name is provided
    inputElements["long_name"]?.onChange {
        inputElements["medium_name"]?.setComputedValue(value.substring(0..19))
        inputElements["short_name"]?.setComputedValue(value.substring(0..9))
    }

    // infer a product_id when manufacturer and short_name have been provided
    inputElements["manufacturer"]?.onChange {
        inputElements["short_name"]?.value?.let { shortName ->
            if (shortName.isNotEmpty()) {
                // applying RE SDK rule
                val regex = Regex("[^A-Za-z0-9._]")
                inputElements["product_id"]?.setComputedValue(
                    "com.${regex.replace(value, "").toLowerCase()}.${regex.replace(shortName, "")}"
                )
            }
        }
    }

    // hide Step.2+ if the form is changed
    inputElements.forEach { (name, elt) ->
        if (name != "submit")
            elt?.onChange {
                document.hide("re-blank-plugin")
                maybeEnableSubmit()
            }
    }

    // hide Step.2+ if the form is changed
    selectElements.forEach { (_, elt) ->
        elt?.addEventListener("change", {
            document.hide("re-blank-plugin")
        })
    }
}

// Entry point for javascript
fun main() {
    window.onload = { init() }
}
