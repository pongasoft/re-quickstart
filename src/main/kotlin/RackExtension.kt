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

    private val _tokens : Map<String, String> by lazy { generateTokens() }

    private val _gui2D: GUI2D = GUI2D(info)

    private val _reProperties: MutableCollection<IREProperty> = mutableListOf()

    private val _reAutoRouting: MutableCollection<IREAutoRouting> = mutableListOf()

    fun getPanelImageKey(panel: Panel) = _gui2D.getPanelImageKey(panel)

    fun generatePanel(panel: Panel) = _gui2D.generatePanelElement(panel)

    fun generateFullPanel(panel: Panel): HTMLCanvasElement {
        val canvas = _gui2D.generatePanelElement(panel)
        with(canvas.getContext("2d")) {
            this as CanvasRenderingContext2D
            _reProperties.forEach { prop -> prop.render(panel, this) }
        }
        return canvas
    }

    fun getWidth() = _gui2D.getWidth()
    fun getHeight(panel: Panel) = _gui2D.getHeight(panel)

    fun getTopLeft() = Pair(GUI2D.emptyMargin + GUI2D.hiResRailWidth, GUI2D.emptyMargin)

    fun addREProperty(prop: IREProperty) {

        _reProperties.add(prop)

        if(prop is AudioStereoPair)
            addREAutoRouting(StereoAudioRoutingPair(prop))
    }

    fun addREAutoRouting(routing: IREAutoRouting) = _reAutoRouting.add(routing)

    fun getPropertyImages(): List<ImageResource> {
        val images = mutableSetOf<ImageResource>()
        _reProperties.forEach { images.addAll(it.getImageResources()) }
        return images.toList()
    }

    fun processContent(content: String) : String {
        var processedContent = content
        for((tokenName, tokenValue) in _tokens) {
          processedContent = processedContent.replace(tokenName, tokenValue)
        }
        return processedContent
    }

    private fun generateTokens() : Map<String, String> {
        val newTokens = mutableMapOf<String, String>()

        val setToken = { key: String, value: String ->
          newTokens.getOrPut(key, {value})
        }

        // CMakeLists.txt
        setToken("cmake_project_name", info.productId.split(".").lastOrNull()?: "Blank")

        val imageKeys = Panel.values().map { getPanelImageKey(it) } + getPropertyImages().map { it.key }
        setToken("cmake-re_sources_2d", imageKeys.joinToString(separator = "\n") { "    \"\${RE_2D_SRC_DIR}/${it}.png\"" })

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

        Panel.values().forEach { panel ->
            // device_2D.lua
            setToken("device2D-${panel}_bg", _gui2D.getPanelImageKey(panel))
            setToken("device2D-$panel",
                _reProperties.map { it.device2D(panel) }.filter { it != "" }.joinToString(separator = "\n"))

            // hdgui_2D.lua
            setToken("hdgui2D-${panel}_widgets",
                _reProperties.map { it.hdgui2D(panel) }.filter { it != "" }.joinToString(separator = "\n\n")
            )
        }

        val t = newTokens.mapKeys { (k,_) -> "[-$k-]" }

        return t
    }
}