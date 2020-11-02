/**
 * A built in property is provided by Reason and as result does not have a representation in the motherboard */
class REBuiltInProperty(name: String) : REProperty(name) {
    /**
     * @return the definition of the property defined in the `motherboard_def.lua` file */
    override fun motherboard(): String = ""
}

/**
 * Device name widget (built-in) represented by a tape */
class REDeviceNameWidget(panel: Panel,
                         prop: REProperty,
                         offsetX: Int,
                         offsetY: Int,
                         image: String) : REPropertyWidget(panel, prop, offsetX, offsetY, image) {
    override fun hdgui2D(): String {
        return """-- device name / tape
${panel}_widgets[#${panel}_widgets + 1] = jbox.device_name {
  graphics = {
    node = "$nodeName",
  }
}
"""
    }
}
