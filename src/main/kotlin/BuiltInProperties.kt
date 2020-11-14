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
 * A built in property is provided by Reason and as result does not have a representation in the motherboard */
open class REBuiltInProperty(name: String) : REProperty(name) {
    /**
     * @return the definition of the property defined in the `motherboard_def.lua` file */
    override fun motherboard(): String = ""

    // textResources
    override fun textResources() = emptyMap<String, String>()
}

/**
 * Device name widget (built-in) represented by a tape */
class REDeviceNameWidget(
    panel: Panel,
    prop: REProperty,
    offsetX: Int,
    offsetY: Int,
    imageResource: ImageResource
) : REPropertyWidget(panel, prop, offsetX, offsetY, imageResource) {

    // hdgui2D
    override fun hdgui2D(): String {
        return """-- device name / tape
${panel}_widgets[#${panel}_widgets + 1] = jbox.device_name {
  graphics = {
    node = "$nodeName",
  }
}"""
    }
}

/**
 * Placeholder property to reserve space in the back panel */
class REPlaceholderProperty(
    name: String,
    offsetX: Int,
    offsetY: Int,
    imageResource: ImageResource
) : REBuiltInProperty(name) {
    init {
        addWidget(REPlaceholderWidget(this, offsetX, offsetY, imageResource))
    }
}

/**
 * Placeholder (built-in) represented by a transparent image (so will not render). */
class REPlaceholderWidget(
    prop: REPlaceholderProperty,
    offsetX: Int,
    offsetY: Int,
    imageResource: ImageResource
) : REPropertyWidget(Panel.back, prop, offsetX, offsetY, imageResource) {

    // hdgui2D
    override fun hdgui2D(): String {
        return """-- placeholder
${panel}_widgets[#${panel}_widgets + 1] = jbox.placeholder {
  graphics = {
    node = "$nodeName",
  }
}"""
    }
}
