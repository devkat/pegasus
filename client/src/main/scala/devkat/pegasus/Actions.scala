package devkat.pegasus

import devkat.pegasus.fonts.Fonts
import devkat.pegasus.hyphenation.HyphenationSpec
import devkat.pegasus.model.sequential.Flow
import diode.Action
import diode.data.{Pot, PotAction}

object Actions {

  sealed trait Direction

  object Direction {
    case object Up extends Direction
    case object Down extends Direction
    case object Left extends Direction
    case object Right extends Direction
  }

  final case class ReplaceFlow(flow: Flow) extends Action

  final case class Insert(s: String) extends Action

  case object Delete extends Action

  case object Backspace extends Action

  final case class MoveCaret(direction: Direction) extends Action

  final case class ExpandSelection(direction: Direction) extends Action

  final case class SetCaret(index: Int) extends Action

  final case class MoveFocus(index: Int) extends Action

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


