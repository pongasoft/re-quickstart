/**
 * A built in property is provided by Reason and as result does not have a representation in the motherboard */
class REBuiltInProperty(
    name: String,
    widgets: Iterable<REPropertyWidget>,
    views: Iterable<REPropertyView>
) : REProperty(name, widgets, views) {
    /**
     * @return the definition of the property defined in the `motherboard_def.lua` file */
    override fun motherboard(): String = ""
}

/**
 * Device name widget (built-in) represented by a tape */
class REDeviceNameWidget(panel: Panel) : REPropertyWidget(panel) {
    override fun hdgui2D(prop: REProperty): String {
        return """
    -- device name / tape
    jbox.device_name {
      graphics = {
        node = "${prop.nodeName(panel)}",
      },
    },
        """
    }
}
