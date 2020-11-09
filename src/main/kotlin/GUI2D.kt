import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

/**
 * Represents the GUI2D section of the rack extension */
class GUI2D(
    val info: RackExtension.Info,
    val frontPanelColor: dynamic = "#999999",
    val backPanelColor: dynamic = "#555555",
    val textFont: String = "50px monospace",
    val textColor: dynamic = "white"
) {
    companion object {
        const val emptyMargin = 25
        const val lowResScalingFactor = 5
        const val hiResWidth = 3770
        const val hiResHeight1U = 345
        const val hiResBackRailWidth = 155
        const val hiResNotePlayerBackRailWidth = 295
        const val hiResNotePlayerFrontRailWidth = 90
        const val hiResFoldedHeight = 150
    }

    fun getWidth() = hiResWidth

    fun getHeight(panel: Panel) = when (panel) {
        Panel.front, Panel.back -> hiResHeight1U * info.sizeInU
        Panel.folded_front, Panel.folded_back -> hiResFoldedHeight
    }

    fun getRailSize(panel: Panel) = when(panel) {
        Panel.front -> if(info.type == RackExtension.Type.note_player)  hiResNotePlayerFrontRailWidth else 0
        Panel.back -> if(info.type == RackExtension.Type.note_player)  hiResNotePlayerBackRailWidth else hiResBackRailWidth
        Panel.folded_back -> hiResBackRailWidth // note player are never folded
        Panel.folded_front -> 0 // note player are never folded
    }


    fun getPanelImageKey(panel: Panel) = "Panel_$panel"

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