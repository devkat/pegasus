package devkat.pegasus.model

import devkat.pegasus.fonts.Fonts
import devkat.pegasus.model.sequential.Flow
import diode.data.Pot

package object editor {

  final case class EditorModel(flow: Flow,
                               selection: Option[Selection],
                               fonts: Pot[Fonts],
                               settings: Settings,
                               status: Option[String])

  final case class Selection(anchor: Int, focus: Int)

  final case class Settings(hyphenate: Boolean)

  object Settings {

    lazy val default: Settings =
      Settings(
        hyphenate = true
      )

  }
}
