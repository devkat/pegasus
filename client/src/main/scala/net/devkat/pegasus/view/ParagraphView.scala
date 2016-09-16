package net.devkat.pegasus.view

import java.util.UUID

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import net.devkat.pegasus.actions.{ClearSelection, InsertCharacter, SetSelection}
import net.devkat.pegasus.model.{Character, Paragraph, Position, Selection}

object ParagraphView {

  case class Props(sectionIndex: Int, paragraphIndex: Int, paragraphProxy: ModelProxy[Paragraph], selectionProxy: ModelProxy[Option[Selection]])

  private val component = ReactComponentB[Props]("ParagraphView")
    .renderP { (_, props) =>
      val paragraphProxy = props.paragraphProxy
      val paragraph = paragraphProxy()
      val selectionOption = props.selectionProxy()

      <.div(
        paragraph.children.zipWithIndex map { case (element, index) =>
          val proxy = paragraphProxy.zoom(_.children(index))
          element match {
            case Character(c) => CharacterView(
              props.sectionIndex,
              props.paragraphIndex,
              index,
              paragraphProxy.zoom(_.children(index).asInstanceOf[Character]),
              props.selectionProxy)
          }
        }
      )
    }.
    build

  def apply(sectionIndex: Int, paragraphIndex: Int, paragraphProxy: ModelProxy[Paragraph], selectionProxy: ModelProxy[Option[Selection]]) =
    component(Props(sectionIndex, paragraphIndex, paragraphProxy, selectionProxy))
}
