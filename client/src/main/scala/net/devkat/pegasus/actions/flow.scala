package net.devkat.pegasus.actions

import java.util.UUID

import diode.ActionResult.ModelUpdate
import diode.{Action, ActionHandler, FastEq, ModelRW}
import net.devkat.pegasus.model.{HasChildren, _}

case class UpdateFlow(flow: Flow) extends Action

case class InsertCharacter(selection: Selection, ch: Char) extends Action

case class Replace(selection: Selection, ch: Char) extends Action

class HasChildrenModel[M, S <: HasChildren](rw: ModelRW[M, S]) {

  type T = S#ChildType

  def zoomToChild(index: Int)(update: (S, Vector[T]) => S): ModelRW[M, T] =
    rw.zoomRW(_.children(index))((m, v) => update(m, m.children.patch(index, Vector(v), 1)))

}

object HasChildrenModel {

  implicit def toHasChildrenModel[M, S <: HasChildren](modelRW: ModelRW[M, S]): HasChildrenModel[M, S]
    = new HasChildrenModel(modelRW)

}

class FlowHandler[M](modelRW: ModelRW[M, Flow]) extends ActionHandler(modelRW) {

  import HasChildrenModel._

  def zoomToParagraph(pos: Position): ModelRW[M, Paragraph] =
    modelRW.
      zoomToChild(pos.section)((m, s) => m.copy(children = s)).
      zoomToChild(pos.paragraph)((m, s) => m.copy(children = s))

  def zoomToElement(pos: Position): ModelRW[M, Element] =
    zoomToParagraph(pos).
      zoomToChild(pos.element)((m, s) => m.copy(children = s))

  /*
    modelRW.
      zoomRW(_.sections.find(_.id == pos.sectionId).get)((flow, section) => flow.copy(sections = replace(flow.sections, pos.sectionId, section))).
      zoomRW(_.paragraphs.find(_.id == pos.paragraphId).get)((section, paragraph) => section.copy(paragraphs = replace(section.paragraphs, pos.paragraphId, paragraph))).
      zoomRW(_.elements.find(_.id == pos.elementId).get)((paragraph, element) => paragraph.copy(elements = replace(paragraph.elements, pos.elementId, element)))
      */

  override def handle = {

    case UpdateFlow(flow) => updated(flow)

    case InsertCharacter(selection, ch) => {
      println(s"Insert character $ch")
      val focus = selection.focus
      val paragraphRW = zoomToParagraph(focus)
      val paragraph = paragraphRW.value
      ModelUpdate(paragraphRW.updated(paragraph.copy(
        children = paragraph.children.patch(focus.element, Vector(Character(ch)), 0))))
    }

    case Replace(selection, ch) => {
      println(s"Replace with $ch")
      val focus = selection.focus
      val paragraphRW = zoomToParagraph(focus)
      val paragraph = paragraphRW.value
      ModelUpdate(paragraphRW.updated(paragraph.copy(
        children = paragraph.children.patch(focus.element, Vector(Character(ch)), 1))))
    }

  }
}

