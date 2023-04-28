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

import org.pongasoft.re_quickstart.BuildConfig
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLFormElement
import org.w3c.xhr.FormData

/**
 * Represents the rack extension */
class RackExtension(val info: Info) {

    /**
     * The type of the device (determines the default sockets created by quick start) */
    enum class Type { instrument, creative_fx, studio_fx, helper, note_player }

    /**
     * Encapsulates all the required info defining the rack extension */
    class Info(
        val longName: String,
        val mediumName: String,
        val shortName: String,
        val manufacturer: String,
        val productId: String,
        val version: String,
        val type: Type,
        val sizeInU: Int = 1
    )

    companion object {
        /**
         * Creates a rack extension from the values coming from the html form */
        fun fromForm(form: HTMLFormElement): RackExtension {
            val data = FormData(form)
            val params = data.keys().asSequence().associateBy({ it }, { e -> data.get(e).toString() })

            return RackExtension(
                Info(
                    longName = params["long_name"] ?: "Blank Plugin",
                    mediumName = params["medium_name"] ?: "Blank Plugin",
                    shortName = params["short_name"] ?: "Blank",
                    manufacturer = params["manufacturer"] ?: "acme",
                    productId = params["product_id"] ?: "com.acme.Blank",
                    version = params["version"] ?: "1.0.0d1",
                    type = Type.valueOf(params["device_type"] ?: Type.studio_fx.toString()),
                    sizeInU = params["device_height_ru"]?.toInt() ?: 1
                )
            )
        }
    }

    /**
     * Defines content processing (token replacement) */
    interface ContentProcessor {
        fun processContent(content: String) : String
    }

    /**
     * The GUI for the panel itself (background image) */
    private val _panelGUI: PanelGUI = PanelGUI(info)

    /**
     * List of all the properties (add via [addREProperty]) */
    private val _reProperties: MutableCollection<IREProperty> = mutableListOf()

    /**
     * List of all the auto routing (add via [addREAutoRouting]) */
    private val _reAutoRouting: MutableCollection<IREAutoRouting> = mutableListOf()

    /**
     * Which panels are available depend on type of device (for note players, there is no folded panels) */
    val availablePanels get() = when(info.type) {
        Type.note_player -> arrayOf(Panel.front, Panel.back)
        else -> Panel.values()
    }

    /**
     * The image key (used in device_2D.lua and hdgui_2D.lua) */
    fun getPanelImageKey(panel: Panel) = _panelGUI.getPanelImageKey(panel)

    /**
     * Generates the panel
     * @return the canvas for further processing/rendering */
    fun generatePanelCanvas(panel: Panel) = _panelGUI.generatePanelElement(panel)

    /**
     * Generates the panel preview which consists of the panel background and all properties rendered
     * (like audio sockets, display name, etc...)
     *
     * @return the canvas for further processing/rendering */
    fun generatePanelPreviewCanvas(panel: Panel): HTMLCanvasElement {
        val canvas = generatePanelCanvas(panel)
        with(canvas.getContext("2d")) {
            this as CanvasRenderingContext2D
            _reProperties.forEach { prop -> prop.render(panel, this) }
        }
        return canvas
    }

    /**
     * @return the width of the rack extension (which is fixed and does not depend on the panel) */
    fun getWidth() = _panelGUI.getWidth()

    /**
     * @return the height of the rack extension (depends on whether the panel is folded and the size in U) */
    fun getHeight(panel: Panel) = _panelGUI.getHeight(panel)

    /**
     * Returns the coordinates of the top left corner where it is safe to draw. This accounts for the required empty
     * margin (as defined by the RE SDK) and the rails (empty space that shows the rails or in the case of note player
     * an area that is discarded if you draw on it!)
     */
    fun getTopLeft(panel: Panel) = Pair(PanelGUI.emptyMargin + _panelGUI.getRailSize(panel), PanelGUI.emptyMargin)

    /**
     * Adds a property to the Rack Extension. */
    fun addREProperty(prop: IREProperty) {

        _reProperties.add(prop)

        if(prop is AudioStereoPair)
            addREAutoRouting(StereoAudioRoutingPair(prop))
    }

    /**
     * Adds auto routing information to the Rack Extension. */
    fun addREAutoRouting(routing: IREAutoRouting) = _reAutoRouting.add(routing)

    /**
     * Collect all (unique) property images across all panels. For example the tape representing the device name */
    fun getPropertyImages(): List<ImageResource> {
        val images = mutableSetOf<ImageResource>()
        _reProperties.forEach { images.addAll(it.getImageResources()) }
        return images.toList()
    }

