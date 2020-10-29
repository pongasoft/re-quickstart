enum class AudioSocketType { input, output }

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

class AudioStereoPair(val left: AudioSocket, val right: AudioSocket) : IREProperty {

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

    override fun device2D(panel: Panel): String {
        return if (panel == Panel.back) """
${left.device2D(panel)}            
${right.device2D(panel)}            
""" else ""
    }

    override fun hdgui2D(panel: Panel): String {
        return if (panel == Panel.back) """
${left.hdgui2D(panel)}            
${right.hdgui2D(panel)}            
""" else ""
    }
}