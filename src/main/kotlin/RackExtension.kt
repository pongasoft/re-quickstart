import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLFormElement
import org.w3c.files.Blob
import org.w3c.xhr.FormData
import kotlin.js.Promise

/**
 * Represents the rack extension */
class RackExtension(val info: Info) {

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

    private val _gui2D: GUI2D = GUI2D(info)

    private val _reProperties: MutableCollection<IREProperty> = mutableListOf()

    fun getPanelImageName(panel: Panel) = _gui2D.getPanelImageName(panel)

    fun generatePanel(panel: Panel) = _gui2D.generatePanelElement(panel)

    fun generateFullPanel(panel: Panel, imageProvider: ImageProvider): HTMLCanvasElement {
        val canvas = _gui2D.generatePanelElement(panel)
        with(canvas.getContext("2d")) {
            this as CanvasRenderingContext2D
            _reProperties.forEach { prop -> prop.render(panel, this, imageProvider) }
        }
        return canvas
    }

    fun getWidth() = _gui2D.getWidth()
    fun getHeight(panel: Panel) = _gui2D.getHeight(panel)

    fun getTopLeft(panel: Panel) = when (panel) {
        Panel.front, Panel.folded_front -> Pair(GUI2D.emptyMargin, GUI2D.emptyMargin)
        Panel.back, Panel.folded_back -> Pair(GUI2D.emptyMargin + GUI2D.hiResRailWidth, GUI2D.emptyMargin)
    }

    fun addREProperty(prop: IREProperty) = _reProperties.add(prop)

    fun getPropertyImages(): List<String> {
        val imgs = mutableSetOf<String>()
        _reProperties.forEach { imgs.addAll(it.getImages()) }
        return imgs.sorted()
    }

    fun infoLua(): String {
        return """
format_version = "1.0"

-- Note that changing this file requires a Reason/Recon restart

-- Max 40 chars
long_name = "${info.longName}"

-- Max 20 chars
medium_name = "${info.mediumName}"

-- Max 10 chars
short_name = "${info.shortName}"

product_id = "${info.productId}"
manufacturer = "${info.manufacturer}"
version_number = "${info.version}"
device_type = "${info.type}"
supports_patches = false
accepts_notes = ${info.type == Type.instrument}
auto_create_track = ${info.type == Type.instrument}
auto_create_note_lane = ${info.type == Type.instrument}
supports_performance_automation = false
device_height_ru = ${info.sizeInU}
automation_highlight_color = {r = 60, g = 255, b = 2}
"""
    }

    fun motherboardLua(): String {
        return """
format_version = "1.0"
            
--------------------------------------------------------------------------
-- Custom properties
--------------------------------------------------------------------------
local documentOwnerProperties = {}
local rtOwnerProperties = {}
local guiOwnerProperties = {}

custom_properties = jbox.property_set {
  gui_owner = {
    properties = guiOwnerProperties
  },

  document_owner = {
    properties = documentOwnerProperties
  },
	
  rtc_owner = {
    properties = {
      instance = jbox.native_object{ },
    }
  },
	
  rt_owner = {
    properties = rtOwnerProperties
  }
}

--------------------------------------------------------------------------
-- Audio Inputs/Outputs
--------------------------------------------------------------------------

audio_outputs = {}
audio_inputs = {}

${_reProperties.map { it.motherboard() }.filter { it != "" }.joinToString(separator = "\n\n")}

--------------------------------------------------------------------------
-- CV Inputs/Outputs
--------------------------------------------------------------------------
cv_inputs = {}
cv_outputs = {}

"""
    }

    fun realtimeControllerLua(): String {
        return """
format_version = "1.0"

rtc_bindings = {
  -- this will initialize the C++ object
  { source = "/environment/system_sample_rate", dest = "/global_rtc/init_instance" },
}

global_rtc = {
  init_instance = function(source_property_path, new_value)
    local sample_rate = jbox.load_property("/environment/system_sample_rate")
    local new_no = jbox.make_native_object_rw("Instance", { sample_rate })
    jbox.store_property("/custom_properties/instance", new_no);
  end,
}

rt_input_setup = {
  notify = {
${_reProperties.flatMap { it.rtInputSetup() }.joinToString(separator = ",\n") { "    \"$it\"" }}
  }
}

sample_rate_setup = {
  native = {
    22050,
    44100,
    48000,
    88200,
    96000,
    192000
  },

}
"""
    }

    fun textsLua() = """
format_version = "1.0"

-- english
texts = {
${_reProperties.flatMap { it.textResources().entries }.joinToString(separator = ",\n") { "    [\"${it.key}\"] = \"${it.value}\"" }}
}
"""

    fun device2DLua(): String {
        val content = Panel.values().map { panel ->
            """
--------------------------------------------------------------------------
-- $panel
--------------------------------------------------------------------------
$panel = {}

-- Background graphic
$panel["${panelNodeName(panel)}"] = "${_gui2D.getPanelImageName(panel).removeSuffix(".png")}"

${_reProperties.map { it.device2D(panel) }.filter { it != "" }.joinToString(separator = "\n")}"""
        }.joinToString(separator = "\n")

        return """
format_version = "2.0"
            
$content"""
    }

    fun hdgui2DLua(): String {
        val content = Panel.values().map { panel ->
            """
--------------------------------------------------------------------------
-- $panel
--------------------------------------------------------------------------
${panel}_widgets = {}

${_reProperties.map { it.hdgui2D(panel) }.filter { it != "" }.joinToString(separator = "\n\n")}

$panel = jbox.panel{
  graphics = {
    node = "${panelNodeName(panel)}",
  },
  widgets = ${panel}_widgets
}"""
        }.joinToString(separator = "\n")

        return """
format_version = "2.0"
            
$content
"""
    }

    /**
     * Generates the (promise) of the zip file
     * @return a (promise of a) pair where the first element is the name of the zip file and the second is the content */
    fun generateZip(): Promise<Pair<String, Blob>> {

        val root = "plugin"

        val zip = JSZip()
        val rootDir = zip.folder(root)

        return Promise.all(_gui2D.generateBackgroundBlobs()).then { array ->
            // add the background images (async generation)
            array.forEach { (name, blob) ->
                rootDir.file("GUI2D/$name", blob)
            }
        }.then {
            // generate the zip
            val options = object : JSZipGeneratorOptions {}.apply {
                type = "blob"
                platform = "UNIX"
            }

            zip.generateAsync(options)
        }.then {
            // return as a pair
            Pair("$root.zip", it as Blob)
        }
    }

    private fun panelNodeName(panel: Panel) = "Panel_${panel}_Bg"
}