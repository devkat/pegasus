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

      def onKeyPress(e: ReactKeyboardEvent) = {
        e.preventDefault()
        selectionOption.map(sel =>
          flowProxy.dispatch(InsertCharacter(sel, e.charCode.toChar))
        ).getOrElse(Callback.empty)
      }

      def onClick(e: ReactMouseEvent) =
        flowProxy.dispatch(ClearSelection)

      <.div(
        ^.`class` := "pegasus",
        ^.tabIndex := 0,
        ^.onKeyPress ==> onKeyPress,
        //^.onKeyDown ==> { (e: ReactKeyboardEvent) => Callback(e.preventDefault()) },
        ^.onKeyUp ==> { (e: ReactKeyboardEvent) => Callback(e.preventDefault()) },
        ^.onClick ==> onClick,
        flow.children.zipWithIndex map { case (section, index) =>
          SectionView(
            index,
            props.flowProxy.zoom(_.children(index)),
            props.selectionProxy)
        }
      )
    }.
    build

  def apply(flowProxy: ModelProxy[Flow], selectionProxy: ModelProxy[Option[Selection]]) =
    component(Props(flowProxy, selectionProxy))
}
