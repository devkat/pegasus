package net.devkat.pegasus.view

import diode.react.ModelProxy
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.{Callback, _}
import net.devkat.pegasus.actions.{ClearSelection, InsertCharacter}
import net.devkat.pegasus.model.{Flow, Selection}

object FlowView {

  case class Props(flowProxy: ModelProxy[Flow], selectionProxy: ModelProxy[Option[Selection]])

  private val component = ReactComponentB[Props]("FlowView")
    .renderP { (_, props) =>
      val flowProxy = props.flowProxy
      val flow = flowProxy()
      val selectionOption = props.selectionProxy()

      def onKeyUp(e: ReactKeyboardEvent) = {
        println("key up")
        val ch = e.charCode.toChar
        println(s"Inserting $ch")
        selectionOption.map(sel => flowProxy.dispatch(InsertCharacter(sel, ch))).getOrElse(Callback.empty)
      }

      def onClick(e: ReactMouseEvent) =
        flowProxy.dispatch(ClearSelection)

      <.div(
        ^.tabIndex := 0,
        ^.onKeyUp ==> onKeyUp,
        ^.onClick ==> onClick,
        flow.sections map { section =>
          SectionView(props.flowProxy.zoom(_.sections.find(_.id == section.id).get), props.selectionProxy)
        }
      )
    }.
    build

  def apply(flowProxy: ModelProxy[Flow], selectionProxy: ModelProxy[Option[Selection]]) =
    component(Props(flowProxy, selectionProxy))
}
