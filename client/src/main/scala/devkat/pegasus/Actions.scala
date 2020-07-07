package devkat.pegasus

import devkat.pegasus.model.EditorModel.Flow
import devkat.pegasus.model.FontFamily
import diode.Action
import diode.data.{Pot, PotAction}

object Actions {

  final case class ReplaceFlow(flow: Flow) extends Action

  final case class Insert(c: Char) extends Action

  case class LoadFonts(potResult: Pot[List[FontFamily]] = Pot.empty)
    extends PotAction[List[FontFamily], LoadFonts] {
    def next(newResult: Pot[List[FontFamily]]): LoadFonts =
      LoadFonts(newResult)
  }

}


