/**
 * A built in property is provided by Reason and as result does not have a representation in the motherboard */
open class REBuiltInProperty(name: String) : REProperty(name) {
    /**
     * @return the definition of the property defined in the `motherboard_def.lua` file */
    override fun motherboard(): String = ""

    override fun textResources() = emptyMap<String, String>()
}

/**
 * Device name widget (built-in) represented by a tape */
class REDeviceNameWidget(
    panel: Panel,
    prop: REProperty,
    offsetX: Int,
    offsetY: Int,
    image: String
) : REPropertyWidget(panel, prop, offsetX, offsetY, image) {
    override fun hdgui2D(): String {
        return """-- device name / tape
${panel}_widgets[#${panel}_widgets + 1] = jbox.device_name {
  graphics = {
    node = "$nodeName",
  }
}"""
    }
}

/**
 * Placeholder property to reserve space in the back panel */
class REPlaceholderProperty(
    name: String,
    offsetX: Int,
    offsetY: Int,
    image: String
) : REBuiltInProperty(name) {
    init {
        addWidget(REPlaceholderWidget(this, offsetX, offsetY, image))
    }
}

/**
 * Placeholder (built-in) represented by a transparent image (so will not render). */
class REPlaceholderWidget(
    prop: REPlaceholderProperty,
    offsetX: Int,
    offsetY: Int,
    image: String
) : REPropertyWidget(Panel.back, prop, offsetX, offsetY, image) {
    override fun hdgui2D(): String {
        return """-- placeholder
${panel}_widgets[#${panel}_widgets + 1] = jbox.placeholder {
  graphics = {
    node = "$nodeName",
  }
}"""
    }
}
