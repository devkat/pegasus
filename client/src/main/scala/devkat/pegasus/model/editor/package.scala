package devkat.pegasus.model

import devkat.pegasus.fonts.Fonts
import devkat.pegasus.hyphenation.HyphenationSpec
import devkat.pegasus.model.sequential.Flow
import diode.data.Pot

package object editor {

  final case class RootModel(editor: EditorModel)

  final case class EditorModel(flow: Flow,
                               selection: Option[Selection],
                               fonts: Pot[Fonts],
                               hyphenationSpec: Pot[HyphenationSpec],
                               status: Option[String])

  final case class Selection(anchor: Int, focus: Int)

}
