package devkat.pegasus.model

import devkat.pegasus.fonts.Fonts
import devkat.pegasus.model.sequential.Flow
import diode.data.Pot

final case class EditorModel(flow: Flow,
                             selection: Option[Selection],
                             fonts: Pot[Fonts],
                             status: Option[String])

final case class Selection(anchor: Int, focus: Int)

