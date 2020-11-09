format_version = "1.0"

--------------------------------------------------------------------------
-- Initialization
--------------------------------------------------------------------------
-- Custom properties
local documentOwnerProperties = {}
local rtOwnerProperties = {}
local guiOwnerProperties = {}

-- Audio Inputs/Outputs
audio_outputs = {}
audio_inputs = {}

-- CV Inputs/Outputs
cv_inputs = {}
cv_outputs = {}

--------------------------------------------------------------------------
-- Properties
--------------------------------------------------------------------------
[-motherboard_def-properties-]

--------------------------------------------------------------------------
-- Setup
--------------------------------------------------------------------------
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
