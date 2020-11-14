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

import org.w3c.dom.CanvasRenderingContext2D

/**
 * A Rack extension has 2 or 4 panels (only note players have only 2) */
enum class Panel {
    front,
    back,
    folded_front,
    folded_back;

    fun isFront() = this == front || this == folded_front
    fun isFolded() = this == folded_front || this == folded_back
}

/**
 * Base interface for a rack extension property */
interface IREProperty {
    /**
     * @return the definition of the property defined in the `motherboard_def.lua` file */
    fun motherboard(): String

    /**
     * Example:
     * ```
     *  audioOutputStereoPairLeft = {
     *     offset = { 0, 0 },
     *      { path = "Cable_Attachment_Audio_01_1frames" }
     *    },
     * ```
     *
     * @param panel the panel on which the graphical representation applies
     * @return the graphical representation (aka view) of the property as defined in the `device2D.lua` file.
     *         Returns empty string if there is no graphical representation for the given panel */
    fun device2D(panel: Panel): String

    /**
     * Example:
     * ```
     * jbox.audio_output_socket {
     *   graphics = {
     *     node = "audioOutputStereoPairLeft",
     *   },
     *   socket = "/audio_outputs/audioOutLeft",
     * },
     * ```
     * @param panel the panel on which the widget applies
     * @return the widget representing the property on a given panel as defined in `hdgui_2D.lua`.
     *         Returns empty string if there is no widget for the given panel */
    fun hdgui2D(panel: Panel): String

    /**
     * Render this property in the given panel (draws itself in the provided canvas) */
    fun render(panel: Panel, ctx: CanvasRenderingContext2D)

    /**
     * @return the list of image resources used by this property (in general one but a property can have different
     *         representations based on the panel it is being rendered on) */
    fun getImageResources() : List<ImageResource>

    /**
     * @return the list of input setup for `realtime_controller.lua` (properties that you want to be notified on when
     *         they change) */
    fun rtInputSetup(): List<String>

    /**
     * @return the map of text resources used by this property for `Resources/English/texts.lua` */
    fun textResources() : Map<String, String>
}

/**
 * Implements the [IREProperty] interface for an actual property represented by a name, and a list of view/widget
 * on which the property is rendered
 *
 * @param name the name of the property (should be unique)
 * @param widgets the list of widgets specifying on which panel this property is rendered
 * @param views the list of views specifying how the property looks on each panel */
abstract class REProperty(val name: String) : IREProperty {
    /**
     * The unique path to the property (ex: `/audio_outputs/audioOutLeft`) */
    open val path: String? = null

    /**
     * Map of panel -> widget */
    private val _widgets: MutableCollection<REPropertyWidget> = mutableListOf()

    /**
     * Add a widget for the given panel. In this version only `device_name` is used and the other properties
     * are audio sockets. This code can be expanded based on needs.
     */
    fun addWidget(
        panel: Panel,
        type: REPropertyWidget.Type,
        imageResource: ImageResource,
        offsetX: Int = 0,
        offsetY: Int = 0,
        frames: Int = 1
    ) = addWidget(
        when (type) {
            REPropertyWidget.Type.device_name -> REDeviceNameWidget(panel, this, offsetX, offsetY, imageResource)
        }
    )

    // getImageResources
    override fun getImageResources() = _widgets.map { it.imageResource }

    fun widgetCount(panel: Panel) = _widgets.count { it.panel == panel }

    /**
     * Adds a widget. Subclasses can use this method */
    protected fun addWidget(widget: REPropertyWidget) = _widgets.add(widget)

    // device2D
    override fun device2D(panel: Panel) =
        _widgets.filter { it.panel == panel }.joinToString(separator = "\n") { it.device2D() }

    // hdgui2D
    override fun hdgui2D(panel: Panel) =
        _widgets.filter { it.panel == panel }.joinToString(separator = "\n") { it.hdgui2D() }

    override fun rtInputSetup(): List<String> = emptyList()

    override fun textResources(): Map<String, String> = mapOf(Pair("$name ui_name", "TBD [$name ui_name]"))

    // render
    override fun render(panel: Panel, ctx: CanvasRenderingContext2D) {
        _widgets.filter { it.panel == panel }.forEach { it.render(ctx) }
    }
}

/**
 * Represents the widget for a property on a given panel */
abstract class REPropertyWidget(
    val panel: Panel,
    val prop: REProperty,
    val offsetX: Int,
    val offsetY: Int,
    val imageResource: ImageResource,
    val frames: Int = 1
) {
    enum class Type {
        device_name
    }

    protected val nodeName = if(prop.widgetCount(panel) == 0) prop.name else "${prop.name}_${prop.widgetCount(panel)}"

    /**
     * This implementation assumes that the `panel` map has been initialized already and simply add to it */
    fun device2D(): String {
        val f = if (frames > 1) ", frames=$frames " else ""
        val offset = if (offsetX != 0 || offsetY != 0) "offset = { $offsetX, $offsetY }, " else ""
        return """$panel["$nodeName"] = { $offset{ path = "${imageResource.key}" $f} }"""
    }

    fun render(ctx: CanvasRenderingContext2D) {
        val src = imageResource.image
        val w = src.width.toDouble()
        val h = src.height / frames.toDouble() // height (first frame)
        ctx.drawImage(
            src,
            0.toDouble(), // src x
            0.toDouble(), // src y
            w, // src width
            h, // src height (first frame)
            offsetX.toDouble(),
            offsetY.toDouble(),
            w,
            h
        )
    }

    /**
     * @return the widget for the property on the given panel */
    abstract fun hdgui2D(): String
}

