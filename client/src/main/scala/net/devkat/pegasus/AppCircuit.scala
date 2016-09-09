package net.devkat.pegasus

import diode._
import diode.react.ReactConnector
import net.devkat.pegasus.model.{Flow, RootModel}

/**
  * AppCircuit provides the actual instance of the `AppModel` and all the action
  * handlers we need. Everything else comes from the `Circuit`
  */
object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  def initialModel = RootModel(Flow())

  override val actionHandler = composeHandlers(
    new FlowHandler(zoomRW(_.flow)((m, v) => m.copy(flow = v))
  )
}

class FlowHandler[M](modelRW: ModelRW[M, Flow]) extends ActionHandler(modelRW) {

  override def handle = {
  }

}