enum class Panel { front, back, folded_front, folded_back }

interface IREProperty {
    fun motherboard(): String
    fun device2D(panel: Panel): String
    fun hdgui2D(panel: Panel): String
}

class REPropertyView(
    val panel: Panel,
    val offsetX: Int,
    val offsetY: Int,
    val image: String,
    val frames: Int = 1
) {
    fun nodeName(prop: REProperty): String = prop.name

    fun device2D(prop: REProperty): String {
        val f = if (frames > 1) ", frames=$frames " else ""
        return """
$panel["${nodeName(prop)}"] = {
  offset = { $offsetX, $offsetY },
  { path = "$image" $f}
}
"""
    }
}

abstract class REProperty(val name: String,
                          widgets: Iterable<REPropertyWidget>,
                          views: Iterable<REPropertyView>) : IREProperty {
    abstract val path: String

    private val _views = views.associateBy { it.panel }
    private val _widgets = widgets.associateBy { it.panel }

    override fun device2D(panel: Panel): String {
        return _views[panel]?.device2D(this) ?: ""
    }

    fun nodeName(panel: Panel) = _views[panel]?.nodeName(this)

    override fun hdgui2D(panel: Panel): String {
        return _widgets[panel]?.hdgui2D(this) ?: ""
    }
}

abstract class REPropertyWidget(val panel: Panel) {
    abstract fun hdgui2D(prop: REProperty): String
}

class REDeviceNameWidget(panel: Panel) : REPropertyWidget(panel) {
    override fun hdgui2D(prop: REProperty): String {
        return """
    -- device name / tape
    jbox.device_name {
      graphics = {
        node = "${prop.name}",
      },
    },
        """
    }
}
