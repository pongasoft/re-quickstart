import org.w3c.dom.CanvasRenderingContext2D

/**
 * An audio socket can either be an input or an output */
enum class AudioSocketType { input, output }

/**
 * Represents the (built-in) audio socket. The default image provided is the one that comes with the SDK but it
 * is ignored anyway because the SDK uses its own representation */
class AudioSocket(
    name: String,
    val type: AudioSocketType,
    offsetX: Int,
    offsetY: Int,
    image: String
) : REProperty(name) {

    init {
        addWidget(AudioSocketWidget(type, this, offsetX, offsetY, image))
    }

    override val path: String
        get() = "/audio_${type}s/$name"

    override fun motherboard(): String {
        return """
audio_${type}s["$name"] = jbox.audio_$type {
  ui_name = jbox.ui_text("$name")
}
"""
    }
}

/**
 * Audio sockets can only be put on the back panel */
class AudioSocketWidget(val type: AudioSocketType,
                        prop: REProperty,
                        offsetX: Int,
                        offsetY: Int,
                        image: String) : REPropertyWidget(Panel.back, prop, offsetX, offsetY, image) {
    override fun hdgui2D(): String {
        return """--- ${prop.name} | audio $type socket
${panel}_widgets[#${panel}_widgets + 1] = jbox.audio_${type}_socket {
  graphics = {
    node = "$nodeName",
  },
  socket = "${prop.path}"
}
"""
    }
}

/**
 * Encapsulates the concept of an audio stereo pair (left and right). Significant for the motherboard routing call */
class AudioStereoPair(val left: AudioSocket, val right: AudioSocket) : IREProperty {

    /**
     * Adds a `add_stereo_audio_routing_pair` call so that Reason automatically wires both cables
     * when one is connected */
    override fun motherboard(): String {
        return """
-- stereo pair ${left.name} / ${right.name}            
${left.motherboard()}
${right.motherboard()}
jbox.add_stereo_audio_routing_pair {
  left = "${left.path}",
  right = "${right.path}"
}
"""
    }

    // device2D
    override fun device2D(panel: Panel): String {
        return if (panel == Panel.back) """
${left.device2D(panel)}            
${right.device2D(panel)}            
""" else ""
    }

    // hdgui2D
    override fun hdgui2D(panel: Panel): String {
        return if (panel == Panel.back) """
${left.hdgui2D(panel)}            
${right.hdgui2D(panel)}            
""" else ""
    }

    override fun render(panel: Panel, ctx: CanvasRenderingContext2D, imageProvider: ImageProvider) {
        left.render(panel, ctx, imageProvider)
        right.render(panel, ctx, imageProvider)
    }
}