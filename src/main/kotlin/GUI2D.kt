import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.files.Blob
import kotlin.js.Promise

/**
 * Extension function that converts the `toBlob` callback API into a `Promise` API. */
fun HTMLCanvasElement.toNamedBlob(name: String) : Promise<Pair<String, Blob>> {
    return Promise { resolve, reject ->
        toBlob(_callback = { blob : Blob? ->
            if(blob != null)
                resolve(Pair(name, blob))
            else
                reject(Exception("Cannot generate blob"))
        })
    }
}

/**
 * Represents the GUI2D section of the rack extension */
class GUI2D(val sizeInU : Int = 1,
            val frontPanelColor : dynamic = "blue",
            val backPanelColor : dynamic = "green",
            val emptyMarginColor : dynamic = "#FF0000A0") {
    companion object {
        const val emptyMargin = 25
        const val lowResScalingFactor = 5
        const val hiResWidth = 3770
        const val hiResHeight1U = 345
        const val hiResRailWidth = 155
        const val hiResFoldedHeight = 150
    }

    fun getWidth(panel: Panel) = hiResWidth

    fun getHeight(panel: Panel) = when(panel) {
        Panel.front, Panel.back -> hiResHeight1U * sizeInU
        Panel.folded_front, Panel.folded_back -> hiResFoldedHeight
    }

    fun getPanelImageName(panel: Panel) = "Panel_$panel.png"

    fun generatePanelElement(panel: Panel) =
        with(document.createElement("canvas")) {
            this as HTMLCanvasElement
            width = getWidth(panel)
            height = getHeight(panel)
            with(getContext("2d")) {
                this as CanvasRenderingContext2D

                // 1. draw the background
                fillStyle = if(panel.isFront()) frontPanelColor else backPanelColor
                val x = if(panel.isFront()) 0 else hiResRailWidth
                val w = if(panel.isFront()) width else width - 2 * hiResRailWidth
                fillRect(x.toDouble(), 0.toDouble(), w.toDouble(), height.toDouble())

                // 2. draw the margin that no widget should be on
                beginPath()
                lineWidth = emptyMargin.toDouble()
                strokeStyle = emptyMarginColor
                rect(emptyMargin / 2.0, emptyMargin / 2.0, width - emptyMargin.toDouble(), height - emptyMargin.toDouble())
                stroke()
            }
            this
        }

    /**
     * Generate the array of background images */
    fun generateBackgroundBlobs() : Array<Promise<Pair<String, Blob>>> {
        return Panel.values().map { panel -> generatePanelElement(panel).toNamedBlob(getPanelImageName(panel)) }.toTypedArray()
    }
}