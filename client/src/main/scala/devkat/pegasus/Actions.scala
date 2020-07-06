package devkat.pegasus

import devkat.pegasus.model.EditorModel.Flow
import diode.Action

object Actions {

  final case class ReplaceFlow(flow: Flow) extends Action

  final case class Insert(c: Char) extends Action

}


