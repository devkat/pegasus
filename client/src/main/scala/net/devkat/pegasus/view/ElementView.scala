package net.devkat.pegasus.view

import java.util.UUID

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import net.devkat.pegasus.actions.{ClearSelection, SetSelection}
import net.devkat.pegasus.model.{Element, Position, Selection}
import org.scalajs.dom.raw.HTMLElement

case class ElementProps[T <: Element](
  sectionIndex: Int,
  paragraphIndex: Int,
  elementIndex: Int,
  elementProxy: ModelProxy[T],
  selectionProxy: ModelProxy[Option[Selection]]
)

abstract class ElementView[T <: Element] {

  type Props = ElementProps[T]

  def selectionAttr(props: Props) = props.selectionProxy().
    map(_.focus).
    filter(_ == Position(props.sectionIndex, props.paragraphIndex, props.elementIndex)).
    map(_ => ^.borderRight := "solid 1px black")

  def onClick(props: Props)(e: ReactMouseEvent) = {
    e.stopPropagation()
    e.target.asInstanceOf[HTMLElement].focus()
    val pos = Position(props.sectionIndex, props.paragraphIndex, props.elementIndex)
    println(s"Updating selection to $pos")
    props.elementProxy.dispatch(SetSelection(Selection(pos, pos)))
  }

}
