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
            val backPanelColor : dynamic = "green") {
    companion object {
        const val lowResScalingFactor = 5
        const val hiResWidth = 3770
        const val hiResHeight1U = 345
        const val hiResRailWidth = 155
        const val hiResFoldedHeight = 150
        const val frontPanelName = "Panel_Front.png"
        const val backPanelName = "Panel_Back.png"
        const val foldedFrontPanelName = "Panel_Folded_Front.png"
        const val foldedBackPanelName = "Panel_Folded_Back.png"
    }

    val width get() = hiResWidth
    val height get() = hiResHeight1U * sizeInU
    val foldedWidth get() = width
    val foldedHeight get() = hiResFoldedHeight

    /**
     * Front panel image (using frontPanelColor) */
    fun generateFrontPanelElement() = with(document.createElement("canvas")) { this as HTMLCanvasElement
        width = this@GUI2D.width
        height = this@GUI2D.height
        with(getContext("2d")) { this as CanvasRenderingContext2D
            fillStyle = frontPanelColor
            fillRect(0.toDouble(), 0.toDouble(), width.toDouble(), height.toDouble())
        }
        this
    }

    /**
     * Back panel image (using backPanelColor) */
    fun generateBackPanelElement() = with(document.createElement("canvas")) { this as HTMLCanvasElement
        width = this@GUI2D.width
        height = this@GUI2D.height
        with(getContext("2d")) { this as CanvasRenderingContext2D
            fillStyle = backPanelColor
            fillRect(hiResRailWidth.toDouble(), 0.toDouble(), (width - 2 * hiResRailWidth).toDouble(), height.toDouble())
        }
        this
    }

    /**
     * Folded front panel image (using frontPanelColor) */
    fun generateFoldedFrontPanelElement() = with(document.createElement("canvas")) { this as HTMLCanvasElement
        width = foldedWidth
        height = foldedHeight
        with(getContext("2d")) { this as CanvasRenderingContext2D
            fillStyle = frontPanelColor
            fillRect(0.toDouble(), 0.toDouble(), foldedWidth.toDouble(), foldedHeight.toDouble())
        }
        this
    }

    /**
     * Folded back panel image (using backPanelColor) */
    fun generateFoldedBackPanelElement() = with(document.createElement("canvas")) { this as HTMLCanvasElement
        width = foldedWidth
        height = foldedHeight
        with(getContext("2d")) { this as CanvasRenderingContext2D
            fillStyle = backPanelColor
            fillRect(hiResRailWidth.toDouble(), 0.toDouble(), (foldedWidth - 2 * hiResRailWidth).toDouble(), foldedHeight.toDouble())
        }
        this
    }

    /**
     * Generate the array of background images */
    fun generateBackgroundBlobs() : Array<Promise<Pair<String, Blob>>> {
        return arrayOf(
            generateFrontPanelElement().toNamedBlob(frontPanelName),
            generateBackPanelElement().toNamedBlob(backPanelName),
            generateFoldedFrontPanelElement().toNamedBlob(foldedFrontPanelName),
            generateFoldedBackPanelElement().toNamedBlob(foldedBackPanelName),
        )
    }
}