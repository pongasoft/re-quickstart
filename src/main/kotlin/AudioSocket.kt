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
    image: String = "Cable_Attachment_Audio_01_1frames"
) : REProperty(name, listOf(AudioSocketWidget(type)), listOf(REPropertyView(Panel.back, offsetX, offsetY, image))) {

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
class AudioSocketWidget(val type: AudioSocketType) : REPropertyWidget(Panel.back) {
    override fun hdgui2D(prop: REProperty): String {
        return """
    --- ${prop.name} | audio $type socket
    jbox.audio_${type}_socket {
      graphics = {
        node = "${prop.nodeName(panel)}",
      },
      socket = "${prop.path}",
    },
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

    override fun render(panel: Panel, ctx: CanvasRenderingContext2D, storage: Storage) {
        left.render(panel, ctx, storage)
        right.render(panel, ctx, storage)
    }
}