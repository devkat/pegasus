package devkat.pegasus

import devkat.pegasus.model.ParagraphStyle
import devkat.pegasus.model.sequential.{Character, Flow}

package object layout {

  final case class Box(x: Double, y: Double, w: Double, h: Double)

  final case class Line(box: Box, elements: List[LineElement], style: ParagraphStyle)

  sealed abstract class LineElement(val box: Box)

  final case class Glyph(override val box: Box,
                         char: Character,
                         hidden: Boolean) extends LineElement(box)

  implicit class FlowSyntax(val flow: Flow) extends AnyVal {
    def dropSpaces: Flow =
      flow.dropWhile {
        case Character(' ', _) => true
        case _ => false
      }
  }

}
