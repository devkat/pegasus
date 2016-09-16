package net.devkat.pegasus.view

import diode.react.ModelProxy
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import net.devkat.pegasus.model.{Flow, Section, Selection}

object SectionView {

  case class Props(sectionIndex: Int, sectionProxy: ModelProxy[Section], selectionProxy: ModelProxy[Option[Selection]])

  private val component = ReactComponentB[Props]("SectionView")
    .renderP { (_, props) =>
      val section = props.sectionProxy()
      <.div(
        section.children.zipWithIndex map { case (paragraph, index) =>
          ParagraphView(
            props.sectionIndex,
            index,
            props.sectionProxy.zoom(_.children(index)),
            props.selectionProxy)
        }
      )
    }.
    build

  def apply(sectionIndex: Int, sectionProxy: ModelProxy[Section], selectionProxy: ModelProxy[Option[Selection]]) =
    component(Props(sectionIndex, sectionProxy, selectionProxy))
}
