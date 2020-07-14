package devkat.pegasus.layout

import devkat.pegasus.fonts.Fonts
import devkat.pegasus.hyphenation.HyphenationSpec

final case class LayoutSettings(showHiddenCharacters: Boolean,
                                hyphenate: Boolean)

final case class LayoutEnv(settings: LayoutSettings,
                           fonts: Fonts,
                           hyphenationSpec: HyphenationSpec)
