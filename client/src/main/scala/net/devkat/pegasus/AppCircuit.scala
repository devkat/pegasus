package net.devkat.pegasus

import diode._
import diode.react.ReactConnector
import net.devkat.pegasus.actions._
import net.devkat.pegasus.model._

/**
  * AppCircuit provides the actual instance of the `AppModel` and all the action
  * handlers we need. Everything else comes from the `Circuit`
  */
object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  def initialModel = RootModel(
    Flow(Vector(Section(children = Vector(Paragraph(children = Vector(Character(ch = 'a'))))))),
    None
  )

  override val actionHandler = composeHandlers(
    new FlowHandler(zoomRW(_.flow)((m, v) => m.copy(flow = v))),
    new SelectionHandler(zoomRW(_.selection)((m, v) => m.copy(selection = v)))
  )
}

