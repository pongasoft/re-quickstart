interface IREAutoRouting {
    /**
     * @return the routing section for `motherboard_def.lua` file */
    fun motherboard(): String
}

class StereoAudioRoutingPair(val socket: AudioStereoPair) : IREAutoRouting {
    /**
     * @return the routing section for `motherboard_def.lua` file */
    override fun motherboard(): String = """jbox.add_stereo_audio_routing_pair {
  left = "${socket.left.path}",
  right = "${socket.right.path}",
}"""
}

class StereoEffectRoutingHint(
    val input: AudioStereoPair,
    val output: AudioStereoPair,
    val type: Type = Type.true_stereo
) : IREAutoRouting {

    enum class Type { true_stereo, mixing_stereo, spreading }

    /**
     * @return the routing section for `motherboard_def.lua` file */
    override fun motherboard(): String = """jbox.add_stereo_effect_routing_hint {
  type = "$type",
  left_input = "${input.left.path}",
  right_input = "${input.right.path}",
  left_output = "${output.left.path}",
  right_output = "${output.right.path}"
}"""
}

class EffectAutoBypassRouting(
    val input: AudioStereoPair,
    val output: AudioStereoPair
) : IREAutoRouting {

    /**
     * @return the routing section for `motherboard_def.lua` file */
    override fun motherboard(): String = """jbox.set_effect_auto_bypass_routing {
  {
    "${input.left.path}",
    "${output.left.path}"
  },
  {
    "${input.right.path}",
    "${output.right.path}"
  }
}"""
}

class StereoInstrumentRoutingHint(val output: AudioStereoPair) : IREAutoRouting {
    /**
     * @return the routing section for `motherboard_def.lua` file */
    override fun motherboard(): String = """jbox.add_stereo_instrument_routing_hint {
  left_output = "${output.left.path}",
  right_output = "${output.right.path}"
}"""
}

class StereoAudioRoutingTarget(val socket: AudioStereoPair, val signalType: SignalType = SignalType.normal, val autoRouteEnable: Boolean = true) : IREAutoRouting {
    enum class SignalType { send, `return`, normal }
    /**
     * @return the routing section for `motherboard_def.lua` file */
    override fun motherboard(): String = """jbox.add_stereo_audio_routing_target {
  signal_type = "$signalType",
  left = "${socket.left.path}",
  right = "${socket.right.path}",
  auto_route_enable = $autoRouteEnable
}"""
}