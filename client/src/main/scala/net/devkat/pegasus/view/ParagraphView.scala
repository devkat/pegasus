package net.devkat.pegasus.view

import java.util.UUID

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import net.devkat.pegasus.actions.{ClearSelection, InsertCharacter, SetSelection}
import net.devkat.pegasus.model.{Character, Paragraph, Position, Selection}

object ParagraphView {

  case class Props(sectionId: UUID, paragraphProxy: ModelProxy[Paragraph], selectionProxy: ModelProxy[Option[Selection]])

  private val component = ReactComponentB[Props]("ParagraphView")
    .renderP { (_, props) =>
      val paragraphProxy = props.paragraphProxy
      val paragraph = paragraphProxy()
      val selectionOption = props.selectionProxy()

      <.div(
        paragraph.elements map { element =>
          val proxy = paragraphProxy.zoom(_.elements.find(_.id == element.id).get)
          element match {
            case Character(id, c) => CharacterView(
              props.sectionId,
              paragraph.id,
              paragraphProxy.zoom(_.elements.find(_.id == id).get),
              props.selectionProxy)
          }
        }
      )
    }.
    build

  def apply(sectionId: UUID, paragraphProxy: ModelProxy[Paragraph], selectionProxy: ModelProxy[Option[Selection]]) =
    component(Props(sectionId, paragraphProxy, selectionProxy))
}