    /**
     * Generate the content processor which will do token replacement for all text files in the plugin  */
    fun getContentProcessor() = object : ContentProcessor {

        val tokens = generateTokens()

        // simply replace each token in the content
        override fun processContent(content: String): String {
            var processedContent = content
            for((tokenName, tokenValue) in tokens) {
              processedContent = processedContent.replace(tokenName, tokenValue)
            }
            return processedContent
        }
    }

    /**
     * A token is of the form `[-xxx-]` and any such string will be replaced with its equivalent value */
    private fun generateTokens() : Map<String, String> {
        val newTokens = mutableMapOf<String, String>()

        val setToken = { key: String, value: String ->
          newTokens.getOrPut(key, {value})
        }

        // using SDK version 4.3.0
        setToken("re_sdk_version", "4.3.0")

        // CMakeLists.txt
        setToken("cmake_project_name", info.productId.split(".").lastOrNull()?: "Blank")

        setToken("cmake_re_cpp_src_dir", "\"\${RE_PROJECT_ROOT_DIR}/src/cpp\"")

        setToken("cmake_re_sources_cpp",
            arrayOf(
                "\${RE_CPP_SRC_DIR}/Device.h",
                "\${RE_CPP_SRC_DIR}/Device.cpp",
                "\${RE_CPP_SRC_DIR}/JukeboxExports.cpp")
                .joinToString(separator = "\n") { "    \"${it}\"" })

        val imageKeys = availablePanels.map { getPanelImageKey(it) } + getPropertyImages().map { it.key }
        setToken("cmake_re_sources_2d", imageKeys.joinToString(separator = "\n") { "    \"\${RE_2D_SRC_DIR}/${it}.png\"" })

        // options.cmake
        setToken("options_re_mock_support_for_audio_file", "OFF")
        setToken("options_extras", "")

        // info.lua
        setToken("info-long_name", info.longName)
        setToken("info-medium_name", info.mediumName)
        setToken("info-short_name", info.shortName)
        setToken("info-product_id", info.productId)
        setToken("info-manufacturer", info.manufacturer)
        setToken("info-version_number", info.version)
        setToken("info-device_type", info.type.toString())
        setToken("info-accepts_notes", (info.type == Type.instrument).toString())
        setToken("info-auto_create_track", (info.type == Type.instrument).toString())
        setToken("info-auto_create_note_lane", (info.type == Type.instrument).toString())
        setToken("info-device_height_ru", info.sizeInU.toString())

        // motherboard_def.lua
        setToken("motherboard_def-properties",
            _reProperties.map { it.motherboard() }.filter { it != "" }.joinToString(separator = "\n\n"))
        setToken("motherboard_def-auto_routing",
            _reAutoRouting.map { it.motherboard() }.filter { it != "" }.joinToString(separator = "\n\n"))

        // realtime_controller.lua
        setToken("realtime_controller-rt_input_setup",
            _reProperties.flatMap { it.rtInputSetup() }.joinToString(separator = ",\n") { "    \"$it\"" })

        // texts.lua
        setToken("texts-text_resources",
            _reProperties.flatMap { it.textResources().entries }.joinToString(separator = ",\n") { "    [\"${it.key}\"] = \"${it.value}\"" })

        availablePanels.forEach { panel ->
            // device_2D.lua
            setToken("device2D-${panel}_bg", _panelGUI.getPanelImageKey(panel))
            setToken("device2D-$panel",
                _reProperties.map { it.device2D(panel) }.filter { it != "" }.joinToString(separator = "\n"))

            // hdgui_2D.lua
            setToken("hdgui2D-${panel}_widgets",
                _reProperties.map { it.hdgui2D(panel) }.filter { it != "" }.joinToString(separator = "\n\n")
            )
        }

        // cpp
        setToken("test_class_name", "Device")
        setToken("test_includes", "#include <Device.h>")
        setToken("tester_device_type", when(info.type) {
            Type.instrument -> "InstrumentTester"
            Type.creative_fx -> "CreativeEffectTester"
            Type.studio_fx -> "StudioEffectTester"
            Type.helper -> "HelperTester"
            Type.note_player -> "NotePlayerTester"
        })

        // README.md
        setToken("re-quickstart-version", BuildConfig.VERSION)
        setToken("reason-browser-section", when(info.type) {
            Type.instrument -> "Instruments"
            Type.creative_fx, Type.studio_fx -> "Effects"
            Type.helper -> "Utilities"
            Type.note_player -> "Players"
        })

        val t = newTokens.mapKeys { (k,_) -> "[-$k-]" }

        return t
    }
}