package net.devkat.pegasus.view

import diode.react.ModelProxy
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import net.devkat.pegasus.model.{Flow, RootModel}
import net.devkat.pegasus.pages.Page

object Homepage {

  case class Props(router: RouterCtl[Page], proxy: ModelProxy[RootModel])

  // create the React component for Dashboard
  private val component = ReactComponentB[Props]("Homepage")
    .renderPS { (_, props, state) =>
      FlowView(props.proxy.zoom(_.flow), props.proxy.zoom(_.selection))
    }.
    build

  def apply(router: RouterCtl[Page], proxy: ModelProxy[RootModel]) =
    component(Props(router, proxy))
}
