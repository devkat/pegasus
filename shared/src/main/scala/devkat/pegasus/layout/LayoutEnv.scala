package devkat.pegasus.layout

import devkat.pegasus.fonts.Fonts

final case class LayoutSettings(showHiddenCharacters: Boolean)

final case class LayoutEnv(fonts: Fonts,
                           settings: LayoutSettings)
