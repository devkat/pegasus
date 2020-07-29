package devkat.pegasus

import devkat.pegasus.fonts.Fonts
import devkat.pegasus.hyphenation.HyphenationSpec
import devkat.pegasus.model.sequential.Flow
import diode.Action
import diode.data.{Pot, PotAction}

object Actions {

  final case class ReplaceFlow(flow: Flow) extends Action

  final case class Insert(c: Char) extends Action

  final case class Delete(from: Int, to: Int) extends Action

  final case class SetCaret(index: Int) extends Action

  final case class LoadFonts(potResult: Pot[Fonts])
    extends PotAction[Fonts, LoadFonts] {
    def next(newResult: Pot[Fonts]): LoadFonts =
      LoadFonts(newResult)
  }

  final case class LoadHyphenationSpec(potResult: Pot[HyphenationSpec])
    extends PotAction[HyphenationSpec, LoadHyphenationSpec] {
    def next(newResult: Pot[HyphenationSpec]): LoadHyphenationSpec =
      LoadHyphenationSpec(newResult)
  }

}


