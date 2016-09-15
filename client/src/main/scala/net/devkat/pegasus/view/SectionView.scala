package net.devkat.pegasus.view

import diode.react.ModelProxy
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import net.devkat.pegasus.model.{Flow, Section, Selection}

object SectionView {

  case class Props(sectionProxy: ModelProxy[Section], selectionProxy: ModelProxy[Option[Selection]])

  private val component = ReactComponentB[Props]("SectionView")
    .renderP { (_, props) =>
      val section = props.sectionProxy()
      <.div(
        section.paragraphs map { paragraph =>
          ParagraphView(section.id, props.sectionProxy.zoom(_.paragraphs.find(_.id == paragraph.id).get), props.selectionProxy)
        }
      )
    }.
    build

  def apply(sectionProxy: ModelProxy[Section], selectionProxy: ModelProxy[Option[Selection]]) =
    component(Props(sectionProxy, selectionProxy))
}
