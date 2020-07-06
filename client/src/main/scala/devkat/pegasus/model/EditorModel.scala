package devkat.pegasus.model

import devkat.pegasus.model.EditorModel.Element.ParagraphBreak
import devkat.pegasus.model.EditorModel.Selection
import devkat.pegasus.model.Element.Character
import devkat.pegasus.model.{Element => FlowElement, Flow => TextFlow}

final case class EditorModel(flow: EditorModel.Flow, selection: Option[Selection])

object EditorModel {

  type Flow = Vector[Element]

  final case class StyledElement(element: Element, style: Style)

  sealed trait Element

  object Element {

    final case class Glyph(char: Char) extends Element

    case object ParagraphBreak extends Element

    def fromFlowElement: FlowElement => Element = {
      case Character(c) => Glyph(c)
    }

  }

  type Style = Map[String, String]

  final case class Selection(anchor: Int, focus: Int)

  def fromFlow(flow: TextFlow): EditorModel =
    EditorModel(
      flow.sections.flatMap(
        _.paragraphs.flatMap(
          _.elements.map(Element.fromFlowElement) :+ ParagraphBreak
        )
      ),
      None
    )

}

