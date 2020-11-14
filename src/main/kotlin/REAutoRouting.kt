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

/**
 * Defines the interface used for auto routing (specified in `motherboard_def.lua`) */
interface IREAutoRouting {
    /**
     * @return the routing section for `motherboard_def.lua` file */
    fun motherboard(): String
}

/**
 * Implementation for `jbox.add_stereo_audio_routing_pair` */
class StereoAudioRoutingPair(val socket: AudioStereoPair) : IREAutoRouting {
    /**
     * @return the routing section for `motherboard_def.lua` file */
    override fun motherboard(): String = """jbox.add_stereo_audio_routing_pair {
  left = "${socket.left.path}",
  right = "${socket.right.path}",
}"""
}

/**
 * Implementation for `jbox.add_stereo_effect_routing_hint` */
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

/**
 * Implementation for `jbox.set_effect_auto_bypass_routing` */
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

/**
 * Implementation for `jbox.add_stereo_instrument_routing_hint` */
class StereoInstrumentRoutingHint(val output: AudioStereoPair) : IREAutoRouting {
    /**
     * @return the routing section for `motherboard_def.lua` file */
    override fun motherboard(): String = """jbox.add_stereo_instrument_routing_hint {
  left_output = "${output.left.path}",
  right_output = "${output.right.path}"
}"""
}

/**
 * Implementation for `jbox.add_stereo_audio_routing_target` */
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