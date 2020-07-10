package devkat.pegasus

import devkat.pegasus.fonts.Fonts
import devkat.pegasus.model.sequential.Flow
import diode.Action
import diode.data.{Pot, PotAction}

object Actions {

  final case class ReplaceFlow(flow: Flow) extends Action

  final case class Insert(c: Char) extends Action

  case class LoadFonts(potResult: Pot[Fonts] = Pot.empty)
    extends PotAction[Fonts, LoadFonts] {
    def next(newResult: Pot[Fonts]): LoadFonts =
      LoadFonts(newResult)
  }

}


