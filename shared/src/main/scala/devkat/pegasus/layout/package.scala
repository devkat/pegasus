package devkat.pegasus

import devkat.pegasus.model.ParagraphStyle
import devkat.pegasus.model.sequential.{Character, Flow}

package object layout {

  final case class Box(x: Double, y: Double, w: Double, h: Double)

  final case class Line(box: Box, elements: List[LineElement], style: ParagraphStyle)

  sealed abstract class LineElement(val index: Int, val box: Box)

  final case class Glyph(override val index: Int,
                         override val box: Box,
                         char: Character,
                         hidden: Boolean) extends LineElement(index, box)

}
