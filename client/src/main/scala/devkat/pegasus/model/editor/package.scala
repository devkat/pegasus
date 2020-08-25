package devkat.pegasus.model

import devkat.pegasus.fonts.Fonts
import devkat.pegasus.hyphenation.HyphenationSpec
import devkat.pegasus.layout.Line
import devkat.pegasus.model.sequential.Flow
import devkat.pegasus.view.SelectionView
import diode.data.Pot

package object editor {

  final case class RootModel(editor: EditorModel)

  final case class EditorModel(flow: Flow,
                               layout: List[Line],
                               selection: Option[Selection],
                               selectionView: Option[SelectionView],
                               fonts: Pot[Fonts],
                               hyphenationSpec: Pot[HyphenationSpec],
                               status: Option[String])

  final case class Selection(anchor: Int, focus: Int)

}
