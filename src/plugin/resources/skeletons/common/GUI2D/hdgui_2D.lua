format_version = "2.0"

--------------------------------------------------------------------------
-- front
--------------------------------------------------------------------------
front_widgets = {}

[-hdgui2D-front_widgets-]

front = jbox.panel{
  graphics = {
    node = "Panel_front_bg",
  },
  widgets = front_widgets
}

--------------------------------------------------------------------------
-- folded_front
--------------------------------------------------------------------------
folded_front_widgets = {}

[-hdgui2D-folded_front_widgets-]

folded_front = jbox.panel{
  graphics = {
    node = "Panel_folded_front_bg",
  },
  widgets = folded_front_widgets
}

--------------------------------------------------------------------------
-- back
--------------------------------------------------------------------------
back_widgets = {}

[-hdgui2D-back_widgets-]

back = jbox.panel{
  graphics = {
    node = "Panel_back_bg",
  },
  widgets = back_widgets
}

--------------------------------------------------------------------------
-- folded_back
--------------------------------------------------------------------------
folded_back_widgets = {}

[-hdgui2D-folded_back_widgets-]

folded_back = jbox.panel{
  graphics = {
    node = "Panel_folded_back_bg",
  },
  cable_origin = {
    node = "CableOrigin",
  },
  widgets = folded_back_widgets
}
