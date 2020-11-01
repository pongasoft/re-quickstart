import org.w3c.dom.CanvasRenderingContext2D

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

    fun render(panel: Panel, ctx: CanvasRenderingContext2D, imageProvider: ImageProvider)
}

/**
 * Implements the [IREProperty] interface for an actual property represented by a name, and a list of view/widget
 * on which the property is rendered
 *
 * @param name the name of the property (should be unique)
 * @param widgets the list of widgets specifying on which panel this property is rendered
 * @param views the list of views specifying how the property looks on each panel */
abstract class REProperty(val name: String,
                          widgets: Iterable<REPropertyWidget>,
                          views: Iterable<REPropertyView>) : IREProperty {
    /**
     * The unique path to the property (ex: `/audio_outputs/audioOutLeft`) */
    abstract val path: String

    /**
     * Map of panel -> view */
    private val _views = views.associateBy { it.panel }

    /**
     * Map of panel -> widget */
    private val _widgets = widgets.associateBy { it.panel }

    // device2D
    override fun device2D(panel: Panel): String {
        return _views[panel]?.device2D(this) ?: ""
    }

    // hdgui2D
    override fun hdgui2D(panel: Panel): String {
        return _widgets[panel]?.hdgui2D(this) ?: ""
    }

    override fun render(panel: Panel, ctx: CanvasRenderingContext2D, imageProvider: ImageProvider) {
        _views[panel]?.render(ctx, imageProvider)
    }

    /**
     * The name of the node in `device2D.lua` is arbitrary but used by the widget in `hdgui2D.lua` for a given
     * panel. This method makes sure that both names are in sync. */
    fun nodeName(panel: Panel) =
        if(_views[panel] != null && _widgets[panel] != null)
            name
        else
            "N/A | $name not available for this panel $panel"
}

/**
 * Represents the view of a property in a given panel (`device2D.lua`) */
class REPropertyView(
    val panel: Panel,
    val offsetX: Int,
    val offsetY: Int,
    val image: String,
    val frames: Int = 1
) {
    /**
     * This implementation assumes that the `panel` map has been initialized already and simply add to it */
    fun device2D(prop: REProperty): String {
        val f = if (frames > 1) ", frames=$frames " else ""
        return """
$panel["${prop.nodeName(panel)}"] = {
  offset = { $offsetX, $offsetY },
  { path = "$image" $f}
}
"""
    }

    fun render(ctx: CanvasRenderingContext2D, imageProvider: ImageProvider) {
        console.log("rendering $image")
        imageProvider.findImage(image)?.let {
            val src = it
            val w = src.width.toDouble()
            val h = src.height / frames.toDouble() // height (first frame)
            console.log("rendering / 0,0 ${w}x$h $offsetX, $offsetY ")
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
    }
}

/**
 * Represents the widget for a property on a given panel */
abstract class REPropertyWidget(val panel: Panel) {
    /**
     * @return the widget for the property on the given panel */
    abstract fun hdgui2D(prop: REProperty): String
}

