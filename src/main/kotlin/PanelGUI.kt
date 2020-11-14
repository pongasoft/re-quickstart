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

import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

/**
 * Represents the GUI2D section of the rack extension */
class PanelGUI(
    val info: RackExtension.Info,
    val frontPanelColor: dynamic = "#999999",
    val backPanelColor: dynamic = "#555555",
    val textFont: String = "50px monospace",
    val textColor: dynamic = "white"
) {
    // constants representing requirements from the SDK
    companion object {
        const val emptyMargin = 25
        const val hiResWidth = 3770
        const val hiResHeight1U = 345
        const val hiResBackRailWidth = 155
        const val hiResNotePlayerBackRailWidth = 295
        const val hiResNotePlayerFrontRailWidth = 90
        const val hiResFoldedHeight = 150
    }

    /**
     * @return the width of the rack extension (which is fixed and does not depend on the panel) */
    fun getWidth() = hiResWidth

    /**
     * @return the height of the rack extension (depends on whether the panel is folded and the size in U) */
    fun getHeight(panel: Panel) = when (panel) {
        Panel.front, Panel.back -> hiResHeight1U * info.sizeInU
        Panel.folded_front, Panel.folded_back -> hiResFoldedHeight
    }

    /**
     * @return the rails size for a given panel (empty space that shows the rails or in the case of note player an area
     *         that is discarded if you draw on it!)
     */
    fun getRailSize(panel: Panel) = when(panel) {
        Panel.front -> if(info.type == RackExtension.Type.note_player)  hiResNotePlayerFrontRailWidth else 0
        Panel.back -> if(info.type == RackExtension.Type.note_player)  hiResNotePlayerBackRailWidth else hiResBackRailWidth
        Panel.folded_back -> hiResBackRailWidth // note player are never folded
        Panel.folded_front -> 0 // note player are never folded
    }

    /**
     * @return the key used for the image panel (let's keep it simple...) */
    fun getPanelImageKey(panel: Panel) = "Panel_$panel"

    /**
     * Generate the panel as a (canvas) element: renders the background with a solid color while making sure that the
     * rails are transparent. Also add text based on the device information. */
    fun generatePanelElement(panel: Panel) =
        with(document.createElement("canvas")) {
            this as HTMLCanvasElement
            width = getWidth()
            height = getHeight(panel)
            with(getContext("2d")) {
                this as CanvasRenderingContext2D

                // 1. draw the background
                fillStyle = if (panel.isFront()) frontPanelColor else backPanelColor
                val x = getRailSize(panel)
                val w = width - 2 * x
                fillRect(x.toDouble(), 0.toDouble(), w.toDouble(), height.toDouble())

                // 2. Renders part of the information as text
                font = textFont
                val text = "${info.longName} | ${info.manufacturer} | ${info.version}"
                val metrics = measureText(text)
                fillStyle = textColor
                fillText(text, (width - metrics.width) / 2.0, height - metrics.actualBoundingBoxDescent - 5)
            }
            this
        }
}