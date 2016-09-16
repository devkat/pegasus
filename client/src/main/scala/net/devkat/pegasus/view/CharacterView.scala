package net.devkat.pegasus.view

import diode.react.ModelProxy
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._
import net.devkat.pegasus.model.{Character, Selection}

object CharacterView extends ElementView[Character] {

  private val component = ReactComponentB[ElementProps[Character]]("CharacterView")
    .renderP { (_, props) =>
      val character = props.elementProxy()
      <.span(
        s"${character.ch}",
        selectionAttr(props),
        ^.onClick ==> onClick(props)
      )
    }.
    build

  def apply(sectionIndex: Int, paragraphIndex: Int, elementIndex: Int, characterProxy: ModelProxy[Character], selectionProxy: ModelProxy[Option[Selection]]) =
    component(ElementProps(sectionIndex, paragraphIndex, elementIndex, characterProxy, selectionProxy))

}
