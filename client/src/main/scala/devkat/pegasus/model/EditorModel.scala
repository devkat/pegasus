package devkat.pegasus.model

import diode.data.Pot

final case class EditorModel(flow: Seq[SeqElement],
                             selection: Option[Selection],
                             fonts: Pot[List[devkat.pegasus.fonts.FontFamily]],
                             status: Option[String])

final case class Selection(anchor: Int, focus: Int)

