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

import org.w3c.dom.CanvasRenderingContext2D

/**
 * An audio socket can either be an input or an output */
enum class AudioSocketType { input, output }

/**
 * Represents the (built-in) audio socket. */
class AudioSocket(
    name: String,
    val type: AudioSocketType,
    offsetX: Int,
    offsetY: Int,
    imageResource: ImageResource
) : REProperty(name) {

    init {
        addWidget(AudioSocketWidget(type, this, offsetX, offsetY, imageResource))
    }

    // path
    override val path: String
        get() = "/audio_${type}s/$name"

    // motherboard
    override fun motherboard(): String {
        return """audio_${type}s["$name"] = jbox.audio_$type {
  ui_name = jbox.ui_text("$name ui_name")
}"""
    }

    // we want to be notified if the socket is connected or disconnected
    override fun rtInputSetup() = listOf("$path/connected")
}

/**
 * Audio sockets can only be put on the back panel */
class AudioSocketWidget(val type: AudioSocketType,
                        prop: REProperty,
                        offsetX: Int,
                        offsetY: Int,
                        imageResource: ImageResource) :
    REPropertyWidget(Panel.back, prop, offsetX, offsetY, imageResource) {

    // hdgui2D
    override fun hdgui2D(): String {
        return """--- ${prop.name} | audio $type socket
${panel}_widgets[#${panel}_widgets + 1] = jbox.audio_${type}_socket {
  graphics = {
    node = "$nodeName",
  },
  socket = "${prop.path}"
}"""
    }
}

/**
 * Encapsulates the concept of an audio stereo pair (left and right). Significant for the motherboard routing call */
class AudioStereoPair(val left: AudioSocket, val right: AudioSocket) : IREProperty {

    /**
     * Adds a `add_stereo_audio_routing_pair` call so that Reason automatically wires both cables
     * when one is connected */
    override fun motherboard(): String {
        return """--------------------------------------------------------------------------
-- stereo pair ${left.name} / ${right.name}            
--------------------------------------------------------------------------
${left.motherboard()}
${right.motherboard()}"""
    }

    // device2D
    override fun device2D(panel: Panel): String {
        return if (panel == Panel.back) """${left.device2D(panel)}            
${right.device2D(panel)}""" else ""
    }

    // hdgui2D
    override fun hdgui2D(panel: Panel): String {
        return if (panel == Panel.back) """${left.hdgui2D(panel)}            
${right.hdgui2D(panel)}""" else ""
    }

    // rtInputSetup
    override fun rtInputSetup() = left.rtInputSetup() + right.rtInputSetup()

    // textResources
    override fun textResources() = left.textResources() + right.textResources()

    // render
    override fun render(panel: Panel, ctx: CanvasRenderingContext2D) {
        left.render(panel, ctx)
        right.render(panel, ctx)
    }

    // getImageResources
    override fun getImageResources(): List<ImageResource> = left.getImageResources() + right.getImageResources()
}